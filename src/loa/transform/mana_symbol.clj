
(ns loa.transform.mana-symbol
  "Transforms mana symbols in card cost and rules.")

;;--------------------------------------------------
;;
;;  Helpers
;;
(def #^{:private true}
  mana-symbols "WUBRGXYZP\\d+")

(def #^{:private true}
  mana-symbols-all "wubrgxyzpWUBRGXYZP\\d+")

(def #^{:private true}
  brace-mana-lowercase-re #"\{[wubrgxyzp\d](?:/[wubrgxyzp\d])?\}")

(defn- uppercase-mana
  "Makes all mana symbols uppercase."
  [string]
  (reduce (fn [^String string ^String lowercase]
            (.replace string lowercase (.toUpperCase lowercase)))
          string
          (set (re-seq brace-mana-lowercase-re string))))

(defn- fix-mana
  "Reshapes mana-symbols to look like {X} or {X/Y}. Will also fix the
  tap symbol."
  [string]
  (reduce (fn [s [a b]]
            (.replaceAll s a b))
          string
          [["ocT" "{T}"]
           [(format "o([%s])" mana-symbols) "$1"]
           ["\\{S\\}i\\{S\\}i\\}" "{S}{S}"]
           ["\\{S\\}i\\}" "{S}"]
           [(format "\\{?\\(([%s])/([%s])\\)\\}?"
                    mana-symbols-all
                    mana-symbols-all)
            "{$1/$2}"]
           ["\\{(\\d)\\}(\\d)\\}" "{$1$2}"]]))

(defn- fix
  [string]
  (when string
    (-> string fix-mana uppercase-mana)))

(defn- update-rules
  [rules]
  (map (fn [{:keys [text reminder] :as rule}]
         (cond
          (and text reminder) (-> rule
                                  (update-in [:text] fix)
                                  (update-in [:reminder] fix))
          text (update-in rule [:text] fix)
          reminder (update-in rule [:reminder] fix)))
       rules))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [card]
  (let [card (update-in card [:rules] update-rules)]
    (if (:cost card)
      (update-in card [:cost] fix)
      card)))
