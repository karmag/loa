
(ns loa.util.log)

(def *logger* (atom println))

(defn set-logger!
  [f]
  (reset! *logger* f))

(defn log
  [level & msg]
  (@*logger* level (apply str msg)))

(defn debug [& msg] (apply log :debug msg))
(defn info  [& msg] (apply log :info  msg))
(defn warn  [& msg] (apply log :warn  msg))
(defn error [& msg] (apply log :error msg))
