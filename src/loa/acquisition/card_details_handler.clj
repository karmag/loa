
(ns loa.acquisition.card-details-handler
  (:require (loa.util (log :as log)
                      (util :as util))))

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
       (util/xml-seq)
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

;;-------------------------------------------------
;;
;;  Interface
;;
(defn get-card-details
  [card-details-url gatherer-id]
  ;;(log/debug (str "Getting card-details for id [" gatherer-id "]"))
  (-> (format card-details-url gatherer-id)
      util/get-url-data
      (process-data gatherer-id)))
