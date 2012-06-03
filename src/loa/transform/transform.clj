
(ns loa.transform.transform
  "Common interface for all transformations."
  (:require (loa.transform
             (double-cards :as double-cards_)
             (fixes :as fixes_)
             (flip-cards :as flip-cards_)
             (injection :as injection_)
             (keyword-split :as keyword-split_)
             (mana-symbol :as mana-symbol_)
             (multi-name-cards :as multi-name-cards_)
             (plane-chaos :as plane-chaos_)
             (raw-card :as raw-card_)
             (reminder :as reminder_)
             (remove-tokens :as remove-tokens_)
             (set-information :as set-information_)
             (transform-cards :as transform-cards_)
             )))

(defmulti process-inner
  "Takes a sequence of cards and returns a new sequence with the given
  transformation applied."
  (fn [type cards] type))

(defmethod process-inner :double-cards
  [_ cards]
  (double-cards_/process cards))

(defmethod process-inner :fixes
  [_ cards]
  (map fixes_/process cards))

(defmethod process-inner :flip-cards
  [_ cards]
  (flip-cards_/process cards))

(defmethod process-inner :injection
  [_ cards]
  (injection_/process cards))

(defmethod process-inner :keyword-split
  [_ cards]
  (map keyword-split_/process cards))

(defmethod process-inner :mana-symbol
  [_ cards]
  (map mana-symbol_/process cards))

(defmethod process-inner :multi-name-cards
  [_ cards]
  (multi-name-cards_/process cards))

(defmethod process-inner :plane-chaos
  [_ cards]
  (map plane-chaos_/process cards))

(defmethod process-inner :raw-card
  [_ cards]
  (map raw-card_/process cards))

(defmethod process-inner :reminder
  [_ cards]
  (map reminder_/process cards))

(defmethod process-inner :remove-tokens
  [_ cards]
  (remove-tokens_/process cards))

(defmethod process-inner :set-information
  [_ cards]
  (set-information_/process cards))

(defmethod process-inner :transform-cards
  [_ cards]
  (transform-cards_/process cards))

;;--------------------------------------------------
;;
;;  Interface
;;
(defn process
  [type cards]
  (process-inner type cards))

(defn process-all
  [cards]
  (->> cards
       (process :raw-card)
       (process :injection)
       (process :remove-tokens)
       (process :set-information)
       (process :mana-symbol)
       (process :reminder)
       (process :double-cards)
       (process :multi-name-cards)
       (process :flip-cards)
       (process :transform-cards)
       (process :fixes)
       (process :keyword-split)
       (process :plane-chaos)))
