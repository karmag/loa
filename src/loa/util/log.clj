
(ns loa.util.log)

(def #^{:private true}
     writer-agent (agent nil))

(defn- writer-fn
  [_ level msg]
  (println (format "<%s> %s" (str level) (str msg))))

(defn- write-log
  [level msg]
  (send writer-agent writer-fn level msg))

(defn debug
  [msg]
  (write-log :debug msg))
