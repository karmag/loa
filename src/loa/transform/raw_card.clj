
(ns loa.transform.raw-card
  "This transformer handles fixes up cards that come directly from the
  card-data ns.")

;;-------------------------------------------------
;;
;;  Card helpers
;;
(defn- parse-types
  [types]
  (map (memfn trim)
       (.split types
               (str "[ " (char 8212) "]+"))))

(defn- get-set-rarity
  [s]
  (let [r-coll ["Common" "Uncommon" "Mythic Rare" "Rare" "Land" "Special"]
        r-map (apply hash-map (interleave r-coll
                                          [:common :uncommon
                                           :mythic :rare
                                           :land :special]))
        rarity (some #(when (.endsWith s %) %) r-coll)]
    [(.trim (.substring s 0 (- (.length s) (count rarity))))
     (r-map rarity)]))

;;--------------------------------------------------
;;
;;  Parse-value
;;
(defmulti #^{:private true}
  parse-value (fn [type & args]
                (if-not (empty? args)
                  type
                  :no-value)))

(defmethod parse-value :no-value
  [& _])

(defmethod parse-value :default
  [type & coll]
  (when-not (empty? coll)
    {type (apply str coll)}))

(defmethod parse-value :cost
  [_ & [cost]]
  (when cost
    {:cost cost}))

(defmethod parse-value :color
  [_ & [color]]
  {:color color})

(defmethod parse-value :loyalty
  [_ & [loyalty]]
  {:loyalty (apply str (remove #{\( \)} loyalty))})

(defmethod parse-value :types
  [_ & [types]]
  (let [types (parse-types types)]
    {:types (if (= "Plane" (first types))
              [(-> types first)
               (apply str (interpose " " (rest types)))]
              types)}))

(defmethod parse-value :pt
  [_ & [pt]]
  (when pt
    (let [[_ p t] (re-matches #"\((.*)/(.*)\)" pt)]
      {:pow p :tgh t})))

(defmethod parse-value :rules
  [_ & rules]
  (let [rules (filter identity rules)]
    (when-not (empty? rules)
      {:rules (map (partial hash-map :text) rules)})))

(defmethod parse-value :set-rarity
  [_ setrare]
  {:set-rarity
   (map get-set-rarity (map (memfn trim) (.split setrare ",")))})

(defmethod parse-value :hand-life
  [_ text]
  (let [[_ hand life] (re-matches #".*([+-]\d+).*([+-]\d+).*" text)]
    {:hand hand :life life}))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [card]
  (->> card
       (map (fn [[k v]]
              (apply parse-value k v)))
       (reduce merge nil)))
