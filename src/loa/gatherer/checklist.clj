(ns loa.gatherer.checklist
  (:require (loa.util (xml :as xml-))))

(defn- cleanup-href [href]
  (let [[_ id] (re-matches #".*?(\d+)$" href)]
    (try (Integer/parseInt id)
         (catch NumberFormatException e))))

(defn- make-data [item]
  [(-> item :content first)
   (cleanup-href (-> item :attrs :href))])

(defn find-cards
  "Returns a seq of [card-name gatherer-id]."
  [page]
  (let [parts (xml-/xml-> (xml-/from-html page)
                          (xml-/search :a
                                       (xml-/re-attr= :href #".*")
                                       (xml-/attr= :class "nameLink"))
                          xml-/node)]
    (doall
     (->> (map make-data parts)
          (set)
          (remove (comp nil? second))))))
