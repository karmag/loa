
(ns loa.persistance.text
  (:require [loa.transformation.text-transform :as text-transform]
            [clojure.contrib.io :as io]
            [clojure.contrib.string :as string]))

(defn ascii
  "Transform to ascii."
  [s]
  (when s
    (string/escape {\u00c6 "Ae"
                    \u2014 "-"
                    \u2019 "'"
                    \u00f6 "o"
                    \u00e0 "a"
                    \u00fb "u"
                    \u00ae ""
                    \u00e1 "a"
                    \u00e2 "a"
                    \u00e9 "e"
                    \u00ed "i"
                    \u00fa "u"}
                   s)))

;;-------------------------------------------------
;;
;;  Helpers
;;
(defn- get-count
  "Counts occurances of value in seq."
  [seq value]
  (count (filter #{value} seq)))

(defn- third
  [seq]
  (nth seq 2 nil))

;;-------------------------------------------------
;;
;;  Printing
;;
(defn- print-setinfo
  [setinfo]
  (doseq [set (sort #(compare (:code %1)
                              (:code %2))
                    (filter #(-> % :code .length (not= 0)) setinfo))]
    (println (format "%-3s  %-10s  %s"
                     (or (:code set) "?")
                     (or (:release-date set) "?")
                     (:name set)))))

(defn- print-card
  "Prints basic card info."
  [card]
  (println (ascii (:name card)))
  (when (:cost card)
    (println (:cost card)))
  (when (:loyalty card)
    (println (:loyalty card)))
  (println (text-transform/types (map ascii (:types card))))
  (when (:pow card)
    (println (str (:pow card) "/" (:tgh card))))
  (when (:hand card)
    (println (format "Hand %s, life %s" (:hand card) (:life card))))
  (when-not (empty? (:rules card))
    (doseq [rule (:rules card)]
      (println (str
                (or (ascii (:text rule)) "")
                (if (and (:text rule) (:reminder rule))
                  " " "")
                (if (:reminder rule)
                  (format "(%s)" (:reminder rule))
                  "")))))
  (doseq [multi (:multi card)]
    (println "----")
    (print-card multi)))

(defn- print-set-rarity
  "Prints the set & rarity. name-to-code and name-to-date are
  functions that take a set-name and returns the corresponding value."
  [sr-seq name-to-code name-to-date]
  (let [sr-seq (map #(vector (first %)
                             (second %)
                             (get-count sr-seq %))
                    (set sr-seq))]
    (->> sr-seq
         (sort #(compare (name-to-date (first %1))
                         (name-to-date (first %2))))
         (map #(str (name-to-code (first %))
                    " "
                    ;; TODO "shield of kaldra" requires the or expression
                    (-> % second (or :s) name .toUpperCase first)
                    (when (< 1 (third %))
                      (format " (x%d)" (third %)))))
         (interpose ", ")
         (reduce str)
         (#(when-not (zero? (count %))
             (println %))))))

(defn- process-data
  "Prints all cards."
  [{:keys [cards meta setinfo]}]
  (print-setinfo setinfo)
  (println)
  (let [cardmap (reduce #(assoc %1 (ascii (:name %2)) %2) nil cards)
        name->code (reduce #(assoc %1 (:name %2) (:code %2)) {} setinfo)
        name->date (reduce #(assoc %1 (:name %2) (:release-date %2)) {} setinfo)]
    (doseq [name (sort (keys cardmap))]
      (print-card (get cardmap name))
      (print-set-rarity (:set-rarity (get cardmap name))
                        name->code
                        name->date)
      (println))))

;;-------------------------------------------------
;;
;;  Interface
;;
(defn write-text
  "Writes the information as plain text to *out*."
  [cards meta setinfo]
  (process-data {:cards cards :meta meta :setinfo setinfo}))

(defn write-text-to
  "Write the information as plain text to the file(name)."
  [cards meta setinfo file]
  (io/with-out-writer file
    (write-text cards meta setinfo)))
