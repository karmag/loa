
(ns loa.transform.keyword-split
  "Break apart compound keyword abilities. This transformation also
  applies rule number to all rules."
  (:require (loa.util (data :as data_))))

(def keyword-regex
  (re-pattern
   (->> data_/keyword-ability
        (interpose "|")
        (apply str)
        (format "(?i)(%s)( [\\dWUBRGXYZ{}]+| from .*?)?"))))

(defn- split-protection
  "Returns a seq of rules. If a rule starts with 'protection from' and
  contains 'and' it will be split further. Other rules are returned as
  is."
  [rule-text]
  (if-let [parts (re-matches #"(?i)protection from (.*) and from (.*)"
                             rule-text)]
    (map #(format "protection from %s" %) (rest parts))
    [rule-text]))

(defn- multi-rule
  "Returns a seq of rules or nil."
  [rule-text]
  (when rule-text
    (let [parts (map re-matches
                     (repeat keyword-regex)
                     (map (memfn trim)
                          (.split rule-text "[,;]")))]
      (when (every? identity parts)
        (mapcat split-protection
                (map first parts))))))

(defn- capitalize
  [s]
  (apply str
         (.toUpperCase (str (first s)))
         (rest s)))

(defn- update-rule
  [index rule]
  (if-let [parts (multi-rule (:text rule))]
    (let [reminder (:reminder rule)
          rules (map (fn [part]
                       {:no index :text (capitalize part)})
                     parts)]
      (if (empty? reminder)
        rules
        (cons (assoc (first rules) :reminder reminder)
              (rest rules))))
    [(assoc rule :no index)]))

(defn- update-rules
  [rules]
  (mapcat #(update-rule %1 %2)
          (iterate inc 1)
          rules))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [card]
  (let [card (update-in card [:rules] update-rules)]
    (if (:multi card)
      (update-in card [:multi] (partial map process))
      card)))
