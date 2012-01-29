
(ns loa.transform.transform-cards)

(def transformers
  { ;; Innistrad
   "Hanweir Watchkeep"      "Bane of Hanweir"
   "Bloodline Keeper"       "Lord of Lineage"
   "Civilized Scholar"      "Homicidal Brute"
   "Cloistered Youth"       "Unholy Fiend"
   "Daybreak Ranger"        "Nightfall Predator"
   "Delver of Secrets"      "Insectile Aberration"
   "Garruk Relentless"      "Garruk, the Veil-Cursed"
   "Gatstaf Shepherd"       "Gatstaf Howler"
   "Grizzled Outcasts"      "Krallenhorde Wantons"
   "Instigator Gang"        "Wildblood Pack"
   "Kruin Outlaw"           "Terror of Kruin Pass"
   "Ludevic's Test Subject" "Ludevic's Abomination"
   "Mayor of Avabruck"      "Howlpack Alpha"
   "Reckless Waif"          "Merciless Predator"
   "Screeching Bat"         "Stalking Vampire"
   "Thraben Sentry"         "Thraben Militia"
   "Tormented Pariah"       "Rampaging Werewolf"
   "Ulvenwald Mystics"      "Ulvenwald Primordials"
   "Village Ironsmith"      "Ironfang"
   "Villagers of Estwald"   "Howlpack of Estwald"
   ;; Dark Ascension
   "Loyal Cathar"               "Unhallowed Cathar"
   "Soul Seizer"                "Ghastly Haunting"
   "Chosen of Markov"           "Markov's Servant"
   "Ravenous Demon"             "Archdemon of Greed"
   "Afflicted Deserter"         "Werewolf Ransacker"
   "Hinterland Hermit"          "Hinterland Scourge"
   "Mondronen Shaman"           "Tovolar's Magehunter"
   "Lambholt Elder"             "Silverpelt Werewolf"
   "Scorned Villager"           "Moonscarred Werewolf"
   "Wolfbitten Captive"         "Krallenhorde Killer"
   "Huntmaster of the Fells"    "Ravager of the Fells"
   "Chalice of Life"            "Chalice of Death"
   "Elbrus, the Binding Blade"  "Withengar Unbound"
   })

(defn- fix-transform-cards
  [cards]
  (let [reg-name? (set (keys transformers))
        tf-name? (set (vals transformers))
        tf-parts (reduce (fn [m card]
                           (assoc m (some (fn [[a b]]
                                            (when (= (:name card) b)
                                              a))
                                          transformers)
                                  (assoc card :multi-type :transform)))
                         nil
                         (filter #(tf-name? (:name %)) cards))]
    (->> cards
         (map (fn [{name :name :as card}]
                (cond
                 (tf-name? name) nil
                 (reg-name? name) (update-in card [:multi] conj (tf-parts name))
                 :else card)))
         (filter identity))))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [cards]
  (fix-transform-cards cards))
