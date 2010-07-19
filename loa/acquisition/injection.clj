(comment
"This namespace constructs the missing cards that are not available in
gatherer.")

(ns loa.acquisition.injection)

;;-------------------------------------------------
;;
;;  Planes
;;
(def celestine-reef
     {:name "Celestine Reef"
      :types ["Plane" "Luvion"]
      :set-rarity [["Planechase" :promo]]
      :rules [{:text "Creatures without flying or islandwalk can't attack."}
              {:text "Whenever you roll {C}, until a player planeswalks, you can't lose the game and your opponents can't win the game."}]})

(def horizon-boughs
     {:name "Horizon Boughs"
      :types ["Plane" "Pyrulea"]
      :set-rarity [["Planechase" :promo]]
      :rules [{:text "All permanents untap during each player's untap step."}
              {:text "Whenever you roll {C}, you may search your library for up to three basic land cards, put them onto the battlefield tapped, then shuffle your library."}]})

(def mirrored-depths
     {:name "Mirrored Depths"
      :types ["Plane" "Karsus"]
      :set-rarity [["Planechase" :promo]]
      :rules [{:text "Whenever a player casts a spell, that player flips a coin. If he or she loses the flip, counter that spell."}
              {:text "Whenever you roll {C}, target player reveals the top card of his or her library. If it's a nonland card, you may cast it without paying its mana cost."}]})

(def tember-city
     {:name "Tember City"
      :types ["Plane" "Kinshala"]
      :set-rarity [["Planechase" :promo]]
      :rules [{:text "Whenever a player taps a land for mana, Tember City deals 1 damage to that player."}
              {:text "Whenever you roll {C}, each other player sacrifices a nonland permanent."}]})

;;-------------------------------------------------
;;
;;  Schemes
;;
(def perhaps-youve-met-my-cohort
     {:name "Perhaps You've Met My Cohort"
      :types ["Scheme"]
      :set-rarity [["Archenemy" :promo]]
      :rules [{:text "When you set this scheme in motion, search your library for a planeswalker card, put it onto the battlefield, then shuffle your library."}]})

(def plots-that-span-centuries
     {:name "Plots That Span Centuries"
      :types ["Scheme"]
      :set-rarity [["Archenemy" :promo]]
      :rules [{:text "When you set this scheme in motion, the next time you would set a scheme in motion, set three schemes in motion instead."}]})

(def your-inescapable-doom
     {:name "Your Inescapable Doom"
      :types ["Scheme"]
      :set-rarity [["Archenemy" :promo]]
      :rules [{:text "At the beginning of your end step, put a doom counter on this scheme, then this scheme deals damage equal to the number of doom counters on it to the opponent with the highest life total among your opponents. If two or more players are tied for highest life total, you choose one."}]})

;;-------------------------------------------------
;;
;;  Interface
;;
(defn get-cards
  []
  (concat [celestine-reef
           horizon-boughs
           mirrored-depths
           tember-city]
          [perhaps-youve-met-my-cohort
           plots-that-span-centuries
           your-inescapable-doom]))
