
(ns loa.acquisition.set-info-handler
  (:require [clojure.contrib.io :as io]))

(defn- parse-csv
  [header lines]
  (let [header (-> header
                   (.split ","))
        header (->> header
                    (map (memfn toLowerCase))
                    (map #(.replace % " " "-"))
                    (map keyword))]
    (map #(apply hash-map (interleave header (.split % ","))) lines)))

(defn- create-set-info
  [lines]
  (let [csv (parse-csv (first lines) (rest lines))]
    (remove empty? csv)))

(defn get-set-info
  [file]
  (let [csv-data (map (memfn trim)
                      (.split (io/slurp* file)
                              (str \newline)))]
    (create-set-info csv-data)))
