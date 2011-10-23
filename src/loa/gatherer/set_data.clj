
(ns loa.gatherer.set-data
  "Retreives and parses set data from gatherer.")

;;-------------------------------------------------
;;
;;  Interface
;;
(defn parse-names
  "Returns a seq of all the set-names found on the page. This page
  should be the index page for gatherer."
  [page]
  (->> page
       (re-matches
        #"(?m)(?s).*Filter Card Set:.*?<select.*?>(.*?)</select>.*")
       second
       (re-seq #">(.*?)<")
       (map second)
       (remove #(zero? (.length %)))
       (map #(.replaceAll % "&quot;" "\""))))
