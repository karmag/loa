
(ns loa.persistance.setinfo-xml
  (:require [loa.util.util :as util]
            [clojure.contrib.io :as io]))

(defn- cardcount-to-xml
  [setdata]
  (let [data
        (->> [:cards :common :uncommon :rare :mythic-rare :basic-land]
             (filter #(and (% setdata)
                           (not= 0 (.length (% setdata)))))
             (map #(vector (if (= :cards %)
                             :total
                             %)
                           {}
                           (% setdata))))]
    (when-not (empty? data)
      (vec (concat [:cards {}] data)))))

(defn- set-to-xml
  [setdata]
  (let [data
        (->> [:name :block :code :release-date]
             (filter #(and (% setdata)
                           (not= 0 (.length (% setdata)))))
             (map #(vector % {} (% setdata))))]
    (vec (concat [:set {}]
                 data
                 [(cardcount-to-xml setdata)]))))

(defn- set-to-xml
  [setdata]
  (->> (keys setdata)
       (filter #(and (% setdata)
                     (not= 0 (.length (% setdata)))))
       (map #(vector % {} (% setdata)))
       (concat [:set {}])
       vec
       ))

(defn write-setinfo
  "Writes the setinfo as xml to *out*."
  [setinfo]
  (util/write-xml
   (vec (concat [:setlist {}]
                (map set-to-xml (sort #(compare (:name %1)
                                                (:name %2))
                                      setinfo))))))

(defn write-setinfo-to
  "Writes the setinfo as xml to the given file(name)."
  [setinfo file]
  (io/with-out-writer file
    (write-setinfo setinfo)))
