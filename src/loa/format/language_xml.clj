(ns loa.format.language-xml
  (:use (loa.util (xml :only (tag tag-attr)))))

(defn- language->xml [set-name lang-data]
  (tag-attr :set
            {:name set-name}
            (when (:name lang-data)
              (tag :name (:name lang-data)))
            (when (:typelist lang-data)
              (tag :type (:typelist lang-data)))
            (when-not (empty? (:rulelist lang-data))
              (apply tag
                     :rulelist
                     (map #(tag :rule %) (:rulelist lang-data))))
            (when (:flavor lang-data)
              (tag :flavor (:flavor lang-data)))
            (when (:multi lang-data)
              (let [lang-data (:multi lang-data)]
                (tag :multi
                     (when (:name lang-data)
                       (tag :name (:name lang-data)))
                     (when (:typelist lang-data)
                       (tag :type (:typelist lang-data)))
                     (when-not (empty? (:rulelist lang-data))
                       (apply tag
                              :rulelist
                              (map #(tag :rule %) (:rulelist lang-data))))
                     (when (:flavor lang-data)
                       (tag :flavor (:flavor lang-data))))))))

(defn- get-entries [card]
  (for [set-data (-> card :sets vals)
        :when (not-empty (:language set-data))
        [lang lang-data] (:language set-data)]
    {:lang lang
     :set (:set-name set-data)
     :xml (language->xml (:set-name set-data) lang-data)}))

(defn- sort-by-language [entry-coll]
  (reduce (fn [m {:keys [lang set xml]}]
            (if (get m lang)
              (update-in m [lang] conj xml)
              (assoc m lang [xml])))
          nil
          (sort-by :set entry-coll)))

(defn- refine-entries [card-name entry-map]
  (reduce (fn [m [lang xml-coll]]
            (assoc m lang (apply tag-attr
                                 :card
                                 {:name card-name}
                                 xml-coll)))
          nil
          entry-map))

(defn to-xml
  "Returns a map of language-name to xml-data. If no language data is
  available in the card nil is returned."
  [card]
  (->> card
       get-entries
       sort-by-language
       (refine-entries (:name card))))

;; <languagelist>
;;   <card name="Goblin Arsonist">
;;     <set code="APC">
;;       <name></name>
;;       <type></type>
;;       <rulelist>
;;         <rule></rule>
;;         ...
;;       </rulelist>
;;       <flavor></flavor>
;;       <multi>...</multi>
;;     </set>
;;     ...
;;   </card>
;; </languagelist>
