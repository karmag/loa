
(ns loa.util.xml
  (:refer-clojure :exclude (xml-seq))
  (:require (clojure.contrib (lazy-xml :as lazy-xml_)
                             (prxml :as prxml_)))
  (import (java.io StringReader
                   StringWriter)))

(defn xml-seq
  "Returns a lazy sequence of xml tokens as per
  clojure.contrib.lazy-xml."
  [string]
  (-> string
      (.replace " & " " &amp; ")
      (.replace "&nbsp;" " ")
      StringReader.
      lazy-xml_/parse-seq))

(defn pretty-string
  "Transforms nested vectors into text as per clojure.contrib.prxml."
  [root & kvs]
  (let [opt (apply hash-map kvs)
        indent (:indent opt 1)
        decl (:xml-decl opt false)
        writer (:writer opt (StringWriter.))]
    (binding [prxml_/*prxml-indent* indent
              *out* writer] ;; TODO this hijacks *out*, must be another way
      (when decl
        (prxml_/prxml [:decl! {:version "1.0"}]))
      (prxml_/prxml root)
      (println))
    (.toString writer)))
