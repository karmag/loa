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
;;  Dreamcast
;;
(def dreamcast-cards
     (map
      #(assoc %1 :set-rarity [["Dreamcast" :special]])
      [{:name "Arden Angel"
        :cost "4WW"
        :types ["Creature" "Angel"]
        :pow "4"
        :tgh "4"
        :rules [{:text "Flying"}
                {:text "At the beginning of your upkeep, if Arden Angel is in your graveyard, choose a number from 1 to 4 at random. If it's 1, you may return Arden Angel onto the battlefield."}]}

       {:name "Ashuza's Breath"
        :cost "1R"
        :types ["Sorcery"]
        :rules [{:text "Ashuza's Breath deals X damage to each creature, where X is a number chosen at random from 0 to 2."}]}

       {:name "Camato Scout"
        :cost "1UU"
        :types ["Creature" "Merfolk" "Scout"]
        :pow "2"
        :tgh "3"
        :rules [{:text "When Camato Scout enters the battlefield, it gains landwalk of a basic land type chosen at random. (This effect lasts indefinitely.)"}]}

       {:name "Hapato's Might"
        :cost "2B"
        :types ["Instant"]
        :rules [{:text "Target creature gets +X/+0 until end of turn, where X is a number chosen randomly from 0 to 6."}]}

       {:name "Lydari Druid"
        :cost "2G"
        :types ["Creature" "Human" "Druid"]
        :pow "2"
        :tgh "2"
        :rules [{:text "When Lydari Druid enters the battlefield, for each land, choose a basic land type at random. That land becomes that land type." :reminder "This effect lasts indefinitely."}]}

       {:name "Lydari Elephant"
        :cost "4G"
        :types ["Creature" "Elephant"]
        :pow "*"
        :tgh "*"
        :rules [{:text "Lydari Elephant enters the battlefield as a X/Y creature, where X and Y are numbers randomly chosen from 3 to 7."}]}

       {:name "Murgish Cemetery"
        :cost "4BB"
        :types ["Enchantment"]
        :rules [{:text "3B, Discard a card: Put an X/X black Zombie token onto the battlefield, where X is a number randomly chosen from 2 to 6."}]}

       {:name "Saji's Torrent"
        :cost "1U"
        :types ["Instant"]
        :rules [{:text "Choose a number from 0 to 5 at random, then choose that many creatures and tap them."}]}

       {:name "Tornellan Protector"
        :cost "2W"
        :types ["Creature" "Human" "Cleric"]
        :pow "1"
        :tgh "2"
        :rules [{:text "T: Choose a number from 1 to 3 at random. Until end of turn, if a source would deal damage to target creature or player, it deals that much damage minus the chosen number to that creature or player instead."}]}

       {:name "Velican Dragon"
        :cost "5RR"
        :types ["Creature" "Dragon"]
        :pow "5"
        :tgh "5"
        :rules [{:text "Flying"}
                {:text "Whenever Velican Dragon becomes blocked, it gets +X/+0 until end of turn, where X is a number chosen at random from 0 to 5."}]}
       ]))

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
           your-inescapable-doom]
          dreamcast-cards))
