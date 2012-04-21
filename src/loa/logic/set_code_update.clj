
(ns loa.logic.set-code-update
  (:require (loa.program (config :as config_))
            (loa.util (util :as util_))))

(defn- make-card-finder
  "Returns a function that takes a setname and returns a gatherer-id
  for a card from that set."
  [card-coll]
  (let [mapping (->> card-coll
                     (mapcat :meta)
                     (map (fn [{:keys [gatherer-id set]}]
                            (when (and gatherer-id set)
                              {:set set :gatherer-id gatherer-id})))
                     (filter identity)
                     distinct)]
    (fn [name]
      (some (fn [{:keys [gatherer-id set]}]
              (when (= name set)
                gatherer-id))
            mapping))))

(defn- get-set-code
  [config gatherer-id]
  (let [url (config_/get-url config :card-details gatherer-id)
        page (util_/get-url-data config url)]
    (when-let [line
               (second
                (re-matches #"(?m)(?s).*Expansion:.*?<img ([^>]*?)>.*"
                            page))]
      (second
       (re-matches #".*src=\".*?set=([^&;]+).*\".*"
                   line)))))

(defn process
  [config set-coll card-coll]
  (let [get-code (comp (partial get-set-code config)
                       (make-card-finder card-coll))]
    (map (fn [set-data]
           (assoc set-data :code (or (get-code (:name set-data))
                                     (:code set-data))))
         set-coll)))
