
(ns loa.logic.card-details-merge)

(defn- merge-details
  [curr-coll more-coll]
  (map #(if (:flavor-text %2)
          (assoc %1 :flavor-text (:flavor-text %2))
          %1)
       curr-coll
       more-coll))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [card detail-list]
  (let [card (update-in card [:meta] merge-details detail-list)]
    (if (:multi card)
      (let [multi (process (first (:multi card))
                           (drop (count (:meta card)) detail-list))]
        (assoc card :multi (list multi)))
      card)))
