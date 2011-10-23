
(ns loa.format.card-xml
  "Transforms data into xml format."
  (:require (loa.util (data :as data_)
                      (xml :as xml_))))

(declare card-to-xml)

;;--------------------------------------------------
;;
;;  Helpers
;;
(defn- tag
  [name & values]
  (when-not (empty? values)
    (apply vector name {} values)))

(defn- rule-tag
  [rule]
  [:rule
   (merge {:no (:no rule)}
          (if (:reminder rule)
            {:reminder (:reminder rule)}
            {}))
   (:text rule)])

(defn- type-tag
  [type]
  (let [meta (cond (data_/super-type type) {:type "super"}
                   (data_/card-type type) {:type "card"}
                   :else {:type "sub"})]
    [:type meta type]))

(defn- get-card-tags
  "Returns a seq of tag for the card."
  [card]
  (filter identity
          [(tag :name (:name card)) ;; TODO repetition, ugly, redo
           (when (:cost card)
             (tag :cost (:cost card)))
           (when (:color card)
             (tag :color (:color card)))
           (when (:loyalty card)
             (tag :loyalty (:loyalty card)))
           (when-not (empty? (:types card))
             (apply tag
                    :typelist
                    (map #(type-tag %) (:types card))))
           (when (:pow card)
             (tag :pow (:pow card)))
           (when (:tgh card)
             (tag :tgh (:tgh card)))
           (when (:hand card)
             (tag :hand (:hand card)))
           (when (:life card)
             (tag :life (:life card)))
           (when-not (empty? (:rules card))
             (apply vector
                    :rulelist
                    {}
                    (map rule-tag (:rules card))))]))

(defn- get-multi-card
  "Returns the multi-part if any."
  [card]
  (when (:multi card)
    (map (fn [multi]
           (-> (card-to-xml multi)
               (assoc 0 :multi)
               (assoc 1 {:type (name (:multi-type multi))})))
         (:multi card))))

(defn- card-to-xml
  "Transforms a card into xml-data."
  [card]
  (apply tag
         :card
         (concat (get-card-tags card)
                 (get-multi-card card))))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn data
  "Returns the xml data."
  [card]
  (card-to-xml card))

(defn string
  "Returns the card data as xml-formatted string."
  [card]
  (xml_/pretty-string (data card)))
