
(ns loa.gatherer.card-data
  (:require (loa.util (util :as util_)
                      (xml :as xml_))))

(def #^{:private true}
  labels
  {"Name:" :name
   "Cost:" :cost
   "Color:" :color
   "Type:" :types
   "Pow/Tgh:" :pt
   "Rules Text:" :rules
   "Set/Rarity:" :set-rarity
   "Loyalty:" :loyalty
   "Hand/Life:" :hand-life})

(defn- parse-raw-set
  "Returns card-data as sequence of maps. A map consist of keyword ->
  sequence."
  [page]
  (let [text (-> #"(?m)(?s).*<div class=\"textspoiler\">(.*?)</div>.*"
                 (re-matches page)
                 second)
        data (->> (xml_/xml-seq text)
                  (filter #(= (:type %) :characters))
                  (map (comp (memfn trim) :str)))
        data (-> data
                 (util_/lazy-split (set (keys labels)))
                 (util_/lazy-split #(= (first %) "Name:")))
        part-seq (map #(map (fn [[k & v]]
                              (apply vector (labels k) v))
                            %)
                      data)]
    (map (fn [parts]
           (reduce (fn [m [k & v]]
                     (assoc m k v))
                   nil
                   parts))
         part-seq)))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn get-cards
  "Returns the cards available on the page."
  [page]
  (parse-raw-set page))
