(ns loa.gatherer.language-index
  (:require (loa.gatherer (common :as common-))
            (loa.util (xml :as xml-))))

;;--------------------------------------------------
;; helpers

(defn- get-language [row]
  (-> (xml-/transform row
                      (xml-/search :td)
                      xml-/text)
      second
      .trim))

(defn- get-id [row]
  (-> (xml-/transform
       row
       (xml-/search (xml-/re-attr= :id #".*cardTitle")))
      first
      :attrs
      :href
      common-/href->gatherer-id))

(defn- get-rows [page]
  (xml-/xml->
   (xml-/from-html page)
   (xml-/search (xml-/re-attr= :class #".*cardItem.*"))
   xml-/node))

;;--------------------------------------------------
;; interface

(defn find-languages [page]
  (let [rows (get-rows page)]
    (reduce conj
            {}
            (map vector
                 (map get-language rows)
                 (map get-id rows)))))

(comment
  {"Spanish" 37862
   "Japanese" 739729
   "Rövarspråket" 345972})
