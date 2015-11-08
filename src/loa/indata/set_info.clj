(ns loa.indata.set-info)

(defn- parse-csv
  [header lines]
  (let [header (-> header
                   (.split "#"))
        header (->> header
                    (map (memfn toLowerCase))
                    (map #(.replace % " " "-"))
                    (map keyword))]
    (map #(apply hash-map (interleave header (.split % "#"))) lines)))

(defn- create-set-info
  [lines]
  (parse-csv (first lines)
             (remove empty? (rest lines))))

(defn get-set-info [csv-string]
  (reduce #(assoc %1 (:name %2) %2)
          nil
          (create-set-info (.split csv-string "\n"))))
