(ns loa.format.meta-xml
  (:use (loa.util (xml :only (tag tag-attr)))))

(defn- get-instance [data]
  (tag :instance
       (when (:set-name data)
         (tag :set (:set-name data)))
       (when (:rarity data)
         (tag :rarity
              (-> data :rarity name first str .toUpperCase)))
       (when (:number data)
         (tag :number (:number data)))
       (when (:artist data)
         (tag :artist (:artist data)))
       (when-let [artist (-> data :meta :artist)]
         (tag :multi
              (tag :artist
                   artist)))))

(defn- get-all-instances [card]
  (map get-instance
       (->> card :sets vals (sort-by :set-name))))

(defn to-xml [card]
  (when-not (empty? (:sets card))
    (apply tag-attr
           :card
           {:name (:name card)}
           (get-all-instances card))))

;; <metalist>
;;   <card name="...">
;;     <instance>
;;       <set></set>
;;       <rarity></rarity>
;;       <number></number>
;;       <artist></artist>
;;       <multi>
;;         <artist></artist>
;;       </multi>
;;     </instance>
;;     ...
;;   </card>
;;   ...
;; </metalist>
