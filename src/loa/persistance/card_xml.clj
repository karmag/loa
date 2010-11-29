
(ns loa.persistance.card-xml
  (:require [clojure.contrib
             [prxml :as prxml]
             [io :as io]]
            [loa.util.util :as util]))

(declare card-to-xml)

(defn- tag
  [name & values]
  (when-not (empty? values)
    (apply vector name {} values)))

(defn- rule-tag
  [rule]
  [:rule
   (if (:reminder rule)
     {:reminder (:reminder rule)}
     {})
   (:text rule)])

(defn- get-card-tags
  "Returns a seq of tag for the card."
  [card]
  (filter identity
          [(tag :name (:name card)) ;; TODO repetition, ugly, redo
           (when (:cost card)
             (tag :cost (:cost card)))
           (when (:loyalty card)
             (tag :loyalty (:loyalty card)))
           (when-not (empty? (:types card))
             (apply tag
                    :typelist
                    (map #(tag :type %) (:types card))))
           (when (:pow card)
             (tag :pow (:pow card)))
           (when (:tgh card)
             (tag :tgh (:tgh card)))
           (when (:hand card)
             (tag :hand (:hand card)))
           (when (:life card)
             (tag :life (:life card)))
           (when-not (empty? (:rules card))
             (apply tag
                    :rulelist
                    (map rule-tag (:rules card))))]))

(defn- get-multi-card
  "Returns the multi-part if any."
  [card]
  (when (:multi card)
    (map (fn [multi]
           (-> (card-to-xml multi)
               (assoc 0 :multi)
               (assoc 1 {:type (name (:multi-type multi))})))
         (:multi card))))

(defn- card-to-xml
  "Transforms a card into xml-data."
  [card]
  (apply tag
         :card
         (concat (get-card-tags card)
                 (get-multi-card card))))

(defn write-card-data
  "Transforms the card-data to xml and writes it to *out*."
  [cards]
  (util/write-xml
   (vec (concat [:cardlist {}] (map card-to-xml cards)))))

(defn write-card-data-to
  "Same as write-card-data but writes to a file(name) instead."
  [cards file]
  (io/with-out-writer file
    (write-card-data cards)))
