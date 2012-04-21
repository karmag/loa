
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

(defn- run-cards
  [opt config]
  (let [sets (apply get-sets config (:set opt))
        cards (->> sets
                   (mapcat (partial command_/get-cards config))
                   (#(apply filter-cards % (:card opt)))
                   (command_/transform :loa.program.command/all)
                   (#(apply filter-cards % (:card opt)))
                   )
        meta (mapcat (partial command_/get-meta config) sets)
        cards (command_/add-metadata cards meta)
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
      (command_/write-meta config cards sets)
      (command_/write-setinfo config sets)
      (command_/write-cards-text config cards sets)
      (command_/create-package config))))

(defn parse-args
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
    (let [config (command_/make-config ".")
          opt (parse-args args)]
      (command_/setup-paths config)
      (run-cards opt config))
    (finally
     (shutdown-agents))))
