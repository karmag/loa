
(ns loa.persistance.meta-xml
  (:require [clojure.contrib.io :as io]
            [loa.util.util :as util]))

(defn- tag
  [name & values]
  (when-not (empty? values)
    (apply vector name {} values)))

(defn- meta-item-to-xml
  [item]
  (apply tag
         :instance
         (map #(tag %1 (%1 item))
              (filter #(% item)
                      [:set :rarity :number :artist]))))

(defn- meta-seq-to-xml
  [item-seq]
  (let [name (-> item-seq first :name)]
    (tag :card
         (tag :name name)
         (map meta-item-to-xml item-seq))))

(defn- meta-to-xml
  [meta setinfo]
  (let [name->code (reduce #(assoc %1 (:name %2) (:code %2))
                           {}
                           setinfo)
        data (->> (map #(update-in % [:set] name->code) meta)
                  (reduce #(update-in %1 [(:name %2)] conj %2)
                          nil)
                  (map second)
                  (sort #(compare (-> %1 first :name)
                                  (-> %2 first :name))))]
    (map meta-seq-to-xml data)))

(defn write-meta-data
  "Transforms the meta-data to xml and writes it to *out*."
  [meta setinfo]
  (util/write-xml
   (vec (concat [:metalist {}] (meta-to-xml meta setinfo)))))

(defn write-meta-data-to
  "Transforms the meta-data to xml and writes it to file(name)."
  [meta setinfo file]
  (io/with-out-writer file
    (write-meta-data meta setinfo)))
