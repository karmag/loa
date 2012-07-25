
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
  [meta multi-meta set-data]
  [:instance {}
   (concat [(tag :set (get-in set-data [(:set meta) :code]))]
           (map #(tag %1 (%1 meta)) [:rarity :number :artist :flavor-text])
           (when multi-meta
             [[:multi {}
               (map #(tag %1 (%1 multi-meta)) [:artist :flavor-text])]]))])

(defn- get-complement-meta
  [card meta]
  (let [refine (fn [meta]
                 (map #(% meta) [:set :number]))]
    (->> (:multi card)
         first
         :meta
         (filter (fn [other]
                   (= (refine meta)
                      (refine other))))
         first)))

(defn- to-instance
  [card set-data]
  [:card {}
   (concat
    (->> (:meta card)
         (map #(update-in % [:rarity] (comp :code data_/rarity-mapping)))
         (sort-by :set)
         (map #(meta-to-instance %
                                 (get-complement-meta card %)
                                 set-data))
         (cons [:name (:name card)])))])

;;--------------------------------------------------
;;
;;  Interface
;;
(defn data
  [cards set-list]
  (let [set-data (prepare-set-data set-list)]
    [:metalist {}
     (map #(to-instance % set-data) cards)]))
