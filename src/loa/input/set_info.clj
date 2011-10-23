
(ns loa.input.set-info)

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

(defn merge-data
  [map set]
  (if (get map (:name set))
    (update-in map [(:name set)] merge set)
    (assoc map (:name set) set)))

(defn combine
  "Returns a new set-list merged with the csv data."
  [set-list csv-string]
  (let [set-list (map #(assoc %1 :download true) set-list)
        csv-list (create-set-info (.split csv-string
                                          (str \newline)))]
    (vals
     (reduce merge-data
             nil
             (concat set-list csv-list)))))
