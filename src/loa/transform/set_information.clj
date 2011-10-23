
(ns loa.transform.set-information)

(defn- reform-set-rarity
  [set-rarities]
  (map (fn [[set rarity]]
         {:set set
          :rarity rarity})
       set-rarities))

(defn- merge-cards
  [map card]
  (if (get map (:name card))
    map
    (let [meta (reform-set-rarity (:set-rarity card))
          card (-> card
                   (dissoc :set-rarity)
                   (assoc :meta meta))]
      (assoc map (:name card) card))))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [cards]
  (->> cards
       (reduce merge-cards nil)
       vals))
