(ns webapp.framework.client.coils
    (:refer-clojure :exclude [val empty remove find next parents])
    (:require
        [cljs.reader :as reader]
        [crate.core :as crate]
        [cljs.core.async :as async :refer         [chan close!]]
        [clojure.string]
        [webapp.framework.client.eventbus]
        [webapp.framework.client.coreclient]
    )
    (:use
        [jayq.core                          :only  [$ css  append fade-out fade-in empty attr bind]]
        [webapp.framework.client.help       :only  [help]]
        [domina                             :only  [ by-id value destroy! ]]
  )
  (:require-macros
    [cljs.core.async.macros :refer                 [go alt!]])
  (:use-macros
        [webapp.framework.client.eventbus :only    [redefine-action define-action]]
        [webapp.framework.client.coreclient :only  [ns-coils makeit defn-html on-click on-mouseover sql defn-html
                                                    defn-html2 neo4j]]
     )
)

(defn !-fn [fn-to-call & args]
   (apply fn-to-call args)
)

(def do-action         webapp.framework.client.eventbus/do-action)
(def undefine-action   webapp.framework.client.eventbus/undefine-action)
(def esb               webapp.framework.client.eventbus/esb)

(def body-html         webapp.framework.client.coreclient/body-html)
(def hide-popovers     webapp.framework.client.coreclient/hide-popovers)
(def new-dom-id        webapp.framework.client.coreclient/new-dom-id)
(def debug             webapp.framework.client.coreclient/debug)
(def popup             webapp.framework.client.coreclient/popup)
(def show-popover      webapp.framework.client.coreclient/show-popover)
(def set-text          webapp.framework.client.coreclient/set-text)
(def value-of          webapp.framework.client.coreclient/value-of)
(def find-el           webapp.framework.client.coreclient/find-el)
(def find-el           webapp.framework.client.coreclient/sql-fn)
(def neo4j-fn          webapp.framework.client.coreclient/neo4j-fn)
(def swap-section      webapp.framework.client.coreclient/swap-section)
(def el                webapp.framework.client.coreclient/el)
(def clear             webapp.framework.client.coreclient/clear)
(def remote            webapp.framework.client.coreclient/remote)
(def add-to            webapp.framework.client.coreclient/add-to)
(def on-click-fn       webapp.framework.client.coreclient/on-click-fn)
(def on-mouseover-fn   webapp.framework.client.coreclient/on-mouseover-fn)



;(!-fn add-to "main-section" "<div>Zoo</div>")
