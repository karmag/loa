
(ns loa.util.zip
  (:require [clojure.java.io :as io])
  (:import java.io.FileOutputStream
           (java.util.zip ZipEntry
                          ZipOutputStream)))

(defn create
  "Creates a zipfile. The infiles are (file, entry-name) pairs to put
  into the zipfile."
  [outfile infiles]
  (with-open [fos (FileOutputStream. outfile)
              out (ZipOutputStream. fos)]
    (doseq [[file entry-name] infiles]
      (println file "->" entry-name)
      (.putNextEntry out (ZipEntry. entry-name))
      (io/copy file out))))
