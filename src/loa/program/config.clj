(ns loa.program.config
  (:require (clojure.java (io :as io-)))
  (:import java.io.File))

(def ^:dynamic *data*)

(defn- make-file
  "Treats all parts as directories (or possibly file for the final
  item) and constructs a File."
  [& parts]
  (reduce (fn [f s]
            (File. f s))
          (File. (first parts))
          (rest parts)))

(defn- create-config [base-dir options]
  (let [base-dir (if base-dir base-dir ".")
        base-dir (-> base-dir File. .getCanonicalPath)]
    {:options options
     :path {:tmp    (make-file base-dir "data" "tmp-download")
            :xml    (make-file base-dir "data" "xml")
            :indata (make-file base-dir "data" "indata")
            :zip    (make-file base-dir "data" "zip")}
     :url {:main "http://gatherer.wizards.com/Pages/Default.aspx"
           :set "http://gatherer.wizards.com/Pages/Search/Default.aspx?output=spoiler&method=text&set=[%%22%s%%22]&special=true"
           :checklist "http://gatherer.wizards.com/Pages/Search/Default.aspx?output=checklist&set=[%%22%s%%22]&special=true"
           :card-details "http://gatherer.wizards.com/Pages/Card/Details.aspx?multiverseid=%d"
           :language-index "http://gatherer.wizards.com/Pages/Card/Languages.aspx?multiverseid=%d"
           :print-details "http://gatherer.wizards.com/Pages/Card/Details.aspx?printed=true&multiverseid=%d"}}))

(defn setup-config [base-dir options]
  (alter-var-root #'*data*
                  (constantly (create-config base-dir options))))

(defn create-dirs! []
  (doseq [file (-> *data* :path vals)]
    (.mkdirs file)))

(defn get-option [key]
  (get-in *data* [:options key]))

(defn get-file
  "Returns the corresponding path as a File."
  [path-type filename]
  (File. (get-in *data* [:path path-type])
         filename))

(defn get-url
  "Returns the corresponding adress as an URL. If the url definition
  contains parameters args are required to fill those spots."
  [url-type & args]
  (let [url (get-in *data* [:url url-type])]
    (-> (apply format url args)
        (.replaceAll " " "%20")
        io-/as-url)))
