
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

(defn- get-flavor
  [data]
  (when (.contains data "Flavor Text:")
    (->> data
         (re-find #"(?s)(?m)Flavor Text:.*?(?:P/T:|Expansion:|Watermark:)")
         (re-seq #"class=.cardtextbox.>(.*?)</div>")
         (map second)
         (map #(-> %
                   (.replaceAll "<i>" "")
                   (.replaceAll "</i>" "")))
         (interpose \newline)
         (reduce str))))

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
