
(ns loa.loa
  (:use clojure.pprint
        clojure.contrib.trace)
  (:require (loa.program (command :as command_))
            (loa.format (card-xml :as card-xml_))))

(defn get-sets
  [config & limit]
  (let [limit (if (empty? limit)
                identity
                (fn [set]
                  (some #(.contains (.toLowerCase (:name set))
                                    (.toLowerCase %))
                        limit)))]
    (filter limit
            (command_/get-sets config))))

(defn filter-cards
  [cards & names]
  (if (empty? names)
    cards
    (filter (fn [card]
              (some (fn [name]
                      (.contains (.toLowerCase (if (string? (:name card))
                                                 (:name card)
                                                 (first (:name card))))
                                 (.toLowerCase name)))
                    names))
            cards)))

(defn- realize-cards
  [cards]
  (dorun
   (pmap #(dorun (:meta %))
         cards)))

;;--------------------------------------------------
;; Processing

(defn- run-cards
  [config]
  (let [opt (:opt config)
        sets (apply get-sets config (:set opt))
        cards (->> sets
                   (mapcat (partial command_/get-cards config))
                   (#(apply filter-cards % (:card opt)))
                   (command_/transform :loa.program.command/all)
                   (#(apply filter-cards % (:card opt)))
                   )
        cards (if (:meta opt)
                (let [meta (mapcat (partial command_/get-meta config) sets)]
                  (command_/add-metadata cards meta))
                cards)
        cards (map (fn [card]
                     (let [details (command_/get-card-details config card)]
                       (command_/add-card-details card details)))
                   cards)
        sets (command_/fix-set-codes config sets cards)]
    (realize-cards cards)
    (when (:debug opt)
      (pprint sets)
      (pprint cards))
    (when (:write opt)
      (command_/write-cards config cards)
      (when (:meta opt)
        (command_/write-meta config cards sets))
      (command_/write-setinfo config sets)
      (command_/write-cards-text config cards sets)
      (command_/create-package config))))

;;--------------------------------------------------
;; Main

(defn- print-config
  [config]
  (println "Paths")
  (doseq [key [:indata :tmp :xml :text :zip]]
    (println (format "%10s :: %s" (name key) (-> config :path key .getPath))))
  (println "Arguments")
  (let [opt (:opt config)]
    (doseq [[k v] [["Write" (if (:write opt) true false)]
                   ["Meta" (if (:meta opt) true false)]
                   ["Debug" (if (:debug opt) true false)]
                   ["Sets" (or (:set opt) "-")]
                   ["Cards" (or (:card opt) "-")]]]
      (println (format "%10s :: %s" k (str v))))))

(defn- parse-args
  ([args]
     (parse-args args nil nil))
  ([args opt key]
     (if (empty? args)
       opt
       (let [[item & more] args
             item-key (keyword (.replaceAll item "^-+" ""))]
         (if (.startsWith item "-")
           (recur more (assoc opt item-key []) item-key)
           (recur more (update-in opt [key] conj item) key))))))

(defn -main
  [& args]
  (try
    (let [opt (parse-args args)
          config (command_/make-config "." opt)]
      (print-config config)
      (command_/setup-paths config)
      (run-cards config))
    (finally
      (shutdown-agents))))
