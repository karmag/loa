
(ns loa.acquisition.set-handler
  (:require [clojure.contrib
             [lazy-xml :as lazy-xml]
             [io :as io]]
            [loa.transformation.transform :as transform]
            [loa.util
             [util :as util]
             [log :as log]])
  (:import java.io.File
           java.io.StringReader
           javax.xml.parsers.SAXParserFactory))

(declare parse-raw-card)

(def #^{:private true}
     labels
     {"Name:" :name
      "Cost:" :cost
      "Type:" :types
      "Pow/Tgh:" :pt
      "Rules Text:" :rules
      "Set/Rarity:" :set-rarity
      "Loyalty:" :loyalty})

;;-------------------------------------------------
;;
;;  Multi-card parsing
;;
(defn- process-double-card
  [cardmap master-old master-new slave-old slave-new]
  (let [slave (-> (get cardmap slave-old)
                  (assoc :name slave-new
                         :multi-type :double)
                  (dissoc :set-rarity))
        master (-> (get cardmap master-old)
                   (assoc :name master-new)
                   (update-in [:multi] conj slave))]
    (-> cardmap
        (assoc master-new master)
        (dissoc master-old)
        (dissoc slave-old))))

(defn- fix-double-cards
  "Fixes double-cards (with // in their name)."
  [cardmap]
  (let [doubles (filter #(.contains % "//") (keys cardmap))
        fmt-string  "%s // %s (%s)"]
    (reduce (fn [cardmap name]
              (if (get cardmap name)
                (let [[_ master slave]
                      (map (memfn trim)
                           (re-matches #"(.*)//(.*)\(.*\)" name))]
                  (process-double-card cardmap
                                       (format fmt-string master slave master)
                                       master
                                       (format fmt-string master slave slave)
                                       slave))
                cardmap))
            cardmap
            doubles)))

(let [safe-name #{"B.F.M. (Big Furry Monster)"
                  "Erase (Not the Urza's Legacy One)"}]
  (defn- rectify-name
    "Change the name of a card."
    [cardmap oldname newname]
    (if (safe-name oldname)
      cardmap
      (let [card (assoc (get cardmap oldname)
                   :name newname)]
        (-> cardmap
            (dissoc oldname)
            (assoc newname card))))))

(defn- process-multi-named-card
  "Adds a partial card onto an actual card, or simply renames the
  master card."
  [cardmap master-name slave-old slave-new]
  (if (get cardmap master-name)
    (let [master (get cardmap master-name)
          slave (assoc (get cardmap slave-old)
                  :name slave-new
                  :multi-type :flip)
          slave (dissoc slave :cost)]
      (-> cardmap
          (update-in [master-name :multi] conj slave)
          (dissoc slave-old)))
    (rectify-name cardmap slave-old slave-new)))

(defn- fix-multi-named-cards
  "Reforms card with 'name (other name)' into flip cards, or just
  fixes the name."
  [cardmap]
  (let [doubles (filter #(and (.contains % "(")
                              (not (.contains % "//")))
                        (keys cardmap))]
    (reduce (fn [cards name]
              (let [[_ master slave]
                    (re-matches #"(.+?)\s*\((.+?)\)" name)]
                (process-multi-named-card cards master name slave)))
            cardmap
            doubles)))

(defn- make------card
  [rules]
  (let [[name types pt & rules] rules]
    (parse-raw-card
     [[:name name]
      [:types types]
      [:pt (format "(%s)" pt)]
      (apply vector :rules rules)])))

(defn- fix------flip-cards
  "Fixes flipcards that have all their info in one card instead of
  spread out over multiple cards."
  [cardmap]
  (reduce (fn [cardmap card]
            (if (some #(= "----" (:text %)) (:rules card))
              (let [regular (take-while #(not= "----" (:text %)) (:rules card))
                    flip (nthnext (:rules card) (inc (count regular)))
                    flip (make------card (map :text flip))]
                (-> cardmap
                    (assoc-in [(:name card) :rules] regular)
                    (update-in [(:name card) :multi] conj
                               (assoc flip :multi-type :flip))))
              cardmap))
          cardmap
          (vals cardmap)))

(defn- fix-multi-cards
  "Straighten out multi-cards."
  [cardseq]
  (-> (reduce #(assoc %1 (:name %2) %2) nil cardseq)
      fix-double-cards
      fix-multi-named-cards
      fix------flip-cards
      vals))

;;-------------------------------------------------
;;
;;  Card helpers
;;
(defn- parse-types
  [types]
  (map (memfn trim)
       (.split types
               (str "[ " (char 8212) "]+"))))

(defn- parse-rule
  [rule]
  (let [rule (transform/fix-mana rule)
        [_ text reminder] (map #(when %
                                  (.trim %))
                               (re-matches #"(.*?)(\(.*\))?" rule))]
    (when (and (= 0 (count (or text "")))
               (= 0 (count (or reminder ""))))
      (throw (Exception.
              (str "Rule '" rule "' has no text or reminder-text."))))
    (merge nil
           (when (not= 0 (count (or text "")))
             {:text text})
           (when (not= 0 (count (or reminder "")))
             {:reminder (.substring reminder 1 (dec (count reminder)))}))))

(defn- get-set-rarity
  [s]
  (let [r-coll ["Common" "Uncommon" "Mythic Rare" "Rare" "Land" "Special"]
        r-map (apply hash-map (interleave r-coll
                                          [:common :uncommon
                                           :mythic :rare
                                           :land :special]))
        rarity (some #(when (.endsWith s %) %) r-coll)]
    [(.trim (.substring s 0 (- (.length s) (count rarity))))
     (r-map rarity)]))

;; parse-value
(defmulti #^{:private true}
  parse-value (fn [type & args]
                (if-not (empty? args)
                  type
                  :no-value)))

(defmethod parse-value :no-value
  [& _])

(defmethod parse-value :default
  [type & coll]
  (when-not (empty? coll)
    {type (apply str coll)}))

(defmethod parse-value :cost
  [_ & [cost]]
  (when cost
    {:cost (transform/fix-mana cost)}))

(defmethod parse-value :loyalty
  [_ & [loyalty]]
  {:loyalty (apply str (remove #{\( \)} loyalty))})

(defmethod parse-value :types
  [_ & [types]]
  (let [types (parse-types types)]
    {:types (if (= "Plane" (first types))
              [(-> types first)
               (apply str (interpose " " (rest types)))]
              types)}))

(defmethod parse-value :pt
  [_ & [pt]]
  (when pt
    (let [[_ p t] (re-matches #"\((.*)/(.*)\)" pt)]
      {:pow p :tgh t})))

(defmethod parse-value :rules
  [_ & rules]
  (let [rules (filter identity rules)]
    (when-not (empty? rules)
      {:rules (map parse-rule rules)})))

(defmethod parse-value :set-rarity
  [_ setrare]
  {:set-rarity
   (map get-set-rarity (map (memfn trim) (.split setrare ",")))})

;;-------------------------------------------------
;;
;;  Fixes
;;
(defn- fix-vanguard
  [card]
  (if (some #{"Vanguard"} (:types card))
    (-> card
        (assoc :hand (:pow card))
        (assoc :life (:tgh card))
        (dissoc :pow :tgh))
    card))

;;-------------------------------------------------
;;
;;  Public helpers
;;
(defn create-set-reader
  "Returns the raw set data as a Reader."
  [set-search-url setname]
  (util/get-uri-reader
   (format set-search-url (.replaceAll setname " " "%20"))))

(defn parse-raw-set
  "Returns card-data as a seq of vectors containing a keyword
  and a seq."
  [reader]
  (with-open [reader (-> #"(?m)(?s).*<div class=\"textspoiler\">(.*?)</div>.*"
                         (re-matches (io/slurp* reader))
                         second
                         (.replace "R&D" "R&amp;D") ;; unhinged fixes
                         (.replace " & " " &amp; ")
                         StringReader.)]
    (let [data (->> (lazy-xml/parse-seq reader)
                    (filter #(= (:type %) :characters))
                    (map #(-> % :str .trim)))
          data (-> data
                   (util/lazy-split (set (keys labels)))
                   (util/lazy-split #(= (first %) "Name:")))]
      (doall
       (map #(map (fn [[k & v]]
                    (apply vector (labels k) v))
                  %)
            data)))))

(defn parse-raw-card
  "Transforms raw a card into a proper card."
  [kv-seq]
  (-> (reduce merge
              nil
              (map #(apply parse-value %) kv-seq))
      fix-vanguard))

;;-------------------------------------------------
;;
;;  Interface
;;
(defn get-set-names
  "Returns a seq of all the set-names."
  [main-url]
  (->> (util/get-url-data main-url)
       (re-matches
        #"(?m)(?s).*Filter Card Set:.*?<select.*?>(.*?)</select>.*")
       second
       (re-seq #">(.*?)<")
       (map second)
       (remove #(= 0 (.length %)))
       (map #(.replaceAll % "&quot;" "\""))))

(defn get-cards
  "Returns the cards associated with the set."
  [set-search-url setname]
  (log/debug (str "Getting cards for set [" setname "]"))
  (with-open [reader (create-set-reader set-search-url setname)]
    (doall
     (fix-multi-cards
      (map parse-raw-card (parse-raw-set reader))))))
