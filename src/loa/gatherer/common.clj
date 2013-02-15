(ns loa.gatherer.common)

(defn href->gatherer-id
  "Parses a gatherer-id from the given href string."
  [href]
  (let [[_ id] (re-matches #".*?(\d+)$" href)]
    (try (Integer/parseInt id)
         (catch NumberFormatException e))))
