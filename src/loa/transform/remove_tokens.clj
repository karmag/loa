
(ns loa.transform.remove-tokens)

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

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [cards]
  (remove #(token-cards (:name %)) cards))
