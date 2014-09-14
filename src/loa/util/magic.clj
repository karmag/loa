(ns loa.util.magic)

;;--------------------------------------------------
;; data

(def super-type #{"Basic" "Legendary" "Ongoing" "Snow" "World"})

(def card-type #{"Artifact" "Creature" "Conspiracy" "Enchantment" "Instant"
                 "Land" "Plane" "Planeswalker" "Scheme" "Sorcery" "Tribal"
                 "Vanguard" "Phenomenon"})

(def keyword-ability
  #{ ;; general
    "Absorb" "Affinity" "Amplify"
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
    "Intimidate" "Kicker" "Level Up"
    "Lifelink" "Living Weapon"
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
    "Vigilance" "Wither"
    ;; landwalk
    "Plainswalk" "Islandwalk" "Swampwalk"
    "Mountainwalk" "Forestwalk"
    ;; Cycling alternatives
    "Plainscycling" "Islandcycling" "Swampcycling"
    "Mountaincycling" "Forestcycling"
    "Basic landcycling"})

(def token-name
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
;; functions

(defn find-type
  "Returns the type of a single type from the typeline of a card."
  [type]
  (cond (super-type type) "super"
        (card-type type) "card"
        :else "sub"))
