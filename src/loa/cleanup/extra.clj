(ns loa.cleanup.extra)

;;-------------------------------------------------
;;  Planes

(def celestine-reef
  {:name "Celestine Reef"
   :typelist ["Plane" "Luvion"]
   :sets {0 {:set-name "Planechase"
             :rarity :promo}}
   :rulelist [{:text "Creatures without flying or islandwalk can't attack."}
              {:text "Whenever you roll {C}, until a player planeswalks, you can't lose the game and your opponents can't win the game."}]})

(def horizon-boughs
  {:name "Horizon Boughs"
   :typelist ["Plane" "Pyrulea"]
   :sets {0 {:set-name "Planechase"
             :rarity :promo}}
   :rulelist [{:text "All permanents untap during each player's untap step."}
              {:text "Whenever you roll {C}, you may search your library for up to three basic land cards, put them onto the battlefield tapped, then shuffle your library."}]})

(def mirrored-depths
  {:name "Mirrored Depths"
   :typelist ["Plane" "Karsus"]
   :sets {0 {:set-name "Planechase"
             :rarity :promo}}
   :rulelist [{:text "Whenever a player casts a spell, that player flips a coin. If he or she loses the flip, counter that spell."}
              {:text "Whenever you roll {C}, target player reveals the top card of his or her library. If it's a nonland card, you may cast it without paying its mana cost."}]})

(def tember-city
  {:name "Tember City"
   :typelist ["Plane" "Kinshala"]
   :sets {0 {:set-name "Planechase"
             :rarity :promo}}
   :rulelist [{:text "Whenever a player taps a land for mana, Tember City deals 1 damage to that player."}
              {:text "Whenever you roll {C}, each other player sacrifices a nonland permanent."}]})

;;-------------------------------------------------
;;  Schemes

(def perhaps-youve-met-my-cohort
  {:name "Perhaps You've Met My Cohort"
   :typelist ["Scheme"]
   :sets {0 {:set-name "Archenemy"
             :rarity :promo}}
   :rulelist [{:text "When you set this scheme in motion, search your library for a planeswalker card, put it onto the battlefield, then shuffle your library."}]})

(def plots-that-span-centuries
  {:name "Plots That Span Centuries"
   :typelist ["Scheme"]
   :sets {0 {:set-name "Archenemy"
             :rarity :promo}}
   :rulelist [{:text "When you set this scheme in motion, the next time you would set a scheme in motion, set three schemes in motion instead."}]})

(def your-inescapable-doom
  {:name "Your Inescapable Doom"
   :typelist ["Ongoing" "Scheme"]
   :sets {0 {:set-name "Archenemy"
             :rarity :promo}}
   :rulelist [{:text "At the beginning of your end step, put a doom counter on this scheme, then this scheme deals damage equal to the number of doom counters on it to the opponent with the highest life total among your opponents. If two or more players are tied for highest life total, you choose one."}]})

(def imprison-this-insolent-wretch
  {:name "Imprison This Insolent Wretch"
   :typelist ["Ongoing" "Scheme"]
   :sets {0 {:set-name "Archenemy"
             :rarity :promo}}
   :rulelist [{:text "When you set this scheme in motion, choose an opponent."}
              {:text "Permanents the chosen player controls don't untap during his or her untap step."}
              {:text "When the chosen player is attacked or becomes the target of a spell or ability, abandon this scheme."}]})

(def drench-the-soil-in-their-blood
  {:name "Drench the Soil in Their Blood"
   :typelist ["Scheme"]
   :sets {0 {:set-name "Archenemy"
             :rarity :promo}}
   :rulelist [{:text "When you set this scheme in motion, after the main phase, there is an additional combat phase followed by an additional main phase. Creatures you control gain vigilance until end of turn."}]})

;;-------------------------------------------------
;;  Dreamcast

(def dreamcast-cards
  (map
   #(assoc % :sets {0 {:set-name "Dreamcast"
                       :rarity :special}})

   [{:name "Arden Angel"
     :cost "{4}{W}{W}"
     :typelist ["Creature" "Angel"]
     :pow "4"
     :tgh "4"
     :rulelist [{:text "Flying"}
                {:text "At the beginning of your upkeep, if Arden Angel is in your graveyard, choose a number from 1 to 4 at random. If it's 1, you may return Arden Angel onto the battlefield."}]}

    {:name "Ashuza's Breath"
     :cost "{1}{R}"
     :typelist ["Sorcery"]
     :rulelist [{:text "Ashuza's Breath deals X damage to each creature, where X is a number chosen at random from 0 to 2."}]}

    {:name "Camato Scout"
     :cost "{1}{U}{U}"
     :typelist ["Creature" "Merfolk" "Scout"]
     :pow "2"
     :tgh "3"
     :rulelist [{:text "When Camato Scout enters the battlefield, it gains landwalk of a basic land type chosen at random."
                 :reminder "This effect lasts indefinitely."}]}

    {:name "Hapato's Might"
     :cost "{2}{B}"
     :typelist ["Instant"]
     :rulelist [{:text "Target creature gets +X/+0 until end of turn, where X is a number chosen randomly from 0 to 6."}]}

    {:name "Lydari Druid"
     :cost "{2}{G}"
     :typelist ["Creature" "Human" "Druid"]
     :pow "2"
     :tgh "2"
     :rulelist [{:text "When Lydari Druid enters the battlefield, for each land, choose a basic land type at random. That land becomes that land type."
                 :reminder "This effect lasts indefinitely."}]}

    {:name "Lydari Elephant"
     :cost "{4}{G}"
     :typelist ["Creature" "Elephant"]
     :pow "*"
     :tgh "*"
     :rulelist [{:text "Lydari Elephant enters the battlefield as a X/Y creature, where X and Y are numbers randomly chosen from 3 to 7."}]}

    {:name "Murgish Cemetery"
     :cost "{4}{B}{B}"
     :typelist ["Enchantment"]
     :rulelist [{:text "{3}{B}, Discard a card: Put an X/X black Zombie token onto the battlefield, where X is a number randomly chosen from 2 to 6."}]}

    {:name "Saji's Torrent"
     :cost "{1}{U}"
     :typelist ["Instant"]
     :rulelist [{:text "Choose a number from 0 to 5 at random, then choose that many creatures and tap them."}]}

    {:name "Tornellan Protector"
     :cost "{2}{W}"
     :typelist ["Creature" "Human" "Cleric"]
     :pow "1"
     :tgh "2"
     :rulelist [{:text "{T}: Choose a number from 1 to 3 at random. Until end of turn, if a source would deal damage to target creature or player, it deals that much damage minus the chosen number to that creature or player instead."}]}

    {:name "Velican Dragon"
     :cost "{5}{R}{R}"
     :typelist ["Creature" "Dragon"]
     :pow "5"
     :tgh "5"
     :rulelist [{:text "Flying"}
                {:text "Whenever Velican Dragon becomes blocked, it gets +X/+0 until end of turn, where X is a number chosen at random from 0 to 5."}]}
    ]))

;;-------------------------------------------------
;;  Astral

(def astral-cards
  (map
   #(assoc % :sets {0 {:set-name "Astral"
                       :rarity :special}})

   [{:name "Aswan Jaguar"
     :cost "{1}{G}{G}"
     :typelist ["Creature" "Cat"]
     :pow "2"
     :tgh "2"
     :rulelist [{:text "As Aswan Jaguar enters the battlefield, choose a creature type at random from among all creature types that a creature card in target opponent's decklist has."}
                {:text "{G}{G}, {T}: Destroy target creature with the chosen type. It can't be regenerated."}]}

    {:name "Call from the Grave"
     :cost "{2}{B}"
     :typelist ["Sorcery"]
     :rulelist [{:text "Choose a player at random. Choose a creature card in that player's graveyard at random and put it onto the battlefield under your control. Call from the Grave deals damage equal to that creature card's converted mana cost to you."}]}

    {:name "Faerie Dragon"
     :cost "{2}{G}{G}"
     :typelist ["Creature" "Dragon"]
     :pow "1"
     :tgh "3"
     :rulelist [{:text "Flying"}
                {:text "{1}{G}{G}: Perform a random action from the following list:"}
                {:text "A creature chosen at random gains trample and gets +X/+0 until end of turn, where X is its power. At the beginning of the next end step, destroy that creature if it attacked this turn."}
                {:text "You may tap or untap an artifact, creature, or land chosen at random."}
                {:text "If a creature chosen at random has toughness 5 or greater, it gets +4/-4 until end of turn. Otherwise, it gets +4/-X until end of turn, where X is its toughness minus 1."}
                {:text "A spell or permanent chosen at random becomes green." :reminder "Mana symbols on that permanent remain unchanged."}
                {:text "A spell or permanent chosen at random becomes white." :reminder "Mana symbols on that permanent remain unchanged."}
                {:text "A spell or permanent chosen at random becomes red." :reminder "Mana symbols on that permanent remain unchanged."}
                {:text "Faerie Dragon deals 3 damage to a creature or player chosen at random."}
                {:text "A creature chosen at random gains flying until end of turn."}
                {:text "A creature chosen at random gets +3/+3 until end of turn."}
                {:text "A creature chosen at random gains banding until end of turn." :reminder "Any creatures with banding, and up to one without, can attack in a band. Bands are blocked as a group. If any creatures with banding a player controls are blocking or being blocked by a creature, that player divides that creature's combat damage, not its controller, among any of the creatures it's being blocked by or is blocking."}
                {:text "A spell or permanent chosen at random becomes black." :reminder "Mana symbols on that permanent remain unchanged."}
                {:text "A spell or permanent chosen at random becomes blue." :reminder "Mana symbols on that permanent remain unchanged."}
                {:text "A creature chosen at random can't be regenerated this turn."}
                {:text "If a creature chosen at random has power 2 or less, it is unblockable this turn."}
                {:text "A creature chosen at random gets -2/-0 until end of turn."}
                {:text "Return a creature chosen at random to its owner's hand."}
                {:text "Faerie Dragon deals 1 damage to a creature or player chosen at random."}
                {:text "A creature other than Faerie Dragon chosen at random becomes 0/2 until end of turn."}
                {:text "Exile a creature chosen at random. Its controller gains life equal to its power."}
                {:text "Randomly distribute X -0/-1 counters among a random number of creatures chosen at random."}]}

    {:name "Gem Bazaar"
     :typelist ["Land"]
     :rulelist [{:text "As Gem Bazaar enters the battlefield, choose a color at random."}
                {:text "{T}: Add one mana of the last chosen color to your mana pool. Then choose a color at random."}]}

    {:name "Goblin Polka Band"
     :cost "{R}{R}"
     :typelist ["Creature" "Goblin"]
     :pow "1"
     :tgh "1"
     :rulelist [{:text "{2}, {T}: Choose any number of target creatures at random. Tap those creatures. Goblins tapped this way do not untap during their controllers' next untap steps. This ability costs R more to activate for each target."}]}

    {:name "Necropolis of Azar"
     :cost "{2}{B}{B}"
     :typelist ["Enchantment"]
     :rulelist [{:text "Whenever a nonblack creature is put into a graveyard from the battlefield, put a husk counter on Necropolis of Azar."}
                {:text "{5}, Remove a husk counter from Necropolis of Azar: Put a X/Y black Spawn creature token named Spawn of Azar with swampwalk onto the battlefield, where X and Y are numbers chosen at random from 1 to 3."}]}

    {:name "Orcish Catapult"
     :cost "{X}{R}{R}"
     :typelist ["Instant"]
     :rulelist [{:text "Randomly distribute X -0/-1 counters among a random number of random target creatures."}]}

    {:name "Pandora's Box"
     :cost "{5}"
     :typelist ["Artifact"]
     :rulelist [{:text "{3}, {T}: Choose a creature card at random from all players' decklists. For each player, flip a coin. If the flip ends up heads, put a token that's a copy of that creature card onto the battlefield under that player's control."}]}

    {:name "Power Struggle"
     :cost "{2}{U}{U}{U}"
     :typelist ["Enchantment"]
     :rulelist [{:text "At the beginning of each player's upkeep, exchange control of a target artifact, creature or land that player controls, chosen at random, and a random target permanent that shares one of those types with it a player who is his or her opponent chosen at random."}]}

    {:name "Prismatic Dragon"
     :cost "{2}{W}{W}"
     :typelist ["Creature" "Dragon"]
     :pow "2"
     :tgh "3"
     :rulelist [{:text "Flying"}
                {:text "At the beginning of your upkeep, Prismatic Dragon becomes a color chosen at random." :reminder "This effect lasts indefinitely."}
                {:text "{2}: Prismatic Dragon becomes a color chosen at random." :reminder "This effect lasts indefinitely."}]}

    {:name "Rainbow Knights"
     :cost "{W}{W}"
     :typelist ["Creature" "Human" "Knight"]
     :pow "2"
     :tgh "1"
     :rulelist [{:text "As Rainbow Knights enters the battlefield, it gains protection from a color chosen at random." :reminder "This effect lasts indefinitely."}
                {:text "{1}: Rainbow Knights gains first strike until end of turn."}
                {:text "{W}{W}: Rainbow Knights gets +X/+0 until end of turn, where X is a number chosen randomly from 0 to 2."}]}

    {:name "Whimsy"
     :cost "{X}{U}{U}"
     :typelist ["Sorcery"]
     :rulelist [{:text "Perform X random actions from the following list:"}
                {:text "Return a permanent that isn't enchanted chosen at random to its owner's hand."}
                {:text "Untap a artifact, creature or land chosen at random."}
                {:text "Tap a artifact, creature or land chosen at random."}
                {:text "Whimsy deals 4 damage to a creature or player chosen at random."}
                {:text "A player chosen at random draws three cards."}
                {:text "Destroy an artifact chosen at random. It can't be regenerated. That artifact's controller gains life equal to its converted mana cost."}
                {:text "Destroy an artifact or enchantment chosen at random."}
                {:text "A player chosen at random gains 3 life."}
                {:text "Prevent the next 3 damage that would be dealt to a creature or player chosen at random this turn."}
                {:text "Destroy a creature or land chosen at random. It can't be regenerated."}
                {:text "A player chosen at random puts the top two cards of his or her library into his or her graveyard."}
                {:text "Put a 1/1 colorless Insect artifact creature token with flying named Wasp onto the battlefield." :reminder "It can't be blocked except by creatures with flying or reach."}
                {:text "Destroy all artifacts, creatures and enchantments."}
                {:text "Flip a coin. If you lose the flip, Whimsy deals 5 damage to you. If you win the flip, put a 5/5 colorless Djinn artifact creature token with flying onto the battlefield."}
                {:text "Choose a creature card at random from all players' decklists. For each player, flip a coin. If the flip ends up heads, put a token that's a copy of that creature card onto the battlefield under that player's control."}
                {:text "A player chosen at random discards a card."}
                {:text "Prevent all combat damage that would be dealt this turn."}
                {:text "Draw a card and reveal it. If it isn't a land card, discard it."}]}]))

;;-------------------------------------------------
;; helpers

(defn- add-rule-number [card]
  (update-in card [:rulelist]
             (fn [rules]
               (map (fn [index rule]
                      (assoc rule :number index))
                    (iterate inc 1)
                    rules))))

;;-------------------------------------------------
;; interface

(defn get-extra-cards []
  (map add-rule-number
       (concat [celestine-reef horizon-boughs mirrored-depths tember-city]
               [perhaps-youve-met-my-cohort plots-that-span-centuries
                your-inescapable-doom imprison-this-insolent-wretch
                drench-the-soil-in-their-blood]
               dreamcast-cards
               astral-cards)))
