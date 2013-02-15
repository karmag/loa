(ns loa.program.process
  (:require (ants (core :as ants-))))

(def ants-instance (ants-/create
                    :max-workers 8)) ;; TODO cli option

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

;;--------------------------------------------------
;; work interface

(defn update-state! [f & args]
  (dosync
   (apply alter state f args)))

(defn update-status! [f & args]
  (dosync
   (apply alter status f args)))

(defn add-work! [name f]
  (ants-/add-task! ants-instance f :name name))

(defn done? []
  (ants-/finished? ants-instance))

(defn snapshot []
  (ants-/snapshot ants-instance))
