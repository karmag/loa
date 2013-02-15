(ns loa.gatherer.set-data
  (:require (loa.util (xml :as xml-))))

(defn find-set-names
  "Returns a seq of all set-names found on the page."
  [page]
  (let [names (xml-/xml-> (xml-/from-html page)
                          (xml-/search (xml-/re-attr= :name #"setAddText$"))
                          (loa.util.xml/search :option)
                          loa.util.xml/text)]
    (doall
     (remove (comp zero? count) names))))
