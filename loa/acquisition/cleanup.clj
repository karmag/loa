(comment
"This functionality is for removal of cards that should not be
present and for any specific card corrections.")

(ns loa.acquisition.cleanup)

;;-------------------------------------------------
;;
;;  Removal
;;
(def #^{:private true}
     token-cards
     #{"Beast"
       "Demon"
       "Elephant"
       "Elemental"
       "Elemental Shaman"
       "Elf Warrior"
       "Goblin"
       "Hornet"
       "Minion"
       "Saproling"
       "Spirit"
       "Thrull"

       "Goblin token card"
       "Pegasus token card"
       "Sheep token card"
       "Soldier token card"
       "Squirrel token card"
       "Zombie token card"})

(defn filter-cards
  [cards]
  (remove #(token-cards (:name %)) cards))

;;-------------------------------------------------
;;
;;  Fixes
;;
(defmulti cleanup-card :name)

(defmethod cleanup-card :default [card] card)

(defmethod cleanup-card "Homura, Human Ascendant"
  [card]
  (let [multi (-> (-> card :multi first)
                  (dissoc :pow :tgh)
                  (assoc :rules (list
                                 {:text "Creatures you control get +2/+2 and have flying and \"{R}: This creature gets +1/+0 until end of turn.\""})))]
    (assoc card :multi (list multi))))

(defmethod cleanup-card "Who/What/When/Where/Why"
  [card]
  (let [make-part (fn [name cost rule]
                    {:name name           :cost cost
                     :types ["Instant"]   :rules [{:text rule}]
                     :multi-type :double})]
    (assoc card
      :name "Who"
      :cost "XW"
      :rules [{:text "Target player gains X life."}]
      :multi (list
              (make-part "What"  "2R" "Destroy target artifact.")
              (make-part "When"  "2U" "Counter target creature spell.")
              (make-part "Where" "3B" "Destroy target land.")
              (make-part "Why"   "1G" "Destroy target enchantment.")))))

;;-------------------------------------------------
;;
;;  Unhinged half mana/pt
;;
(defn- is-half
  [s]
  (and s (.contains s "1/2")))

(defn- get-halves
  "Returns the keys that contains halfsies."
  [card]
  (when (some #(= % "Unhinged")
            (map first (:set-rarity card)))
    (concat (filter identity
                    (map #(when (is-half (% card)) %)
                         [:pow :tgh :cost]))
            (when (some is-half (map :text (:rules card)))
              [:rules]))))

(defn fix-half
  "Fix cards that contain 1/2 mana/pow/tgh."
  [card]
  (let [ks (get-halves card)]
    (if (empty? ks)
      card
      card))) ;; TODO

;;-------------------------------------------------
;;
;;  Interface
;;
(defn process
  [cards]
  (->> (filter-cards cards)
       (map cleanup-card)
       (map fix-half)))
