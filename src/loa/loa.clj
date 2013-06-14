(ns loa.loa
  (:refer-clojure :exclude (write time))
  (:use
   (clojure (pprint :only (pprint)))
   loa.program.config
   loa.program.process
   loa.util.xml
   loa.program.work
   loa.util.download
   loa.util.util
   loa.format.card-xml
   loa.program.command
   ))

(defn- human-time [secs]
  (let [conv [["s" 1]
              ["m" 60]
              ["h" (* 60 60)]]]
    (loop [remaining secs
           convertions (reverse conv)
           result ""]
      (if (or (zero? remaining)
              (empty? convertions))
        (.trim result)
        (let [[unit power] (first convertions)
              whole (long (/ remaining power))]
          (recur (- remaining (* whole power))
                 (rest convertions)
                 (format "%s %d%s" result whole unit)))))))

(defn -main [& args]
  (let [start (System/currentTimeMillis)]
    (try
      (let [opts (parse-args args)]
        (pprint opts)
        (verify-options opts)
        (run! "." opts))
      (finally
        (shutdown)
        (shutdown-agents)))
    (println "Runtime:"
             (human-time
              (long (/ (- (System/currentTimeMillis) start)
                       1000)))))
  nil)

;; TODO add --full parameter
