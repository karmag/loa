(ns loa.cleanup.card-cleanup
  "These functions handles cleanup of the data returned from the
  gatherer.card-details"
  (:require (loa.util (magic :as magic-))))

;;--------------------------------------------------
;; mana

(def ^:private mana-symbols
  "WUBRGXYZP\\d+")

(def ^:private mana-symbols-all
  "wubrgxyzpWUBRGXYZP\\d+")

(def ^:private brace-mana-lowercase-re
  #"\{[wubrgxyzp\d](?:/[wubrgxyzp\d])?\}")

(defn- uppercase-mana
  "Makes all mana symbols uppercase."
  [string]
  (reduce (fn [^String string ^String lowercase]
            (.replace string lowercase (.toUpperCase lowercase)))
          string
          (set (re-seq brace-mana-lowercase-re string))))

(defn- fix-mana
  "Reshapes mana-symbols to look like {X} or {X/Y}. Will also fix the
  tap symbol."
  [string]
  (reduce (fn [s [a b]]
            (.replaceAll s a b))
          string
          [["ocT" "{T}"]
           [(format "o([%s])" mana-symbols) "{$1}"]
           ["\\{S\\}i\\{S\\}i\\}" "{S}{S}"]
           ["\\{S\\}i\\}" "{S}"]
           [(format "\\{?\\(([%s])/([%s])\\)\\}?"
                    mana-symbols-all
                    mana-symbols-all)
            "{$1/$2}"]
           ["\\{(\\d)\\}(\\d)\\}" "{$1$2}"]]))

;;--------------------------------------------------
;; helpers

(defn- lower [^String s] (.toLowerCase s))

(def ^:private keyword-regex
  (re-pattern
   (->> magic-/keyword-ability
        (interpose "|")
        (apply str)
        (format "(?i)(%s)( [\\dWUBRGXYZ{}]+| from .*?)?"))))

(defn- fix-chaos [rule-text]
  (.replaceAll rule-text "chaos" "{C}"))

(defn- fix-type-delimiter [type-text]
  (-> type-text
      (.replaceAll "～ - " "～")
      (.replaceAll ": - " ": ")
      (.replaceAll "― - " "― ")
      (.replaceAll " - " " — ")))

(defn- reminder-split
  "Returns [rule-text, reminder-text]."
  [text]
  (let [reminders (->> (re-seq #"\((.+?)\)" text)
                       (map second))
        rule (reduce (fn [rule reminder]
                       (-> rule
                           (.replace (format "(%s)" reminder) "")
                           (.replaceAll " \\." ".")))
                     text
                     reminders)
        reminder (->> reminders (interpose " ") (apply str) .trim)]
    [(.trim rule) (when-not (empty? reminder)
                    reminder)]))

(defn- split-protection
  "Returns a seq of rules. If a rule starts with 'protection from' and
  contains 'and' it will be split further. Other rules are returned as
  is."
  [rule-text]
  (if-let [parts (or (re-matches #"(?i)protection from (.*), from (.*), and from (.*)"
                                 rule-text)
                     (re-matches #"(?i)protection from (.*) and from (.*)"
                                 rule-text))]
    (map #(format "protection from %s" %) (rest parts))
    [rule-text]))

(defn- capitalize
  [s]
  (apply str
         (.toUpperCase (str (first s)))
         (rest s)))

(defn- split-rule [text]
  (or (when-let [parts (map re-matches
                            (repeat keyword-regex)
                            (map (memfn trim)
                                 (.split text "[,;]")))]
        (when (every? identity parts)
          (mapcat split-protection
                  (map first parts))))
      (split-protection text)))

(defn- fix-rule [number text]
  (let [text (fix-chaos text)
        [text reminder] (reminder-split text)
        texts (map capitalize (split-rule text))]
    (cons {:text (first texts)
           :number number
           :reminder reminder}
          (map #(hash-map :text % :number number)
               (rest texts)))))

(defn- fix-rules [rules]
  (doall
   (mapcat fix-rule (iterate inc 1) rules)))

;;--------------------------------------------------
;; value cleanup

(defmulti ^:private cleanup-value
  (fn [card key]
    (if (get card key) key :ignore)))

(defmethod cleanup-value :ignore [card _] card)

(defmethod cleanup-value :cost            [card _] card)
(defmethod cleanup-value :gatherer-id     [card _] card)
(defmethod cleanup-value :name            [card _] card)
(defmethod cleanup-value :artist          [card _] card)
(defmethod cleanup-value :expansion       [card _] card)
(defmethod cleanup-value :multi           [card _] card)
(defmethod cleanup-value :color-indicator [card _] card)
(defmethod cleanup-value :rarity          [card _] card)
(defmethod cleanup-value :loyalty         [card _] card)
(defmethod cleanup-value :flavor          [card _] card)

(defmethod cleanup-value :number
  [card key]
  (update-in card [key] #(->> (seq %)
                              (filter (set "0123456789"))
                              (apply str))))

(defmethod cleanup-value :typelist
  [card key]
  (let [types (->> (.split (get card key) " ")
                   (remove #{"—"})
                   doall)]
    (assoc card key types)))

(defmethod cleanup-value :pt
  [card key]
  (let [[_ pow tgh] (re-matches #"(.*)/(.*)" (get card key))]
    (-> (dissoc card key)
        (assoc :pow (.trim pow))
        (assoc :tgh (.trim tgh)))))

(defmethod cleanup-value :rulelist
  [card key]
  (update-in card [key] fix-rules))

(defmethod cleanup-value :hand-life
  [card key]
  (let [[_ hand life]
        (re-matches #"\(Hand Modifier:(.*), Life Modifier: (.*)\)" (get card key))]
    (-> (dissoc card :hand-life)
        (assoc :hand (.trim hand))
        (assoc :life (.trim life)))))

(defmethod cleanup-value :sets
  [card key]
  (let [set-data
        (reduce (fn [m [id text]]
                  (let [[_ setname rarity]
                        (re-matches #"(.*)\((.*)\)" text)]
                    (assoc m id {:gatherer-id id
                                 :set-name (.trim setname)
                                 :rarity (-> rarity .trim lower keyword)})))
                nil
                (:sets card))]
    (assoc card :sets set-data)))

;;--------------------------------------------------
;; cleanup helpers

(defn- pre-cleanup [card]
  (let [id (:gatherer-id card)
        text (format "%s (%s)" (:expansion card) (:rarity card))]
    (dissoc (or (when id
                  (update-in card [:sets] assoc id text))
                card)
            :expansion :rarity :gatherer-id)))

(defn- move-meta-items [card gatherer-id]
  (-> card
      (update-in [:sets gatherer-id]
                 assoc
                 :artist (:artist card)
                 :number (:number card)
                 :flavor (:flavor card))
      (dissoc :artist :number :flavor)))

(defn- move-multi-meta [card gatherer-id]
  (if-let [meta (get-in card [:multi :sets nil])]
    (-> card
        (assoc-in [:sets gatherer-id :meta] meta)
        (update-in [:multi] dissoc :sets))
    card))

(defn- post-cleanup [card gatherer-id]
  (-> card
      (move-meta-items gatherer-id)
      (move-multi-meta gatherer-id)))

;;--------------------------------------------------
;; print cleanup

(defn- cleanup-print-side [card]
  (let [wupdate (fn [card keys f & args]
                  (if (get-in card keys)
                    (apply update-in card keys f args)
                    card))]
    (-> card
        (wupdate [:rulelist] #(map (comp uppercase-mana
                                         fix-mana
                                         fix-chaos)
                                   %))
        (wupdate [:typelist] fix-type-delimiter))))

(defn- cleanup------card [card]
  (if (some #(.startsWith % "----") (:rulelist card))
    (let [[r1 [_ & r2]] (split-with #(not (.startsWith % "----"))
                                    (:rulelist card))
          [name types & r2] r2
          [pow tgh r2] (if (.contains (first r2) "/")
                         (let [[pt & r2] r2
                               [pow tgh] (.split pt "/")]
                           [pow tgh r2])
                         [nil nil r2])]
      (-> card
          (assoc-in [:rulelist] r1)
          (update-in [:multi] assoc
                     :name name :typelist types
                     :pow pow :tgh tgh :rulelist r2)))
    card))

;;--------------------------------------------------
;; interface

(defn cleanup [card]
  (let [gatherer-id (:gatherer-id card)
        card (pre-cleanup card)
        card (reduce cleanup-value card (keys card))
        card (if (:multi card)
               (assoc card :multi (cleanup (:multi card)))
               card)
        card (post-cleanup card gatherer-id)]
    card))

(defn cleanup-print [card]
  (-> card
      cleanup-print-side
      (update-in [:multi] cleanup-print-side)
      cleanup------card))

(comment
  {:name "Reckless Waif",
   :typelist ("Creature" "Human" "Rogue" "Werewolf"),
   :pow "1",
   :tgh "1",
   :sets
   {222111
    {:meta
     {:flavor
      "Before she just wanted to snatch your purse; now she'll take the whole arm.",
      :number "159b",
      :artist "Michael C. Hayes"},
     :flavor "Yes, I'm alone. No, I'm not worried.",
     :number "159a",
     :artist "Michael C. Hayes",
     :gatherer-id 222111,
     :set-name "Innistrad",
     :rarity :uncommon}},
   :multi
   {:color-indicator "Red",
    :name "Merciless Predator",
    :typelist ("Creature" "Werewolf"),
    :pow "3",
    :tgh "2",
    :rulelist
    ({:number 1,
      :text
      "At the beginning of each upkeep, if a player cast two or more spells last turn, transform Merciless Predator."
      :reminder "This rule has no reminder text."})},
   :cost "{R}",
   :rulelist
   ({:number 1,
     :text
     "At the beginning of each upkeep, if no spells were cast last turn, transform Reckless Waif."})})
