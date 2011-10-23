
(ns loa.transform.flip-cards
  (:require (loa.transform (raw-card :as raw-card_))))

(defn- make------card
  [rules]
  (let [[name types pt & rules] rules]
    (raw-card_/process (merge nil
                              [:name [name]]
                              [:types [types]]
                              [:pt [(format "(%s)" pt)]]
                              (apply vector :rules [rules])))))

(defn- fix------flip-cards
  "Fixes flipcards that have all their info in one card instead of
  spread out over multiple cards."
  [cardmap]
  (reduce (fn [cardmap card]
            (if (some #(= "----" (:text %)) (:rules card))
              (let [regular (take-while #(not= "----" (:text %)) (:rules card))
                    flip (nthnext (:rules card) (inc (count regular)))
                    flip (make------card (map :text flip))]
                (-> cardmap
                    (assoc-in [(:name card) :rules] regular)
                    (update-in [(:name card) :multi] conj
                               (assoc flip :multi-type :flip))))
              cardmap))
          cardmap
          (vals cardmap)))

(defn- to-map
  [cards]
  (reduce #(assoc %1 (:name %2) %2)
          nil
          cards))

(defn- from-map
  [cardmap]
  (vals cardmap))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [cards]
  (->> cards
       to-map
       fix------flip-cards
       from-map))
