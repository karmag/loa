(ns loa.format.set-info-xml
  (:use (loa.util (xml :only (tag)))))

(defn to-xml [set-info]
  (apply tag
         :set
         (map #(when-not (empty? (%1 set-info))
                 (tag %1 (%1 set-info)))
              (sort [:name :block :code :release-date]))))

;; <set>
;;  <block>Urza</block>
;;  <code>UZ</code>
;;  <name>Urza's Saga</name>
;;  <release-date>1998-10-12</release-date>
;; </set>
