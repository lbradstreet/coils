
(ns webapp.framework.client.coils
  [:use [webapp.framework.server.encrypt]]
  [:use [webapp.framework.client.eventbus]]
  [:use [webapp.framework.client.coreclient]]
   (:use clojure.pprint)
   (:require [rewrite-clj.parser :as p])
   (:require [rewrite-clj.printer :as prn])

)

(defmacro !macro [ fn-symbol & args ]
      `(~fn-symbol ~@args))

(def macros-list
  #{
    "define-action"
    "defn-html"
    "redefine-action"
    "makeit"
    "ns-coils"
    "on-click"
    "on-mouseover"
    "sql"
})


(defmacro ! [ fn-symbol & args ]
    (cond
      (contains? macros-list  (name fn-symbol)) `(!macro
                  ~(symbol "webapp.framework.client.coils" (name fn-symbol))  ~@args)

      :else `(webapp.framework.client.coils/!-fn
                ~(symbol "webapp.framework.client.coils" (name fn-symbol)) ~@args
             )
     )
 )

;(symbol (name 'webapp.client.topnav/add-to))

(macroexpand '(! sdasd2/add-to "a" "b"))

(macroexpand '(! do-action "b"))



(macroexpand '(! define-action "scream"  (js/alert "aaargghh!")))



(macroexpand '(! define-action "scream"  (js/alert "aaargghh!")))


