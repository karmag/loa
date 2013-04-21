(ns loa.gatherer.card-details
  (:require (loa.gatherer (common :as common-))
            (loa.util (xml :as xml-))))

(declare translate-image-string)

;;--------------------------------------------------
;; helpers

(defn- get-text [xml]
  (-> (xml-/xml-> (xml-/from-xml xml) xml-/text)
      first
      .trim))

(def ^:private alt->letter
  {"Tap" "T"
   "Untap" "Q"
   "White" "W"
   "Black" "B"
   "Blue" "U"
   "Green" "G"
   "Red" "R"
   "Snow" "S"
   "[chaos]" "C"
   "Variable Colorless" "X"
   "Black or Green" "B/G"
   "Black or Red" "B/R"
   "Blue or Red" "U/R"
   "Green or Blue" "G/U"
   "Green or White" "G/W"
   "Red or White" "R/W"
   "White or Black" "W/B"
   "Red or Green" "R/G"
   "Blue or Black" "U/B"
   "White or Blue" "W/U"
   "Two or Black" "2/B"
   "Two or Blue" "2/U"
   "Two or Green" "2/G"
   "Two or Red" "2/R"
   "Two or White" "2/W"
   "Phyrexian" "P"
   "Phyrexian Black" "P/B"
   "Phyrexian Blue" "P/U"
   "Phyrexian Green" "P/G"
   "Phyrexian Red" "P/R"
   "Phyrexian White" "P/W"})

(defn- translate-image
  "Converts an image tag into its corresponding text equivalent."
  [xml]
  (let [alt (-> xml :attrs :alt)
        int? (try (Integer/parseInt alt)
                  true
                  (catch NumberFormatException e))
        value (if int?
                alt
                (alt->letter alt))]
    (if value
      (format "{%s}" value)
      (throw
       (Exception. (format "Unknown image alt value: '%s'" (str alt)))))))

(defn- translate-tag [xml]
  (case (:tag xml)
    :img (translate-image xml)
    :i (translate-image-string xml)))

(defn- cleanup-spaces
  "When translating images to text it's not quite possible to know
  within that limited context if there should be surronding whitespace
  or not. This method should be applied after such transformation has
  taken place."
  [text]
  (-> text
      (.replaceAll "} ([:,.{])" "}$1")
      (.replaceAll "([\"(]) \\{" "$1{")
      (.replaceAll " ' " " '")))

(defn- translate-image-string [xml]
  (->> (:content xml)
       (map #(if (string? %) % (translate-tag %)))
       (map (memfn trim))
       (interpose " ")
       (apply str)
       cleanup-spaces))

(defn- cleanup-rulelist [xml]
  (let [rules-xml
        (xml-/transform xml
                        (xml-/search (xml-/attr= :class "cardtextbox")))]
    (doall
     (->> rules-xml
          (map translate-image-string)
          (remove empty?)))))

(defn- cleanup-all-sets [xml]
  (let [sets (xml-/transform xml (xml-/search :a))
        href [:attrs :href]
        name [:content 0 :attrs :alt]]
    (reduce #(assoc %1
               (common-/href->gatherer-id (get-in %2 href))
               (get-in %2 name))
            nil
            sets)))

;;--------------------------------------------------
;; data cleanup

(defmulti cleanup-value (fn [key value] key))

(defmethod cleanup-value "Community Rating:" [name value])
(defmethod cleanup-value "Converted Mana Cost:" [name value])
(defmethod cleanup-value "Watermark:" [name value])

(defmethod cleanup-value "Card Name:"
  [name value]
  [:name (get-text value)])

(defmethod cleanup-value "Types:"
  [name value]
  [:typelist (get-text value)])

(defmethod cleanup-value "Mana Cost:"
  [name value]
  [:cost (translate-image-string value)])

(defmethod cleanup-value "Card Text:"
  [name value]
  [:rulelist (cleanup-rulelist value)])

(defmethod cleanup-value "Flavor Text:"
  [name value]
  [:flavor (first
            (xml-/transform value (xml-/search :i) xml-/text))])

(defmethod cleanup-value "P/T:"
  [name value]
  [:pt (get-text value)])

(defmethod cleanup-value "Expansion:"
  [name value]
  [:expansion (get-text value)])

(defmethod cleanup-value "Rarity:"
  [name value]
  [:rarity (get-text value)])

(defmethod cleanup-value "Color Indicator:"
  [name value]
  [:color-indicator (get-text value)])

(defmethod cleanup-value "All Sets:"
  [name value]
  [:sets (cleanup-all-sets value)])

(defmethod cleanup-value "Other Sets:"
  [name value]
  (cleanup-value "All Sets:" value))

(defmethod cleanup-value "Card Number:"
  [name value]
  [:number (get-text value)])

(defmethod cleanup-value "Artist:"
  [name value]
  [:artist (get-text value)])

(defmethod cleanup-value "Hand/Life:"
  [name value]
  [:hand-life (get-text value)])

(defmethod cleanup-value "Loyalty:"
  [name value]
  [:loyalty (get-text value)])

(defn- cleanup-row [[key value]]
  (let [key (-> key xml-/from-xml xml-/text .trim)]
    (cleanup-value key value)))

;;--------------------------------------------------
;; data extraction

(defn- get-rows [card-grid]
  (let [items
        (xml-/transform card-grid
                        (xml-/search (xml-/re-attr= :id #".*SubContent_.*Row"))
                        (xml-/search (xml-/re-attr= :class #"label|value")))]
    (partition 2 items)))

(defn- get-grids [page]
  (xml-/xml-> (xml-/from-html page)
              (xml-/search (xml-/attr= :class "rightCol"))
              xml-/node))

(defn- combine-grids [grids]
  (case (count grids)
    1 (first grids)
    2 (assoc (first grids) :multi (second grids))))

(defn- get-gatherer-id [page]
  (-> (xml-/xml1-> (xml-/from-html page)
                   (xml-/search (xml-/attr= :id "cardTextSwitchLink1"))
                   xml-/node)
      :attrs
      :href
      common-/href->gatherer-id))

;;--------------------------------------------------
;; interface

(defn find-details [page]
  (let [details (->> (get-grids page)
                     (map get-rows)
                     (map (partial map cleanup-row))
                     (map #(reduce (fn [m [k v]]
                                     (if k (assoc m k v) m))
                                   nil
                                   %))
                     combine-grids)]
    (assoc details :gatherer-id (get-gatherer-id page))))

(defn find-print-details [page]
  (let [base (select-keys (find-details page)
                          [:name :typelist :rulelist :flavor :multi])]
    (if (:multi base)
      (update-in base [:multi]
                 select-keys [:name :typelist :rulelist :flavor])
      base)))

(defn find-set-codes [page]
  (->> (re-seq #"Image.ashx?[^>]*set=(\w+)[^>]*alt=\"([^\"(]+)" page)
       (map (fn [[_ code name]]
              [(-> (.trim name)
                   (.replaceAll "&quot;" "\"")
                   (.replaceAll "&apos;" "'"))
               code]))
       (reduce (fn [m [name code]]
                 (assoc m name code))
               nil)))

(defn find-double-card-information
  "Returns information about the other parts of the card if any."
  [name page]
  (when-let [[_ fst snd]
             (re-find #"SubContentHeader_subtitleDisplay\"[^>]*>([^/]*)//([^<]*)<"
                      page)]
    (let [fst (.trim fst)
          snd (.trim snd)
          re (re-pattern
              (format "part=([A-Za-z]+)[^>]+multiverseid=(\\d+)[^<]*%s // %s"
                      fst
                      snd))
          [_ part gatherer-id] (re-find re page)
          gatherer-id (Integer/parseInt gatherer-id)]
      {:fst fst
       :snd snd
       :link {:part part
              :gatherer-id gatherer-id}})))

(comment
  {:gatherer-id 123456,
   :rarity "Common",
   :artist "Stabby Stanley",
   :name "Uslar",
   :typelist "Creature — Bear Knight",
   :expansion "Magic 2050",
   :sets
   {123 "Portal (Common)",
    456 "Magic 2050 (Common)",
    789 "Duel Decks: Bears vs. Fungus (Common)"},
   :pt "2 / 2",
   :flavor "He ate the world, his paws, his pals. But mostly he just ate.",
   :cost "{G}",
   :number "235",
   :rulelist ("Haste","{T}: Uslar deals 10 damage to everything.")}
  ;; Additional data
  {:loyalty "5",
   :hand-life "(Hand Modifier: +3 , Life Modifier: -4)"
   :multi <card>
   ;; :multi-type :transform
   :color-indicator "Green"})
