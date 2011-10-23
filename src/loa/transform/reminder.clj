
(ns loa.transform.reminder
  "Moves the reminder text to to the :reminder key for card rules.")

;;--------------------------------------------------
;;
;;  Helpers
;;
(defn- update-rule
  [rule]
  (let [rem-list (map second
                      (re-seq #"\((.+?)\)" (:text rule)))]
    (if (empty? rem-list)
      rule
      (let [reminder (reduce str (interpose " " rem-list))
            text (.trim
                  (reduce (fn [txt rem]
                            (-> txt
                                (.replace (format "(%s)" rem) "")
                                (.replaceAll " \\." ".")))
                          (:text rule)
                          rem-list))]
        (merge (when-not (empty? text)
                 {:text text})
               (when-not (empty? reminder)
                 {:reminder reminder}))))))

(defn- update-all-rules
  [rules]
  (map update-rule rules))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [card]
  (update-in card [:rules] update-all-rules))
