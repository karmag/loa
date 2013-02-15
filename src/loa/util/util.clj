(ns loa.util.util)

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

(defn verify-options
  [options]
  (let [verify-data
        {:sets {:f empty?
                :report "--sets can not be empty"}
         :cards {:f empty?
                 :report "--cards can not be empty"}}]
    (doseq [[key {:keys [f report]}] verify-data]
      (when (find options key)
        (when (f (get options key))
          (throw (Exception. report)))))))
