
(ns loa.transform.plane-chaos)

(defn- fix-chaos-rule
  [{text :text :as rule}]
  (if (empty? text)
    rule
    (assoc rule :text (.replaceAll text "chaos" "{C}"))))

(defn process
  [card]
  (if ((set (:types card)) "Plane")
    (update-in card [:rules] #(map fix-chaos-rule %))
    card))
