
(ns loa.program.command
  "High-level functions for running loa."
  (:require (loa.gatherer (card-data :as card-data_)
                          (card-detail-data :as card-detail-data_)
                          (meta-data :as meta-data_)
                          (set-data :as set-data_))
            (loa.input (set-info :as set-info_))
            (loa.logic (card-meta-merge :as card-meta-merge_)
                       (card-details-merge :as card-details-merge_))
            (loa.program (config :as config_)
                         (file-writer :as file-writer_))
            (loa.transform (transform :as transform_))
            (loa.util (util :as util_)
                      (zip :as zip_)))
  (:import java.text.SimpleDateFormat))

;;--------------------------------------------------
;;
;;  Setup
;;
(defn make-config
  "Construct a configuration based on the root path."
  [rootpath]
  (config_/create-config rootpath))

(defn setup-paths
  "Creates all required paths if they do not exist."
  [config]
  (doseq [f (vals (:path config))]
    (.mkdirs f)))

;;--------------------------------------------------
;;
;;  Data fetching
;;
(defn get-sets
  "Returns all set objects."
  [config]
  (let [page (util_/get-url-data config
                                 (config_/get-url config :main))
        csv-file (config_/get-file config :indata "set-info.csv")]
    (->> page
         set-data_/parse-names
         (remove #{"Unglued" "Unhinged"})
         (map (partial hash-map :name))
         (#(set-info_/combine % (slurp csv-file))))))

(defn get-cards
  "Returns a seq of cards."
  [config set]
  (when (:download set)
    (let [url (config_/get-url config :set (:name set))
          page (util_/get-url-data config url)]
      (card-data_/get-cards page))))

(defn get-meta
  "Returns a seq of card meta data."
  [config set]
  (when (:download set)
    (let [url (config_/get-url config :checklist (:name set))
          page (util_/get-url-data config url)]
      (meta-data_/get-meta page))))

(defn get-card-details
  "Returns a sequence of card-details for the card."
  [config card]
  (map (fn [id]
         (let [url (config_/get-url config :card-details id)
               page (util_/get-url-data config url)]
           (card-detail-data_/get-details page id)))
       (map :gatherer-id
            (concat (:meta card)
                    (-> card :multi first :meta)))))

;;--------------------------------------------------
;;
;;  Transformation / cleanup
;;
(defn transform
  "Applies a transformation to a card seq."
  [type cards]
  (if (= type ::all)
    (transform_/process-all cards)
    (transform_/process type cards)))

(defn add-metadata
  "Adds the information in the meta-coll to the card-coll."
  [card-coll meta-coll]
  (card-meta-merge_/process card-coll meta-coll))

(defn add-card-details
  [card detail-coll]
  (card-details-merge_/process card detail-coll))

;;--------------------------------------------------
;;
;;  Output
;;
(defn write-cards
  [config cards]
  (println "Write cards")
  (with-open [writer (file-writer_/make-writer config :xml "cards.xml")]
    (file-writer_/card-list writer cards)))

(defn write-meta
  [config cards sets]
  (println "Write meta")
  (with-open [writer (file-writer_/make-writer config :xml "meta.xml")]
    (file-writer_/meta-list writer cards sets)))

(defn write-setinfo
  [config set-coll]
  (println "Write set-info")
  (with-open [writer (file-writer_/make-writer config :xml "setinfo.xml")]
    (file-writer_/setinfo writer set-coll)))

(defn write-cards-text
  [config card-coll set-coll]
  (println "Write cards (text)")
  (let [writer (file-writer_/make-writer config :text "mtg-data.txt")]
    (file-writer_/card-list-text writer card-coll set-coll)))

;;--------------------------------------------------
;;
;;  Packaging
;;
(defn create-package
  [config]
  (println "Creating zip")
  (let [zipname (format "mtg-data-%s.zip"
                        (.format (SimpleDateFormat. "yyyy-MM-dd")
                                 (System/currentTimeMillis)))]
    (zip_/create
     (config_/get-file config :zip zipname)
     (concat (map #(vector (config_/get-file config :xml %) (str "xml/" %))
                  ["cards.xml"
                   "meta.xml"
                   "setinfo.xml"])
             [[(config_/get-file config :indata "format.txt") "xml/format.txt"]]
             (map #(vector (config_/get-file config :text %) (str "text/" %))
                  ["mtg-data.txt"])))))
