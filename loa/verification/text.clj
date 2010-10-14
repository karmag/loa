
(ns loa.verification.text)

(defn verify
  [text-file]
  (let [bad (->> (slurp text-file)
                 (map int)
                 (remove #(< % 126)))]
    (when-not (-> bad count zero?)
      (throw (Exception.
              (apply str "Crappy letters in " text-file ": " bad))))))
