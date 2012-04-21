
(ns loa.format.card-text
  (:require (loa.format (common :as common_))
            (loa.util (data :as data_)
                      (util :as util_))))

;;-------------------------------------------------
;;
;;  Helpers
;;
(defn- get-count
  "Counts occurances of value in seq."
  [seq value]
  (count (filter #{value} seq)))

(defn- third
  [seq]
  (nth seq 2 nil))

(defn- fix-planeswalker-rule
  [rule]
  (when rule
    (if-let [[_ cost text] (re-matches #"([+-]?[\dX]+):(.*)" rule)]
      (format "[%s]%s" cost text)
      rule)))

(defn- restore-protection-rule
  [text]
  (when text
    (.replaceAll text
                 "([Pp]rotection) from (\\w+), protection from (\\w+)"
                 "$1 from $2 and from $3")))

(defn- from-rule
  [rule reminder]
  (let [rule (fix-planeswalker-rule rule)]
    (str
     (or (util_/ascii-string rule) "")
     (if (and rule reminder)
       " " "")
     (if reminder
       (format "(%s)" reminder)
       ""))))

(defn- from-single-rule
  [rule-coll]
  (let [reminder (-> rule-coll first :reminder)
        text (map :text rule-coll)]
    (from-rule (->> (cons (first text)
                          (map (memfn toLowerCase) (rest text)))
                    (interpose ", ")
                    (reduce str)
                    restore-protection-rule)
               reminder)))

(defn- from-rules
  [rule-coll]
  (let [lines (partition-by :no rule-coll)]
    (->> (map from-single-rule lines)
         (interpose \newline)
         (apply str))))

(let [pre-type? (apply conj data_/super-type data_/card-type)]
  (defn- from-types
    [types]
    (let [pre (take-while pre-type? types)
          post (drop-while pre-type? types)]
      (->> (concat pre (when-not (or (empty? pre) (empty? post)) ["-"]) post)
           (interpose " ")
           (apply str)))))

;;--------------------------------------------------
;;
;;  String transformers
;;
(defn- from-card
  [card]
  (let [strings
        [(util_/ascii-string (:name card))
         (when (:cost card)
           (common_/format-mana (:cost card)))
         (when (:color card)
           (format "(%s)" (:color card)))
         (from-types (map util_/ascii-string (:types card)))
         (when (:loyalty card)
           (:loyalty card))
         (when (:pow card)
           (str (:pow card) "/" (:tgh card)))
         (when (:hand card)
           (format "Hand %s, life %s" (:hand card) (:life card)))
         (when-not (empty? (:rules card))
           (from-rules (:rules card)))
         (when-not (empty? (:multi card))
           (str "----"
                \newline
                (from-card (-> card :multi first))))]]
    (->> strings
         (filter identity)
         (interpose \newline)
         (reduce str))))

(defn- from-meta
  [meta set-map]
  (let [meta (->> meta
                  (sort-by #(-> % :set set-map :release-date))
                  (map (fn [{:keys [set rarity]}]
                         (str (-> set set-map :code) " "
                              (get-in data_/rarity-mapping
                                      [rarity :code]
                                      ;; TODO report
                                      ))))
                  (partition-by identity)
                  (map (fn [group]
                         (let [amount (count group)]
                           (str (first group)
                                (when-not (= 1 amount)
                                  (format " (x%d)" amount))))))
                  (interpose ", "))]
    (apply str meta)))

(defn- full-info
  [card set-map]
  (str (from-card card)
       \newline
       (from-meta (:meta card) set-map)))

(defn- oneline-set
  [set]
  (format "%-6s %-10s  %s"
          (:code set)
          (let [date (:release-date set)]
            (if (empty? date) "?" date))
          (:name set)))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn data
  [card-coll set-coll]
  (let [set-map (reduce #(assoc %1 (:name %2) %2) nil set-coll)]
    (map #(full-info % set-map) card-coll)))

(defn set-info
  "Returns a string representing the set-info."
  [set-coll]
  (->> set-coll
       (remove (comp empty? :code))
       (sort-by :code)
       (map oneline-set)
       (interpose \newline)
       (reduce str)))
