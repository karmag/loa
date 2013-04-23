(ns loa.format.card-xml
  (:use (loa.util (xml :only (tag tag-attr))))
  (:require (loa.util (magic :as magic-))))

(declare card-to-xml)

(defn- typelist [card]
  (apply tag
         :typelist
         (map #(tag-attr :type {:type (magic-/find-type %)} %)
              (:typelist card))))

(defn- rulelist [card]
  (when-not (empty? (:rulelist card))
    (apply tag
           :rulelist
           (map #(tag-attr :rule (merge {:no (:number %)}
                                        (when (:reminder %)
                                          {:reminder (:reminder %)}))
                           (:text %))
                (:rulelist card)))))

(defn- find-multi-type [card]
  (let [rules (.toLowerCase (apply str (map :text (:rulelist card))))]
    (cond (.contains rules "transform") "transform"
          (.contains rules "flip") "flip"
          :else "double")))

(defn- multi [card]
  (when (:multi card)
    (card-to-xml (:multi card)
                 :multi
                 {:type (find-multi-type card)})))

(defn- part [card key & [card-key]]
  (when-let [value (or (get card key)
                       (get card card-key))]
    (tag key value)))

(defn- card-to-xml [card root-tag attrs]
  (tag-attr root-tag
            attrs
            (part card :name)
            (part card :cost)
            (part card :color :color-indicator)
            (part card :loyalty)
            (typelist card)
            (part card :pow)
            (part card :tgh)
            (part card :hand)
            (part card :life)
            (rulelist card)
            (multi card)))

(defn to-xml [card]
  (card-to-xml card :card nil))

;; <cardlist>
;;   <card>
;;     <name></name>
;;     <cost></cost>
;;     <color></color>
;;     <loyalty></loyalty>
;;     <typelist>
;;       <type type=""></type>
;;       ...
;;     </typelist>
;;     <pow></pow>
;;     <tgh></tgh>
;;     <hand></hand>
;;     <life></life>
;;     <rulelist>
;;       <rule no="" reminder=""></rule>
;;       ...
;;     </rulelist>
;;     <multi type="">
;;       ...
;;     </multi>
;;   </card>
;;   ...
;; </cardlist>
