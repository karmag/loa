
(ns loa.util.util
  (:require [clojure.contrib.http.agent :as http-agent]
            [clojure.contrib.prxml :as prxml]
            [clojure.contrib.io :as io]
            [clojure.contrib.lazy-xml :as lazy-xml]
            [loa.program.config :as config]
            [loa.util.log :as log])
  (:import (java.io File
                    FileInputStream
                    InputStreamReader
                    StringReader)))

(defn- escape-filename
  "Makes the string filename-friendly."
  [s]
  (.replaceAll s "[:/?&% '.\"]" "_"))

(defn lazy-split
  "Lazily split the coll at the predicate. Always include the first
  item in the coll."
  [coll pred]
  (when-not (empty? coll)
    (let [part (conj (take-while (complement pred) (rest coll))
                     (first coll))]
      (lazy-cat [part]
                (lazy-split (nthnext coll (count part))
                            pred)))))

(defn get-url-data
  "Fetches the url and saves to temp directory. If the url has already
  been fetched will return the file content instead."
  [url]
  (let [file (File. (get-in config/*config* [:path :tmp])
                    (escape-filename url))]
    (when-not (.exists file)
      (log/debug (str "Downloading: " url))
      (io/spit file
               (-> url http-agent/http-agent http-agent/string)))
    (io/slurp* file)))

(defn get-uri-reader
  "Returns a Reader."
  [uri]
  (let [file (File. (get-in config/*config* [:path :tmp])
                    (escape-filename uri))]
    (when-not (.exists file)
      (log/debug (str "Downloading: " uri))
      (io/spit file
               (-> uri http-agent/http-agent http-agent/string)))
    (-> file FileInputStream. (InputStreamReader. "UTF-8"))))

;;-------------------------------------------------
;;
;;  Generic file IO
;;
(defn write-data
  [file data]
  (binding [*print-dup* true]
    (io/with-out-writer file
      (println data))))

(defn read-data
  [file]
  (io/with-in-reader file
    (read)))

;;-------------------------------------------------
;;
;;  XML
;;
(defn write-xml
  "Writes the data as xml to *out*."
  [xml-data & kvs]
  (let [opt (apply hash-map kvs)
        indent (:indent opt 1)]
    (binding [prxml/*prxml-indent* indent]
      (prxml/prxml [:decl! {:version "1.0"}])
      (prxml/prxml xml-data)
      (println))))

(defn xml-seq
  "Transforms a string to a lazy sequence of elements as per
  clojure.contrib.lazy-xml."
  [string]
  (lazy-xml/parse-seq (StringReader. string)))
