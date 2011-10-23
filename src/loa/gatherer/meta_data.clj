
(ns loa.gatherer.meta-data
  (:require (loa.util (xml :as xml_))))

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

(defn- parse-meta-part
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

(defn- get-parts
  [page]
  (let [parts (re-seq #"<tr class=\"cardItem\">.*?</tr>" page)]
    (map xml_/xml-seq parts)))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn get-meta
  [page]
  (map parse-meta-part (get-parts page)))
