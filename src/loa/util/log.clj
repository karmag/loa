
(ns loa.util.log)

(defn log-fn
  [level & msg]
  (println (format "<%5s> %s"
                   (name level)
                   (reduce str
                           (interpose " " msg)))))

(def throttle (agent log-fn))

(defn- perform-log
  [logger & args]
  (apply logger args)
  logger)

(defn log
  [level & msg]
  (apply send throttle perform-log level msg)
  (await throttle)
  nil)

(defn debug [& msg] (apply log :debug msg))
(defn info  [& msg] (apply log :info  msg))
(defn warn  [& msg] (apply log :warn  msg))
(defn error [& msg] (apply log :error msg))
