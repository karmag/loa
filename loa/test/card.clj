
(ns loa.test.card)

;;-------------------------------------------------
;;
;;  Validation
;;
(defn- validate-name
  [card]
  (when (or (not (:name card))
            (= 0 (.length (:name card))))
    "No name."))

(defn- validate-set-rarity
  [card]
  (if-not (:set-rarity card)
    "Missing set/rarity."
    (reduce (fn [errors item]
              (let [error
                    (cond (not= 2 (count item))
                          (str "Not a set/rarity pair '" item "'")
                          (not (#{:common :uncommon :rare :mythic :land :special} (second item)))
                          (str "Rarity not known '" (second item) "'")
                          (not (string? (first item)))
                          (str "Set name is not string '" (first item) "'"))]
                (if error
                  (conj errors error)
                  errors)))
            nil
            (:set-rarity card))))

(defn- validate-no-rules
  [card]
  (let [creature? (some #(= % "Creature") (:type card))
        land? (some #(= % "Land") (:type card))
        rule-count (count (:rules card))]
    (when (and (and (not creature?)
                    (not land?))
               (= 0 rule-count))
      ["Not a creature or land and has no rules"])))

(defn- validate-loyalty
  [card]
  (let [planeswalker? (some #(= % "Planeswalker") (:type card))
        loyalty? (:loyalty card)]
    (when (or (and planeswalker? (not loyalty?))
              (and (not planeswalker?) loyalty?))
      ["Planeswalker/loyalty mismatch"])))

(defn validate
  [card]
  (let [validators [validate-name
                    validate-set-rarity
                    validate-no-rules
                    validate-loyalty]
        result (reduce (fn [result error]
                         (if (string? error)
                           (conj result error)
                           (reduce conj result error)))
                       []
                       (map #(% card) validators))]
    (when-not (empty? result)
      result)))
