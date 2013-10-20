(ns loa.util.xml
  (:use (clojure.java (io :only (input-stream))))
  (:require clojure.data.xml
            clojure.data.zip
            clojure.data.zip.xml
            clojure.xml
            clojure.zip)
  (:import org.jsoup.Jsoup))

;;--------------------------------------------------
;; xml internal

(defmulti xml->str! (fn [state xml] (type xml)))

(defmethod xml->str! clojure.lang.PersistentVector
  [state xml]
  state)

(def ^:private escapes
  {"&nbsp;" " "
   ;;"&quot;" "\""
   ;;"&apos;" "'"
   ;;"&amp;" "&"
   "&copy;" "(c)"
   "&AElig;"   "Æ"      "&Aacute;"  "Á"      "&Acirc;"   "Â"
   "&Agrave;"  "À"      "&Aring;"   "Å"      "&Atilde;"  "Ã"
   "&Auml;"    "Ä"      "&Ccedil;"  "Ç"      "&ETH;"     "Ð"
   "&Eacute;"  "É"      "&Ecirc;"   "Ê"      "&Egrave;"  "È"
   "&Euml;"    "Ë"      "&Iacute;"  "Í"      "&Icirc;"   "Î"
   "&Igrave;"  "Ì"      "&Iuml;"    "Ï"      "&Ntilde;"  "Ñ"
   "&Oacute;"  "Ó"      "&Ocirc;"   "Ô"      "&Ograve;"  "Ò"
   "&Oslash;"  "Ø"      "&Otilde;"  "Õ"      "&Ouml;"    "Ö"
   "&THORN;"   "Þ"      "&Uacute;"  "Ú"      "&Ucirc;"   "Û"
   "&Ugrave;"  "Ù"      "&Uuml;"    "Ü"      "&Yacute;"  "Ý"
   "&aacute;"  "á"      "&acirc;"   "â"      "&aelig;"   "æ"
   "&agrave;"  "à"      "&aring;"   "å"      "&atilde;"  "ã"
   "&auml;"    "ä"      "&ccedil;"  "ç"      "&eacute;"  "é"
   "&ecirc;"   "ê"      "&egrave;"  "è"      "&eth;"     "ð"
   "&euml;"    "ë"      "&iacute;"  "í"      "&icirc;"   "î"
   "&igrave;"  "ì"      "&iuml;"    "ï"      "&ntilde;"  "ñ"
   "&oacute;"  "ó"      "&ocirc;"   "ô"      "&ograve;"  "ò"
   "&oslash;"  "ø"      "&otilde;"  "õ"      "&ouml;"    "ö"
   "&szlig;"   "ß"      "&thorn;"   "þ"      "&uacute;"  "ú"
   "&ucirc;"   "û"      "&ugrave;"  "ù"      "&uuml;"    "ü"
   "&yacute;"  "ý"      "&yuml;"    "ÿ"
   "&laquo;"   "«"      "&raquo;"   "»"
   "&acute;"   "´"      "&iexcl;"   "¡"      "&iquest;" "¿"
   "&middot;"  "·"      "&ordm;"    "º"      "&sup2;"   "²"})

(defn- unescape-text [text]
  (loop [text text escapes escapes]
    (if (empty? escapes)
      text
      (let [[k v] (first escapes)]
        (recur (.replaceAll text k v)
               (dissoc escapes k))))))

(defn- remove-scripts [text]
  (.replaceAll text "(?s)<script.*?</script>" "--KILLED_SCRIPT--"))

;;--------------------------------------------------
;; xml construction

(defn tag-attr [name attrs value & values]
  (apply clojure.data.xml/element
         name
         attrs
         (filter identity (cons value values))))

(defn tag [name value & values]
  (apply tag-attr name nil value values))

(defn to-str [xml]
  (let [transformer
        (doto (-> (javax.xml.transform.TransformerFactory/newInstance)
                  .newTransformer)
          (.setOutputProperty (javax.xml.transform.OutputKeys/INDENT) "yes")
          (.setOutputProperty (javax.xml.transform.OutputKeys/METHOD) "xml")
          (.setOutputProperty "{http://xml.apache.org/xslt}indent-amount" "1"))
        sw (java.io.StringWriter.)
        output (java.io.StringWriter.)
        _ (clojure.data.xml/emit xml sw)
        source (-> sw .toString java.io.StringReader.
                   javax.xml.transform.stream.StreamSource.)
        result (javax.xml.transform.stream.StreamResult. output)]
    (.transform transformer source result)
    (.toString output)))

(defn write [xml file]
  (with-open [fw (java.io.FileWriter. file)]
    (.write fw (to-str xml))))

;;--------------------------------------------------
;; xml querying

(def xml-> clojure.data.zip.xml/xml->)
(def xml1-> clojure.data.zip.xml/xml1->)
(def node clojure.zip/node)
(def text clojure.data.zip.xml/text)
(def attr= clojure.data.zip.xml/attr=)

(def from-xml clojure.zip/xml-zip)

(defn from-string [string]
  (-> string
      (.getBytes "UTF-8")
      input-stream
      clojure.xml/parse
      from-xml))

(defn from-html [html]
  (-> (Jsoup/parse html)
      (.select "html")
      first
      (.toString)
      remove-scripts
      unescape-text
      from-string))

(defn transform [xml & preds]
  (let [tail (if (#{node text} (last preds))
               nil
               [node])]
    (apply xml-> (from-xml xml) (concat preds tail))))

(defn search
  "Returns a query predicate that will traverse and return each
  location where every predicate given matches."
  [& preds]
  (fn [loc]
    (apply xml-> loc clojure.data.zip/descendants preds)))

(defn re-attr=
  "Returns a query predicate that matches nodes where the given
  regular expression is found in the attribute."
  [attrname re]
  (fn [loc]
    (when-let [attrvalue (clojure.data.zip.xml/attr loc attrname)]
      (when (re-find re attrvalue)
        loc))))
