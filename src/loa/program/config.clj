
(ns loa.program.config
  (:import java.io.File))

(defn- make-file
  [& parts]
  (reduce (fn [f s]
            (File. f s))
          (File. (first parts))
          (rest parts)))

(defn create-config
  [base-dir]
  (let [base-dir (if base-dir base-dir ".")
        base-dir (-> base-dir File. .getCanonicalPath)]
    {:path {:tmp    (make-file base-dir "data" "tmp-download")
            :xml    (make-file base-dir "data" "xml")
            :text   (make-file base-dir "data" "text")
            :indata (make-file base-dir "data" "indata")
            :zip    (make-file base-dir "data" "zip")}
     :url {:main "http://gatherer.wizards.com/Pages/Default.aspx"
           :set "http://gatherer.wizards.com/Pages/Search/Default.aspx?output=spoiler&method=text&set=[%%22%s%%22]&special=true"
           :checklist "http://gatherer.wizards.com/Pages/Search/Default.aspx?output=checklist&set=[%%22%s%%22]&special=true"
           :card-details "http://gatherer.wizards.com/Pages/Card/Details.aspx?multiverseid=%d"}}))

(defn get-file
  [config path filename]
  (File. (get-in config [:path path])
         filename))

(defn get-url
  [config url & args]
  (let [url (get-in config [:url url])]
    (-> (apply format url args)
        (.replaceAll " " "%20"))))
