
(ns loa.program.file-writer
  "Helper for writing data to disk."
  (:require (loa.format (card-text :as card-text_)
                        (card-xml :as card-xml_)
                        (meta-xml :as meta-xml_)
                        (setinfo-xml :as setinfo-xml_))
            (loa.program (config :as config_))
            (loa.util (util :as util_)
                      (xml :as xml_)))
  (:import java.io.FileWriter))

(defn make-writer
  [config type filename]
  (FileWriter.
   (config_/get-file config type filename)))

(defn card-list
  [writer card-coll]
  (let [card-coll (sort-by :name card-coll)]
    (xml_/pretty-string [:cardlist {} (map card-xml_/data card-coll)]
                        :xml-decl true
                        :writer writer))
  (.flush writer))

(defn meta-list
  [writer card-coll set-coll]
  (let [card-coll (sort-by :name card-coll)]
    (xml_/pretty-string (meta-xml_/data card-coll set-coll)
                        :xml-decl true
                        :writer writer))
  (.flush writer))

(defn setinfo
  [writer set-coll]
  (let [set-coll (sort-by :name set-coll)]
    (xml_/pretty-string (setinfo-xml_/data set-coll)
                        :xml-decl true
                        :writer writer))
  (.flush writer))

(defn card-list-text
  [writer card-coll set-coll]
  (let [card-coll (->>  card-coll
                        (map #(update-in % [:name] util_/ascii-string))
                        (sort-by :name))]
    (.write writer (card-text_/set-info set-coll))
    (doseq [text (card-text_/data card-coll set-coll)]
      (.write writer (str \newline \newline))
      (.write writer text)))
  (.flush writer))
