
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

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [card]
  (cleanup-card card))
