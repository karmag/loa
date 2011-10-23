
(ns loa.logic.card-meta-merge)

(defn- merge-data
  [card meta]
  (let [card-meta (:meta card)
        result
        (reduce (fn [acc item]
                  (if (and (= (:set meta) (:set item))
                           (not (:number item))
                           (not (:has-set acc)))
                    (-> acc
                        (update-in [:output] conj (merge meta item))
                        (assoc :has-set true))
                    (update-in acc [:output] conj item)))
                {:output nil :has-set false}
                card-meta)]
    (when-not (:has-set result)
      ;; TODO should report
      )
    (assoc card :meta (:output result))))

(defn- add-to-card
  [card meta]
  (merge-data card meta))

(defn- add-to-multi
  [card meta]
  (let [multi (-> card :multi first)]
    (assoc card :multi [(merge-data multi meta)])))

(defn- find-multi-name
  "Returns the name of the master card if the given name matches a
  multi-part."
  [cardmap name]
  (some (fn [card]
          (when (= name (-> card :multi first :name))
            (:name card)))
        (vals cardmap)))

(defn- apply-meta
  "Apply the meta to its matching card."
  [cardmap meta]
  (if (get cardmap (:name meta))
    (update-in cardmap [(:name meta)] add-to-card meta)
    (if-let [name (find-multi-name cardmap (:name meta))]
      (update-in cardmap [name] add-to-multi meta)
      cardmap ;; TODO should report
      )))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [card-list meta-list]
  (let [cardmap (reduce #(assoc %1 (:name %2) %2) nil card-list)]
    (vals
     (reduce apply-meta cardmap meta-list))))
