
(ns loa.transformation.transform)

(def #^{:private true}
     mana-symbols "WUBRGXYZ\\d")

(defn fix-mana
  "Reshapes mana-symbols to look like {X} or {X/Y}. Will also fix the
  tap symbol."
  [s]
  (reduce (fn [s [a b]]
            (.replaceAll s a b))
          s
          [["ocT" "{T}"]
           [(format "o([%s])" mana-symbols) "$1"]
           ["\\{S\\}i\\{S\\}i\\}" "{S}{S}"]
           ["\\{S\\}i\\}" "{S}"]
           [(format "\\(([%s])/([%s])\\)" mana-symbols mana-symbols)
            "{$1/$2}"]]))
