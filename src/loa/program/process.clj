(ns loa.program.process
  (:require (karmag.ants (core :as ants-))))

(def ants-instance
  (ants-/make-processor))

(def optimizer-instance
  (ants-/start-optimizer ants-instance :interval [1 :m]))

(def status
  "The current processing status listing the number of currently known
  jobs and their status."
  (ref {:main {:total 0 :complete 0}
        :set {:total 0 :complete 0}
        :card {:total 0 :complete 0}
        :meta {:total 0 :complete 0}
        :language {:total 0 :complete 0}
        :file {:total 0 :complete 0}}))

(def state
  "State is used by the various work-items to store data during
  processing."
  (ref {:cards {}
        :sets {}}))

(def ^:private failed (atom []))

;;--------------------------------------------------
;; work interface

(defn update-state! [f & args]
  (dosync
   (apply alter state f args)))

(defn update-status! [f & args]
  (dosync
   (apply alter status f args)))

(defn add-work! [name f]
  (ants-/add ants-instance
             (fn []
               (try (f)
                    (catch Throwable t
                      (swap! failed conj {:exception t
                                          :task {:f f, :name name}})
                      (println (str "Failed task: " name " - " t)))))))

(defn done? []
  (ants-/done? ants-instance))

(defn snapshot []
  (let [status (ants-/status ants-instance)]
    {:failed @failed
     :queue-size (- (:total-task-count status)
                    (:completed-task-count status))
     :workers-current (:active-threads status)
     :workers-max (:maximum-pool-size status)}))

(defn shutdown []
  (ants-/release-optimizer optimizer-instance)
  (ants-/shutdown ants-instance))
