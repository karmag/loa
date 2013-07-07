(ns loa.program.work
  (:require (loa.cleanup (card-adjustment :as card-adjustment-)
                         (card-cleanup :as card-cleanup-)
                         (extra :as extra-))
            (loa.format (card-xml :as card-xml-)
                        (language-xml :as language-xml-)
                        (meta-xml :as meta-xml-)
                        (set-info-xml :as set-info-xml-))
            (loa.gatherer (card-details :as card-details-)
                          (checklist :as checklist-)
                          (language-index :as language-index-)
                          (set-data :as set-data-))
            (loa.program (config :as config-)
                         (process :as process-))
            (loa.util (download :as download-)
                      (magic :as magic-)
                      (xml :as xml-)
                      (zip :as zip-)))
  (:import java.io.File
           java.text.SimpleDateFormat))

(declare process-card-meta process-language-index)

;;--------------------------------------------------
;; helper functions

(defn- inc-status! [number key & keys]
  (process-/update-status! update-in
                           (vec (cons key keys))
                           + number))

(defn- card-processed? [name]
  (get-in @process-/state [:cards name]))

(defn filter-set-names [sets]
  (let [texts (map #(.toLowerCase %) (config-/get-option :sets))]
    (if (empty? texts)
      sets
      (filter (fn [set]
                (some #(.contains (.toLowerCase set) %) texts))
              sets))))

(defn- filter-card-names [cards]
  (let [cards (remove #(magic-/token-name
                        (or (:name %) (first %)))
                      cards)
        texts (map #(.toLowerCase %) (config-/get-option :cards))]
    (if (empty? texts)
      cards
      (filter (fn [[cardname _]]
                (some #(.contains (.toLowerCase cardname) %) texts))
              cards))))

(defn- process-set-codes [page]
  "Locates set-codes in the given page and updates the set-data if
  needed."
  (doseq [[set-name set-code]
          (card-details-/find-set-codes page)]
    (when-not (get-in @process-/state [:sets set-name :code])
      (dosync
       (alter process-/state
              assoc-in [:sets set-name :code] set-code)))))

(defn- multi-name? [card name]
  (= name (-> card :multi :name)))

(defn- launch-process-card-meta [name id]
  (when (config-/get-option :meta)
    (inc-status! 1 :meta :total)
    (process-/add-work! (format "Card meta setup '%s' (%d)" name id)
                        #(process-card-meta name))))

(defn- launch-process-card-language [name id]
  (when (config-/get-option :language)
    (inc-status! 1 :language :total)
    (process-/add-work! (format "Language index '%s' (%d)" name id)
                        #(process-language-index name id))))

(defn- get-maybe-double-card
  "Downloads and does basic parsing of the card with id. If it is a
  double card the second half will be downloaded as well and merged
  with base card. This function returns [real-name, page, card]."
  [name id]
  (let [page (download-/get-url-data
              (config-/get-url :card-details id))
        double-info (card-details-/find-double-card-information name page)
        card (card-details-/find-details page)
        multi (when double-info
                (-> (config-/get-url :double-card-details
                                     (-> double-info :link :part)
                                     (-> double-info :link :gatherer-id))
                    download-/get-url-data
                    card-details-/find-details))]
    [(or (:fst double-info) name)
     page
     (if multi
       (if (= (:fst double-info) name)
         (assoc card :multi multi)
         (assoc multi :multi card))
       card)]))

;;--------------------------------------------------
;; download functions

(defn- process-card-printed [id save-path]
  (let [details (card-details-/find-print-details
                 (download-/get-url-data
                  (config-/get-url :print-details id)))
        details (card-cleanup-/cleanup-print details)]
    (dosync
     (alter process-/state assoc-in save-path details)))
  (inc-status! 1 :language :complete))

(defn- process-language-index [name id]
  (let [langs (language-index-/find-languages
               (download-/get-url-data
                (config-/get-url :language-index id)))
        langs (assoc langs "English" id)]
    (doseq [[lang lang-id] langs]
      (inc-status! 1 :language :total)
      (process-/add-work! (format "Printed card '%s' (%d) - %s" name id lang)
                          #(process-card-printed
                            lang-id
                            [:cards name :sets id :language lang]))))
  (inc-status! 1 :language :complete))

(defn- process-card-meta-single [name id]
  (let [[name _ details] (get-maybe-double-card name id)
        details (card-cleanup-/cleanup details)
        set-data (get-in details [:sets id])]
    (dosync
     (alter process-/state update-in [:cards name :sets id]
            merge set-data)))
  (inc-status! 1 :meta :complete))

(defn- process-card-meta [name]
  (doseq [[id data]
          (get-in @process-/state [:cards name :sets])]
    (when-not (:number data)
      (inc-status! 1 :meta :total)
      (process-/add-work! (format "Card meta '%s' (%d)" name id)
                          #(process-card-meta-single name id))))
  (inc-status! 1 :meta :complete))

(defn- process-card [name id]
  (let [processed? (card-processed? name)
        [name page card] (or (when-not processed?
                               (get-maybe-double-card name id))
                             [name nil nil])
        card (when-not processed?
               (-> card card-cleanup-/cleanup card-adjustment-/fix))]
    (when-not (multi-name? card name)
      (when-not processed?
        (dosync
         (alter process-/state update-in [:cards]
                assoc name card))
        (process-set-codes page)
        (launch-process-card-meta name id))
      (launch-process-card-language name id))
    (inc-status! 1 :card :complete)))

(defn- process-set [name]
  (let [cards (checklist-/find-cards
               (download-/get-url-data
                (config-/get-url :checklist name)))
        cards (filter-card-names cards)]
    (inc-status! (count cards) :card :total)
    (doseq [[name id] cards]
      (process-/add-work! (format "Card detail '%s' (%d)" name id)
                          #(process-card name id)))
    (inc-status! 1 :set :complete)))

(defn- process-set-names []
  (let [names (set-data-/find-set-names
               (download-/get-url-data
                (config-/get-url :main)))
        names (remove #{"Unglued" "Unhinged"} names)
        names (filter-set-names names)]
    (inc-status! (count names) :set :total)
    (doseq [name names]
      (process-/add-work! (format "Set '%s'" name)
                          #(process-set name)))
    (inc-status! 1 :main :complete)))

;;--------------------------------------------------
;; write functions

(defn- write-card-xml []
  (let [cards (->> @process-/state :cards vals (sort-by :name))
        xml (xml-/tag :cardlist (map card-xml-/to-xml cards))
        file (config-/get-file :xml "cards.xml")]
    (xml-/write xml file))
  (inc-status! 1 :file :complete))

(defn- write-meta-xml []
  (let [cards (->> @process-/state :cards vals (sort-by :name))
        xml (xml-/tag :metalist (map meta-xml-/to-xml cards))
        file (config-/get-file :xml "meta.xml")]
    (xml-/write xml file))
  (inc-status! 1 :file :complete))

(defn- write-set-info-xml []
  (let [set-data (->> @process-/state :sets vals (sort-by :name))
        xml (xml-/tag :setlist (map set-info-xml-/to-xml set-data))
        file (config-/get-file :xml "setinfo.xml")]
    (xml-/write xml file))
  (inc-status! 1 :file :complete))

(defn- write-language-xml []
  (let [cards (->> @process-/state :cards vals (sort-by :name))
        lang-map
        (reduce (fn [all card]
                  (reduce (fn [m [lang xml]]
                            (if (get m lang)
                              (update-in m [lang] conj xml)
                              (assoc m lang [xml])))
                          all
                          (language-xml-/to-xml card)))
                nil
                cards)]
    (doseq [[lang xml-coll] lang-map]
      (let [xml (apply xml-/tag-attr :languagelist {:language lang} xml-coll)
            file (config-/get-file :xml
                                   (-> (format "language_%s.xml" lang)
                                       .toLowerCase
                                       (.replaceAll " " "_")))]
        (xml-/write xml file))))
  (inc-status! 1 :file :complete))

(defn- zip-xml-files []
  (let [zipname (format "mtg-data-%s.zip"
                        (.format (SimpleDateFormat. "yyyy-MM-dd")
                                 (System/currentTimeMillis)))
        files (->> (config-/get-file :xml "")
                   .listFiles
                   (map (memfn getCanonicalPath))
                   (filter #(.endsWith % ".xml")))]
    (zip-/create
     (config-/get-file :zip zipname)
     (cons [(config-/get-file :indata "format.txt")
            "mtg-data/format.txt"]
           (map (fn [path]
                  [(File. path)
                   (->> (File. path) .getName (str "mtg-data/"))])
                files))))
  (inc-status! 1 :file :complete))

;;--------------------------------------------------
;; other work

(defn inject-extra-cards []
  (let [cards (extra-/get-extra-cards)
        keep? (->> cards
                   (map #(vector (:name %) 0))
                   filter-card-names
                   (map first)
                   set)
        cards (reduce (fn [m card]
                        (assoc m (:name card) card))
                      nil
                      (filter (comp keep? :name) cards))]
    (when-not (empty? cards)
      (dosync
       (alter process-/state update-in [:cards]
              merge cards))))
  (inc-status! 1 :card :complete))

(defn- cleanup-files
  "Removes xml files that have been generated by this or previous
  runs."
  []
  (let [files (->> (config-/get-file :xml "")
                   .listFiles
                   (map (memfn getCanonicalPath))
                   (filter #(.endsWith % ".xml")))
        deleted (doall (map #(.delete (File. %)) files))]
    (when (some not deleted)
      (throw (Exception. "Some XML files not cleaned up."))))
  (inc-status! 1 :file :complete))

;;--------------------------------------------------
;; launch points

(defn data-download
  "Download and pre-process gatherer data."
  []
  (inc-status! 1 :main :total)
  (process-/add-work! "Main page" process-set-names)
  (inc-status! 1 :card :total)
  (process-/add-work! "Inject extra cards" inject-extra-cards)
  (when (config-/get-option :package)
    (inc-status! 1 :file :total)
    (process-/add-work! "File cleanup" cleanup-files)))

(defn file-creation
  "Generates output files and packages them."
  []
  (inc-status! 1 :file :total)
  (process-/add-work! "Write card xml" write-card-xml)
  (when (config-/get-option :meta)
    (inc-status! 1 :file :total)
    (process-/add-work! "Write meta xml" write-meta-xml))
  (inc-status! 1 :file :total)
  (process-/add-work! "Write set-info xml" write-set-info-xml)
  (when (config-/get-option :language)
    (inc-status! 1 :file :total)
    (process-/add-work! "Write language xml" write-language-xml)))

(defn zipit
  "Zip the files into a package."
  []
  (inc-status! 1 :file :total)
  (process-/add-work! "Zip xml files" zip-xml-files))
