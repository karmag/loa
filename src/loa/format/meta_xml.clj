
(ns loa.format.meta-xml
  (:require (loa.util (data :as data_))))

(defn- prepare-set-data
  [set-list]
  (reduce #(assoc %1 (:name %2) %2) nil set-list))

(defn- tag
  [name value]
  (when value
    [name {} value]))

(defn- meta-to-instance
  [meta set-data]
  [:instance {}
   (cons (tag :set (get-in set-data [(:set meta) :code]))
         (map #(tag %1 (%1 meta)) [:rarity :number :artist :flavor-text]))])

(defn- to-instance
  [card set-data]
  [:card {}
   (->> (:meta card)
        (map #(update-in % [:rarity] (comp :code data_/rarity-mapping)))
        (sort-by :set)
        (map #(meta-to-instance % set-data))
        (cons [:name (:name card)]))])

;;--------------------------------------------------
;;
;;  Interface
;;
(defn data
  [cards set-list]
  (let [set-data (prepare-set-data set-list)]
    [:metalist {}
     (map #(to-instance % set-data) cards)]))
