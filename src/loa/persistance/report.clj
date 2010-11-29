
(ns loa.persistance.report)

(defn- print-header
  [text]
  (let [width 60
        line (format "o%so" (reduce str (repeat (- width 2) "-")))
        fmt (format "| %%-%ds |" (- width 4))]
    (println line)
    (println (format fmt text))
    (println line)))

(defn- print-kvs
  [kvseq]
  (let [width (+ 3 (reduce max (map #(-> % first count) kvseq)))
        fmt " %s %s"]
    (doseq [[k v] kvseq]
      (let [k (format "%s%s"
                      (str k " ")
                      (reduce str (repeat (- width (count k)) ".")))]
        (println (format fmt k (str v)))))))

(defn- write-set-info
  [{:keys [cards meta setinfo]}]
  (let []))

(defn- write-overview
  [{:keys [cards meta setinfo]}]
  (print-header "Total")
  (print-kvs
   [["Total cards" (count cards)]
    ["Total meta" (count meta)]
    ["Set-info" (count setinfo)]]))

(defn write-report
  "Writes a report to *out*."
  [cards meta setinfo]
  (let [data {:cards cards :meta meta :setinfo setinfo}]
    (write-overview data)
    (write-set-info data)))
