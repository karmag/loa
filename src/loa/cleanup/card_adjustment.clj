(ns loa.cleanup.card-adjustment)

;;--------------------------------------------------
;; internal

(defmulti fix-card :name)

(defmethod fix-card :default [card] card)

(defmethod fix-card "Forest"   [card] (dissoc card :rulelist))
(defmethod fix-card "Island"   [card] (dissoc card :rulelist))
(defmethod fix-card "Mountain" [card] (dissoc card :rulelist))
(defmethod fix-card "Plains"   [card] (dissoc card :rulelist))
(defmethod fix-card "Swamp"    [card] (dissoc card :rulelist))
(defmethod fix-card "Snow-Covered Forest"   [card] (dissoc card :rulelist))
(defmethod fix-card "Snow-Covered Island"   [card] (dissoc card :rulelist))
(defmethod fix-card "Snow-Covered Mountain" [card] (dissoc card :rulelist))
(defmethod fix-card "Snow-Covered Plains"   [card] (dissoc card :rulelist))
(defmethod fix-card "Snow-Covered Swamp"    [card] (dissoc card :rulelist))

(defmethod fix-card "Master of the Wild Hunt Avatar"
  [card]
  (update-in card [:rulelist]
             (fn [[rule & _]]
               [(update-in rule [:text]
                           #(.replace % "green Rhino" "Rhino"))])))

(defmethod fix-card "Homura, Human Ascendant"
  [card]
  (-> card
      (update-in [:multi] dissoc :pow :tgh :cost)
      (assoc-in [:multi :rulelist]
                [{:number 1
                  :text "Creatures you control get +2/+2 and have flying and \"{R}: This creature gets +1/+0 until end of turn.\""}])))

(defmethod fix-card "Soltari Guerrillas"
  [card]
  (assoc-in card [:cost] "{2}{R}{W}"))

(defmethod fix-card "Budoka Pupil"
  [card]
  (assoc-in card
            [:multi]
            {:name "Ichiga, Who Topples Oaks"
             :typelist ["Legendary" "Creature" "Spirit"]
             :pow "4"
             :tgh "3"
             :rulelist [{:number 1
                         :text "Trample"}
                        {:number 2
                         :text "Remove a ki counter from Ichiga, Who Topples Oaks: Target creature gets +2/+2 until end of turn."}]}))

(defmethod fix-card "Cunning Bandit"
  [card]
  (assoc-in card
            [:multi]
            {:name "Azamuki, Treachery Incarnate"
             :typelist ["Legendary" "Creature" "Spirit"]
             :pow "5"
             :tgh "2"
             :rulelist [{:number 1
                         :text "Remove a ki counter from Azamuki, Treachery Incarnate: Gain control of target creature until end of turn."}]}))

(defmethod fix-card "Callow Jushi"
  [card]
  (-> card
      (update-in [:rulelist] (partial take 2))
      (assoc :multi {:name "Jaraku the Interloper"
                     :typelist ["Legendary" "Creature" "Spirit"]
                     :pow "3"
                     :tgh "4"
                     :rulelist [{:number 1
                                 :text "Remove a ki counter from Jaraku the Interloper: Counter target spell unless its controller pays {2}."}]})))

(defmethod fix-card "Hired Muscle"
  [card]
  (-> card
      (update-in [:rulelist] (partial take 2))
      (assoc :multi {:name "Scarmaker"
                     :typelist ["Legendary" "Creature" "Spirit"]
                     :pow "4"
                     :tgh "4"
                     :rulelist [{:number 1
                                 :text "Remove a ki counter from Scarmaker: Target creature gains fear until end of turn."
                                 :reminder "It can't be blocked except by artifact creatures and/or black creatures."}]})))

;;--------------------------------------------------
;; interface

(defn fix [card]
  (fix-card card))
