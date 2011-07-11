
(ns loa.persistance.card-xml
  (:require [clojure.contrib
             [prxml :as prxml]
             [io :as io]]
            [loa.util.util :as util]))

(declare card-to-xml)

(def super-type #{"Basic" "Legendary" "Ongoing" "Snow" "World"})
(def card-type #{"Artifact" "Creature" "Enchantment" "Instant" "Land" "Plane"
                 "Planeswalker" "Scheme" "Sorcery" "Tribal" "Vanguard"})

(def keyword-ability #{"Absorb" "Affinity" "Amplify"
                       "Annihilator" "Aura Swap" "Banding"
                       "Battle Cry" "Bloodthirst" "Bushido"
                       "Buyback" "Cascade" "Champion"
                       "Changeling" "Conspire" "Convoke"
                       "Cumulative Upkeep" "Cycling" "Deathtouch"
                       "Defender" "Delve" "Devour"
                       "Double Strike" "Dredge" "Echo"
                       "Enchant" "Entwine" "Epic"
                       "Equip" "Evoke" "Exalted"
                       "Fading" "Fear" "First Strike"
                       "Flanking" "Flash" "Flashback"
                       "Flying" "Forecast" "Fortify"
                       "Frenzy" "Graft" "Gravestorm"
                       "Haste" "Haunt" "Hexproof"
                       "Hideaway" "Horsemanship" "Infect"
                       "Intimidate" "Kicker" "Landwalk"
                       "Level Up" "Lifelink" "Living Weapon"
                       "Madness" "Modular" "Morph"
                       "Ninjutsu" "Offering" "Persist"
                       "Phasing" "Poisonous" "Protection"
                       "Provoke" "Prowl" "Rampage"
                       "Reach" "Rebound" "Recover"
                       "Reinforce" "Replicate" "Retrace"
                       "Ripple" "Shadow" "Shroud"
                       "Soulshift" "Splice" "Split Second"
                       "Storm" "Sunburst" "Suspend"
                       "Totem Armor" "Trample" "Transfigure"
                       "Transmute" "Unearth" "Vanishing"
                       "Vigilance" "Wither"})

(def keyword-regex
  (re-pattern
   (->> keyword-ability
        (interpose "|")
        (apply str)
        (#(format "(?i)(%s)( [\\dXYZ]| from .*?)?" %)))))

(defn- tag
  [name & values]
  (when-not (empty? values)
    (apply vector name {} values)))

(defn- rule-tag
  [rule]
  [:rule
   (merge {:no (:no rule)}
          (if (:reminder rule)
            {:reminder (:reminder rule)}
            {}))
   (:text rule)])

(defn- type-tag
  [type]
  (let [meta (cond (super-type type) {:type "super"}
                   (card-type type) {:type "card"}
                   :else {:type "sub"})]
    [:type meta type]))

(defn- split-protection
  "Returns a seq of rules. If a rule starts with 'protection from' and
  contains 'and' it will be split further. Other rules are returned as
  is."
  [rule-text]
  (if-let [parts (re-matches #"(?i)protection from (.*) and from (.*)"
                             rule-text)]
    (map #(format "protection from %s" %) (rest parts))
    [rule-text]))

(defn- multi-rule
  "Returns a seq of rules or nil."
  [rule-text]
  (when rule-text
    (let [parts (map re-matches
                     (repeat keyword-regex)
                     (map (memfn trim)
                          (.split rule-text "[,;]")))]
      (when (every? identity parts)
        (mapcat split-protection
                (map first parts))))))

(defn- capitalize
  [s]
  (apply str
         (.toUpperCase (str (first s)))
         (rest s)))

(defn- transform-rules
  "Turns single lines keyword rules into multiple rules. This function
  also adds number tag to all rules."
  ([rules]
     (transform-rules rules 1 []))
  ([rules num acc]
     (if (empty? rules)
       acc
       (if-let [parts (multi-rule (:text (first rules)))]
         (recur (rest rules)
                (inc num)
                (reduce conj
                        acc
                        (->> parts
                             (map capitalize)
                             (map #(hash-map :text % :no num)))))
         (recur (rest rules)
                (inc num)
                (conj acc (assoc (first rules) :no num)))))))

(defn- get-card-tags
  "Returns a seq of tag for the card."
  [card]
  (filter identity
          [(tag :name (:name card)) ;; TODO repetition, ugly, redo
           (when (:cost card)
             (tag :cost (:cost card)))
           (when (:loyalty card)
             (tag :loyalty (:loyalty card)))
           (when-not (empty? (:types card))
             (apply tag
                    :typelist
                    (map #(type-tag %) (:types card))))
           (when (:pow card)
             (tag :pow (:pow card)))
           (when (:tgh card)
             (tag :tgh (:tgh card)))
           (when (:hand card)
             (tag :hand (:hand card)))
           (when (:life card)
             (tag :life (:life card)))
           (when-not (empty? (:rules card))
             (apply vector
                    :rulelist
                    {}
                    (map rule-tag
                         (transform-rules (:rules card)))))]))

(defn- get-multi-card
  "Returns the multi-part if any."
  [card]
  (when (:multi card)
    (map (fn [multi]
           (-> (card-to-xml multi)
               (assoc 0 :multi)
               (assoc 1 {:type (name (:multi-type multi))})))
         (:multi card))))

(defn card-to-xml
  "Transforms a card into xml-data."
  [card]
  (apply tag
         :card
         (concat (get-card-tags card)
                 (get-multi-card card))))

(defn write-card-data
  "Transforms the card-data to xml and writes it to *out*."
  [cards]
  (util/write-xml
   (vec (concat [:cardlist {}] (map card-to-xml cards)))))

(defn write-card-data-to
  "Same as write-card-data but writes to a file(name) instead."
  [cards file]
  (io/with-out-writer file
    (write-card-data cards)))
