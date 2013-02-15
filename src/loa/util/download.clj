(ns loa.util.download
  (:require (clojure.java (io :as io-))
            (loa.program (config :as config-))))

(defn- escape-filename [s]
  (.replaceAll s "[:/?&% '.\"]" "_"))

(defn get-url-data
  "Fetches the url and saves to temp directory, returning the data. If
  the url has already been fetched will return the file content
  directly instead."
  [url]
  (let [filename (escape-filename (str url))
        file (config-/get-file :tmp filename)]
    (when-not (.exists file)
      (with-open [input (io-/reader url)
                  output (io-/writer file)]
        (io-/copy input output)))
    (slurp file)))
