
(ns loa.transformation.transform)

(def #^{:private true}
  mana-symbols "WUBRGXYZ\\d")

(def #^{:private true}
  mana-symbols-all "wubrgxyzWUBRGXYZ\\d")

(def #^{:private true}
  brace-mana-lowercase-re #"\{[wubrgxyz\d](?:/[wubrgxyz\d])?\}")

(defn uppercase-mana
  "Makes all mana symbols uppercase."
  [string]
  (reduce (fn [^String string ^String lowercase]
            (.replace string lowercase (.toUpperCase lowercase)))
          string
          (set (re-seq brace-mana-lowercase-re string))))

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
           [(format "\\{?\\(([%s])/([%s])\\)\\}?"
                    mana-symbols-all
                    mana-symbols-all)
            "{$1/$2}"]]))

