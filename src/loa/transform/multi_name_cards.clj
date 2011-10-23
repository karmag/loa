
(ns loa.transform.multi-name-cards)

(defn- rectify-name
    "Change the name of a card."
    [cardmap oldname newname]
    (let [card (assoc (get cardmap oldname)
                 :name newname)]
      (-> cardmap
          (dissoc oldname)
          (assoc newname card))))

(defn- process-multi-named-card
  "Adds a partial card onto an actual card, or simply renames the
  master card."
  [cardmap master-name slave-old slave-new]
  (if (get cardmap master-name)
    (let [master (get cardmap master-name)
          slave (assoc (get cardmap slave-old)
                  :name slave-new
                  :multi-type :flip)
          slave (dissoc slave :cost)]
      (-> cardmap
          (update-in [master-name :multi] conj slave)
          (dissoc slave-old)))
    (rectify-name cardmap slave-old slave-new)))

(defn- fix-multi-named-cards
  "Reforms card with 'name (other name)' into flip cards, or just
  fixes the name."
  [cardmap]
  (let [doubles (filter #(and (.contains % "(")
                              (not (.contains % "//")))
                        (keys cardmap))]
    (reduce (fn [cards name]
              (let [[_ master slave]
                    (re-matches #"(.+?)\s*\((.+?)\)" name)]
                (process-multi-named-card cards master name slave)))
            cardmap
            doubles)))

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
       fix-multi-named-cards
       from-map))
