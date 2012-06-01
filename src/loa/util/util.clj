
(ns loa.util.util
  (:require (clojure.contrib (io :as io_))
            (clojure.contrib.http (agent :as http-agent_))
            (loa.util (data :as data_)
                      (log :as log_)))
  (:import java.io.File))

(defn- escape-filename
  "Makes the string filename-friendly."
  [s]
  (.replaceAll s "[:/?&% '.\"]" "_"))

(defn get-url-data
  "Fetches the url and saves to temp directory. If the url has already
  been fetched will return the file content instead."
  [config url]
  (let [file (File. (get-in config [:path :tmp])
                    (escape-filename url))]
    (when-not (.exists file)
      (log_/debug (str "Downloading: " url))
      (io_/spit file
                (-> url http-agent_/http-agent http-agent_/string)))
    (io_/slurp* file)))

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

(defn ascii-string
  "Transforms certain non-characters to an ascii representation."
  [string]
  (when string
    (->> (map data_/ascii-mapping string)
         (map (fn [orig change] (or change orig))
              string)
         (apply str))))
