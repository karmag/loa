
(ns loa.util.data)

(def super-type #{"Basic" "Legendary" "Ongoing" "Snow" "World"})

(def card-type #{"Artifact" "Creature" "Enchantment" "Instant" "Land" "Plane"
                 "Planeswalker" "Scheme" "Sorcery" "Tribal" "Vanguard"})

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

(def ascii-mapping
  {
   \u00ae ""
   \u00c6 "AE"
   \u00e0 "a"
   \u00e1 "a"
   \u00e2 "a"
   \u00e9 "e"
   \u00ed "i"
   \u00f6 "o"
   \u00fa "u"
   \u00fb "u"
   \u2014 "-"
   \u2018 "'"
   \u2019 "'"
   })

(def rarity-mapping
  {
   :common      {:code "C"}
   :uncommon    {:code "U"}
   :rare        {:code "R"}
   :mythic      {:code "M"}
   :land        {:code "L"}
   :promo       {:code "P"}
   :special     {:code "S"}
   })
