
(ns loa.format.setinfo-xml)

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
                           (not= 0 (.length (str (% setdata))))))
             (map #(vector % {} (% setdata))))]
    (vec (concat [:set {}]
                 data
                 [(cardcount-to-xml setdata)]))))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn data
  [set-coll]
  [:setlist {} (map set-to-xml set-coll)])
