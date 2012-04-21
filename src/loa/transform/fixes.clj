
(ns loa.transform.fixes
  "This transformer updates cards that are wrong in gatherer.")

(defmulti cleanup-card :name)

(defmethod cleanup-card :default [card] card)

(defmethod cleanup-card "Homura, Human Ascendant"
  [card]
  (let [multi (-> (-> card :multi first)
                  (dissoc :pow :tgh)
                  (assoc :rules (list
                                 {:text "Creatures you control get +2/+2 and have flying and \"{R}: This creature gets +1/+0 until end of turn.\""})))]
    (assoc card :multi (list multi))))

(defmethod cleanup-card "Rage Extractor"
  [card]
  (update-in card [:rules]
             (fn [[one two]]
               (list one
                     {:text
                      "Whenever you cast a spell with {P} in its mana cost, Rage Extractor deals damage equal to that spell's converted mana cost to target creature or player."}))))

(defmethod cleanup-card "Soltari Guerrillas"
  [card]
  (assoc-in card [:cost] "2RW"))

(defmethod cleanup-card "Budoka Pupil"
  [card]
  (assoc-in card
            [:multi]
            [{:multi-type :flip
              :name "Ichiga, Who Topples Oaks"
              :types ["Legendary" "Creature" "Spirit"]
              :pow "4"
              :tgh "3"
              :rules [{:text "Trample"}
                      {:text "Remove a ki counter from Ichiga, Who Topples Oaks: Target creature gets +2/+2 until end of turn."}]
              :set-rarity [["Betrayers of Kamigawa" :uncommon]]}]))

(defmethod cleanup-card "Cunning Bandit"
  [card]
  (assoc-in card
            [:multi]
            [{:multi-type :flip
              :name "Azamuki, Treachery Incarnate"
              :types ["Legendary" "Creature" "Spirit"]
              :pow "5"
              :tgh "2"
              :rules [{:text "Remove a ki counter from Azamuki, Treachery Incarnate: Gain control of target creature until end of turn."}]
              :set-rarity [["Betrayers of Kamigawa" :uncommon]]}]))

(defmethod cleanup-card "Shield of Kaldra"
  [card]
  (update-in card
             [:meta]
             #(map (fn [item]
                     (if (= (:set item) "Promo set for Gatherer Promo")
                       (assoc item :set "Promo set for Gatherer" :rarity :rare)
                       item))
                   %)))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [card]
  (cleanup-card card))
