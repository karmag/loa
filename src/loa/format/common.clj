
(ns loa.format.common)

(defn format-mana
  "Takes a mana string and applies {}-brackets where suitable."
  [mana]
  (reduce (fn [s item]
            (if (= \{ (first item))
              (str s item)
              (format "%s{%s}" s item)))
          ""
          (re-seq #"\{.+?\}|\d+|." mana)))
