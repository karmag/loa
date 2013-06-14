(ns loa.program.command
  (:use clojure.pprint)
  (:require (loa.format (card-xml :as card-xml-))
            (loa.indata (set-info :as set-info-))
            (loa.program (config :as config-)
                         (process :as process-)
                         (work :as work-))
            (loa.util (xml :as xml-))
            (loa.validation (card-validation :as card-validation-))))

(defn- progress-report! []
  (let [stat (fn [key]
               (format "%s: %d / %d"
                       (name key)
                       (get-in @process-/status [key :complete])
                       (get-in @process-/status [key :total])))
        snapshot (process-/snapshot)
        q (format "Queue: %d" (:queue-size snapshot))
        failed (format "Failed: %d" (count (:failed snapshot)))
        workers (format "Workers: %d / %d"
                        (:workers-current snapshot)
                        (:workers-max snapshot))]
    (->> (map stat [:main :set :card :meta :language :file])
         (concat [q failed workers])
         (interpose \tab)
         (reduce str)
         println)))

(defn- fail-report! []
  (doseq [{:keys [exception task]} (:failed (process-/snapshot))]
    (println (format "Failed task: %s" (:name task)))
    (.printStackTrace exception (java.io.PrintWriter. *out*)))
  (flush))

(defn- await-completion []
  (while (not (process-/done?))
    (progress-report!)
    (Thread/sleep 1500)))

(defn- set-data-setup! []
  (let [string (slurp (config-/get-file :indata "set-info.csv"))
        set-data (set-info-/get-set-info string)]
    (dosync
     (alter process-/state assoc-in [:sets] set-data))))

(defn- validate! []
  (println "Validation report:")
  (doseq [card (-> @process-/state :cards vals)]
    (let [errors (card-validation-/validate card)]
      (when-not (empty? errors)
        (println " " (:name card))
        (doseq [err errors]
          (println "   -" err))))))

(defn setup! [base-dir options]
  (config-/setup-config base-dir options)
  (config-/create-dirs!)
  (set-data-setup!))

(defn get-data! []
  (process-/add-work! "Data download setup" work-/data-download)
  (await-completion))

(defn package! []
  (process-/add-work! "File creation setup" work-/file-creation)
  (await-completion)
  (process-/add-work! "Zip packaging setup" work-/zipit)
  (await-completion))

(defn post-report! []
  (fail-report!))

(defn print-debug! []
  (pprint (:cards @process-/state))
  (flush)
  (->> @process-/state
       :cards vals
       (map (comp println
                  xml-/to-str
                  card-xml-/to-xml))
       dorun))

(defn run! [base-dir options]
  (setup! base-dir options)
  (get-data!)
  (when (:package options)
    (package!))
  (progress-report!)
  (post-report!)
  (when (:debug options)
    (print-debug!))
  (when (:validate options)
    (validate!)))
