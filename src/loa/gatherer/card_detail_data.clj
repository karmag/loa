
(ns loa.gatherer.card-detail-data
  (:require (loa.util (xml :as xml_))))

;;-------------------------------------------------
;;
;;  Helper
;;
(defn- get-ids
  [data]
  (->> data
       (re-seq #"Details.aspx\?multiverseid=(\d+).*?img title=\"(.*?)\"")
       (map #(let [[_ id set-name] %]
               {:id (Integer/parseInt id)
                :set (second (re-matches #"(.*) \(.*?\)" set-name))}))))

(defn- get-name
  [data]
  (->> (re-find #"(?s)<div class=\"contentTitle\">.*?</div>" data)
       (#(.replaceAll % "&" "&amp;"))
       (xml_/xml-seq)
       (filter #(= :characters (:type %)))
       (map :str)
       (apply str)))

(defn- get-flavor
  [data]
  (when (.contains data "Flavor Text:")
    (let [name (get-name data)
          primary (->> data
                       (re-find (re-pattern (str "(?s)(?m)Card Name:.*?(" name ".*)")))
                       second)]
      (when (and primary
                 (.contains primary "Flavor Text:"))
        (->> primary
             (re-find #"(?s)(?m)Flavor Text:.*?(?:P/T:|Expansion:|Watermark:)")
             (re-seq #"class=.cardtextbox.>(.*?)</div>")
             (map second)
             (map #(-> %
                       (.replaceAll "<i>" "")
                       (.replaceAll "</i>" "")))
             (interpose \newline)
             (reduce str))))))

(defn- process-data
  [data id]
  (let [flavor (get-flavor data)
        others (get-ids data)]
    {:flavor-text flavor
     :other (remove #{id} others)}))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn get-details
  [page id]
  (process-data page id))
