
(ns loa.program.loa
  (:require [clojure.contrib
             [io :as io]
             [prxml :as prxml]]
            [loa.acquisition.card-details-handler :as card-details-handler]
            [loa.acquisition.cleanup :as cleanup]
            [loa.acquisition.injection :as injection]
            [loa.acquisition.meta-handler :as meta-handler]
            [loa.acquisition.set-handler :as set-handler]
            [loa.acquisition.set-info-handler :as set-info-handler]
            [loa.persistance.card-xml :as card-xml]
            [loa.persistance.meta-xml :as meta-xml]
            [loa.persistance.report :as report]
            [loa.persistance.setinfo-xml :as setinfo-xml]
            [loa.persistance.text :as text]
            [loa.program.config :as config]
            [loa.util
             [log :as log]
             [util :as util]
             [zip :as zip]]
            [loa.verification [text :as text-check]])
  (:use clojure.pprint)
  (:import (java.util.logging Level
                              Logger)
           java.io.File
           java.text.SimpleDateFormat))


;;-------------------------------------------------
;;
;;  Other
;;
(comment
  (defn- validate-cards
    "Validate the given cards."
    [cards]
    (doseq [card cards]
      (when-let [res (card-handler/validate card)]
        (pprint card)
        (println "Fault:")
        (doseq [line res]
          (println "  *" line))
        (println)))))

(defn- realize
  [& seqs]
  (doseq [s seqs]
    (dorun s)))

;;-------------------------------------------------
;;
;;  To disk
;;
(defn- write-xml
  [cards meta setinfo]
  (let [cards (sort #(compare (:name %1) (:name %2)) cards)]
    (binding [prxml/*prxml-indent* 1]
      (log/debug "Writing cards as XML.")
      (card-xml/write-card-data-to
       cards
       (config/construct-file :xml "cards.xml"))
      (log/debug "Writing meta as XML.")
      (meta-xml/write-meta-data-to
       meta
       setinfo
       (config/construct-file :xml "meta.xml"))
      (log/debug "Writing set-info as XML.")
      (setinfo-xml/write-setinfo-to
       setinfo
       (config/construct-file :xml "setinfo.xml")))))

(defn- write-txt
  [cards meta setinfo]
  (log/debug "Writing cards as text.")
  (text/write-text-to cards meta setinfo
                      (config/construct-file :text "mtg-data.txt"))
  (comment
    (let [rem #{"Unhinged" "Unglued"}
          cards (remove (fn [card]
                          (let [set-rarity (:set-rarity card)]
                            (and (= 1 (count set-rarity))
                                 (rem (ffirst set-rarity)))))
                        cards)]
      (text/write-text-to cards meta setinfo
                          (config/construct-file :text "mtg-data.txt")))))

(defn- to-disk
  [cards meta setinfo]
  (util/write-data (config/construct-file :tmp "cards.dat") cards)
  (util/write-data (config/construct-file :tmp "meta.dat") meta)
  (util/write-data (config/construct-file :tmp "set-info.dat") setinfo))

(defn- from-disk
  "Returns data; [cards meta setinfo]."
  []
  [(util/read-data (config/construct-file :tmp "cards.dat"))
   (util/read-data (config/construct-file :tmp "meta.dat"))
   (util/read-data (config/construct-file :tmp "set-info.dat"))])


;;-------------------------------------------------
;;
;;  Helpers
;;
(defn- get-card-details
  [meta-item]
  (card-details-handler/get-card-details
   (get-in config/*config* [:url :card-details])
   (:gatherer-id meta-item)))

(defn- get-data
  [get-fn parse-url & names]
  (let [sets (set-handler/get-set-names
              (get-in config/*config* [:url :main]))
        selected (if (empty? names)
                   sets
                   (filter (fn [setname]
                             (some #(.contains setname %) names))
                           sets))
        cfg config/*config*]
    (apply concat
           (pmap (fn [setname]
                   (binding [config/*config* cfg]
                     (get-fn parse-url setname)))
                 selected))))

(defn- get-cards
  [& names]
  (-> (apply get-data
             set-handler/get-cards
             (get-in config/*config* [:url :set])
             names)
      set
      cleanup/filter-cards))

(defn- get-meta
  [& names]
  (let [coll (apply get-data
                    meta-handler/get-meta
                    (get-in config/*config* [:url :checklist])
                    names)]
    (doall (pmap merge (map get-card-details coll) coll))))

(defn- filter-name
  [cards & names]
  (if-not (empty? names)
    (filter (fn [card]
              (when (:name card)
                (some #(.contains (:name card) %) names)))
            cards)
    cards))

(defn- filter-type
  [cards & types]
  (if-not (empty? types)
    (filter (fn [card]
              (some (fn [type]
                      (when (some #(.contains type %) types)
                        true))
                    (:types card)))
            cards)
    cards))

(defn- get-set-info
  []
  (set-info-handler/get-set-info
   (config/construct-file :indata "set-info.csv")))



(comment ;; finding not fixed 
  (use 'clojure.contrib.lazy-xml)
  (defn- get-old-names
    []
    (let [files (.listFiles (File. "c:\\karl\\dev\\eclipse_workspace\\magic-data\\indata\\cardinjection\\"))]
      (->> (mapcat parse-seq files)
           (reduce (fn [[coll name] item]
                     (if name
                       [(conj coll (:str item)) false]
                       (if (and (= :name (:name item))
                                (= :start-element (:type item)))
                         [coll true]
                         [coll name])))
                   [nil false])
           first
           (map (memfn trim))
           sort)))
  (binding [config/*config* (config/create-config ".")]
    (let [[cards meta setinfo] (from-disk)
          old (set (get-old-names))]
      (->> (map :name cards)
           (reduce disj old)
           sort
           pprint))))

;;-------------------------------------------------
;;
;;  High-level
;;
(defmacro with-default-config
  [& expr]
  `(binding [config/*config* (config/create-config ".")]
     (do ~@expr)))

(defn acquire-and-write-to-disk
  "Returns (cards, meta, setinfo)."
  []
  (let [sets [""]
        cards (apply get-cards sets)
        meta (apply get-meta sets)
        setinfo (get-set-info)]
    (realize cards meta setinfo)
    (to-disk cards meta setinfo)
    [cards meta setinfo]))

(defn write-all
  [cards meta setinfo]
  (write-xml cards meta setinfo)
  (write-txt cards meta setinfo))

(defn get-x-cards
  [sets cardnames typenames]
  (let [cards (apply get-cards sets)
        selected (apply filter-name cards cardnames)
        selected (apply filter-type selected typenames)]
    selected))

(defn create-package
  []
  (log/debug "Creating ZIP-file.")
  (zip/create
   (config/construct-file :zip
                          (format "mtg-data-%s.zip"
                                  (.format (SimpleDateFormat. "yyyy-MM-dd")
                                           (System/currentTimeMillis))))
   (concat (map #(vector (config/construct-file :xml %) (str "xml/" %))
                ["cards.xml"
                 "meta.xml"
                 "setinfo.xml"])
           [[(config/construct-file :indata "format.txt") "xml/format.txt"]]
           (map #(vector (config/construct-file :text %) (str "text/" %))
                ["mtg-data.txt"]))))

(defn- remove-unhg
  [cards meta]
  (let [only-unhg (fn [card]
                    (and (every? #{"Unhinged" "Unglued"}
                                 (map first (:set-rarity card)))
                         (not (zero? (count (:set-rarity card))))))
        cards (remove only-unhg cards)
        names (set (map :name cards))
        meta (filter #(names (:name %)) meta)]
    [cards meta]))

(defn verify
  []
  (log/debug "Verifying data.")
  (text-check/verify (config/construct-file :text "mtg-data.txt")))

(defn full-run
  []
  (let [[cards meta setinfo] (acquire-and-write-to-disk)
        cards (cleanup/process
               (concat cards
                       (injection/get-cards)))
        [cards meta] (remove-unhg cards meta)]
    (write-all cards meta setinfo)
    (create-package)
    (verify)))

(defn full-run-from-disk
  []
  (let [[cards meta setinfo] (from-disk)
        cards (cleanup/process cards)
        cards (concat cards (injection/get-cards))
        [cards meta] (remove-unhg cards meta)]
    (write-all cards meta setinfo)
    (create-package)
    (verify)))

;;-------------------------------------------------
;;
;;  Main
;;
(defn -main
  [& _]
  (try
    (with-default-config
      (println "---[ FULL RUN ]-------------------------------------")
      (config/init-paths)
      (full-run))
    (finally
     (shutdown-agents))))

(defn -debug
  [& _]
  (try
    (with-default-config
      (println "---[ DEBUG ]----------------------------------------")
      (config/init-paths)
      (let [cards (get-x-cards ["Innistrad"]
                               ["Scree" "Stalking Vampire"]
                               [])
            cards (cleanup/process cards)]
        (pprint cards)
        (println "********************")
        (text/write-text cards nil nil)
        (println "********************")
        (comment
          (pprint
           (map card-xml/card-to-xml cards))
          (println "********************"))
        (println "Count:" (count cards))
        )
      (pprint (filter #(or (.contains (:name %) "Scree")
                           (.contains (:name %) "Stalking"))
                      (get-meta "Innistrad")))
      ;;(full-run-from-disk)
      )
    (finally
     (shutdown-agents))))
