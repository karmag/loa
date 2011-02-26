
(ns loa.acquisition.meta-handler
  (:require [clojure.contrib
             [lazy-xml :as lazy-xml]
             [io :as io]]
            [loa.util
             [log :as log]
             [util :as util]])
  (:import java.io.StringReader))

(defn create-meta-reader
  "Returns the meta data as a Reader."
  [meta-search-url setname]
  (util/get-uri-reader
   (format meta-search-url (.replaceAll setname " " "%20"))))

(defn- get-meta-key
  [s]
  (if (= s "nameLink")
    :name
    (keyword s)))

(defn- get-multiverse-id
  [xml-node]
  (->> (get-in xml-node [:attrs :href])
       (re-matches #".*=(\d+)")
       second
       Integer/parseInt))

(defn parse-meta-part
  [xml-part]
  (first
   (reduce (fn [[data key] part]
             (cond
              (and (= (:type part) :start-element) ;; elem
                   (= (:name part) :td))
              [data (get-meta-key (-> part :attrs :class))]
              (= (:type part) :characters) ;; data
              [(assoc data key (:str part)) nil]
              (and (= key :name) (= :a (:name part))) ;; multiverse id
              [(assoc data :gatherer-id (get-multiverse-id part)) key]
              :else ;; default
              [data key]))
           [nil key]
           xml-part)))

(defn parse-raw-meta
  [reader]
  (let [metas (re-seq #"<tr class=\"cardItem\">.*?</tr>"
                      (.replaceAll (io/slurp* reader) ;; Ugly hax
                                   "R&D" "R&amp;D"))]
    (->> metas
         (map #(.replaceAll % " & " "&amp;"))
         (map #(lazy-xml/parse-seq (StringReader. %)))
         (map parse-meta-part))))

;;-------------------------------------------------
;;
;;  Interface
;;
(defn get-meta
  "Get meta-data for the given set."
  [meta-search-url setname]
  (log/debug (str "Getting meta for set [" setname "]"))
  (doall
   (-> (create-meta-reader meta-search-url setname)
       parse-raw-meta)))
