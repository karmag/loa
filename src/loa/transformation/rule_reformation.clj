
(ns loa.transformation.rule-reformation)


(defn- trim-rule
  [rule]
  (-> rule
      ;; mana
      (.replaceAll "(?:\\{[WUBRG0-9XYZ](?:/[WUBRG0-9XYZ])*\\})+" "<mana>")
      ;; pow/tgh
      (.replaceAll "[+-]\\d+/[+-]\\d" "<pt-change>")
      (.replaceAll "\\d+/\\d+" "<pt-absolute>")
      ;; tap
      (.replaceAll "\\{T\\}" "<tap>")
      ;; life up-down
      (.replaceAll "gain \\d+ life" "<life-gain>")
      (.replaceAll "lose \\d+ life" "<life-loss>")
      ;; colors
;;      (.replaceAll "(?i)(white|blue|black|red|green)" "<color>")
      ))

(defn- trim-rule-2
  [rule type-names]
  (comment
    (reduce #(.replaceAll %1 %2 "<type>")
            rule
            type-names))
  rule)

(defn print-rules
  [cards]
  (let [type-names (->> (map :type cards)
                        (apply concat)
                        (remove #(= "of" %))
                        set)
        rules (->> (map (fn [{:keys [name rules]}]
                          (map #(.replaceAll % name "<name>")
                               (filter identity
                                       (map :text rules))))
                        cards)
                   (apply concat)
                   (map trim-rule)
                   (map #(trim-rule-2 % type-names))
                   set)]
    (pprint  (sort rules))
    (println (count (set rules)))))
