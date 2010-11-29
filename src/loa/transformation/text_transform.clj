
(ns loa.transformation.text-transform)

(def #^{:private true}
     super-type
     #{"basic" "legendary" "ongoing" "snow" "world"
       "artifact" "creature" "enchantment" "instant" "land"
       "plane" "planeswalker" "scheme" "sorcery" "tribal"
       "vanguard"})

(defn types
  [typeseq]
  (let [supers (take-while #(super-type (.toLowerCase %)) typeseq)
        regulars (nthnext typeseq (count supers))
        typeseq (concat supers
                        (when-not (empty? regulars) ["-"])
                        regulars)]
    (reduce #(str %1 " " %2) typeseq)))

