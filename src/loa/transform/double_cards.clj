
(ns loa.transform.double-cards)

(defn- process-double-card
  [cardmap master-old master-new slave-old slave-new]
  (let [slave (-> (get cardmap slave-old)
                  (assoc :name slave-new
                         :multi-type :double)
                  (dissoc :set-rarity))
        master (-> (get cardmap master-old)
                   (assoc :name master-new)
                   (update-in [:multi] conj slave))]
    (-> cardmap
        (assoc master-new master)
        (dissoc master-old)
        (dissoc slave-old))))

(defn- fix-double-cards
  "Fixes double-cards (with // in their name)."
  [cardmap]
  (let [doubles (filter #(.contains % "//") (keys cardmap))
        fmt-string  "%s // %s (%s)"]
    (reduce (fn [cardmap name]
              (if (get cardmap name)
                (let [[_ master slave]
                      (map (memfn trim)
                           (re-matches #"(.*)//(.*)\(.*\)" name))]
                  (process-double-card cardmap
                                       (format fmt-string master slave master)
                                       master
                                       (format fmt-string master slave slave)
                                       slave))
                cardmap))
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
       fix-double-cards
       from-map))
