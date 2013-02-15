(ns loa.validation.card-validation)

;;--------------------------------------------------
;; helpers and internal

(def ^:private validators (atom []))

(defmacro defvalidator [msg body]
  `(swap! ~'validators conj [(fn [~'card] ~body) ~msg]))

(defn- creature? [card] (some #{"Creature"} (:typelist card)))
(defn- vanguard? [card] (some #{"Vanguard"} (:typelist card)))

(defn- str? [value]
  (and (string? value) (not-empty value)))

;;--------------------------------------------------
;; general validation

(defvalidator "Cards must have a name."
  (str? (:name card)))

(defvalidator "Cards must have a non-empty type-list."
  (let [types (:typelist card)]
    (and (not-empty types)
         (every? str? types))))

(defvalidator "Cards must belong to at least one set."
  (not-empty (:sets card)))

(defvalidator "Cards must have rules or be a creature."
  (if (creature? card)
    true
    (not-empty (:rulelist card))))

;;--------------------------------------------------
;; creature validation

(defvalidator "Creatures must have pow/tgh."
  (if (creature? card)
    (let [pow (:pow card)
          tgh (:tgh card)]
      (and pow tgh (str? pow) (str? tgh)))
    true))

;;--------------------------------------------------
;; vanguard validation

(defvalidator "Vanguard must have hand/life."
  (if (vanguard? card)
    (and (str? (:hand card))
         (str? (:life card)))
    true))

;;--------------------------------------------------
;; interface

(defn validate
  "Returns a seq of strings detailing any validation errors that were
  found."
  [card]
  (->> @validators
       (map (fn [[f msg]] (when-not (f card) msg)))
       (remove nil?)
       doall))
