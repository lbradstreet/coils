
(ns webapp.framework.client.coils
  [:use [webapp.framework.server.encrypt]]
  [:use [webapp.framework.client.eventbus]]
  [:use [webapp.framework.client.coreclient]]
   (:use clojure.pprint)
   (:require [rewrite-clj.parser :as p])
   (:require [rewrite-clj.printer :as prn])

)


(defmacro !! [ fn-symbol & args ]
      `(~fn-symbol
       ~@args))

(defmacro ! [ fn-symbol & args ]
    `(webapp.framework.client.coils/!-fn
      ~(!! fn-symbol args)
     )
 )



(macroexpand '(! add-to "a" "b"))


(macroexpand '(! define-action "scream"  (js/alert "aaargghh!")))



(macroexpand '(! define-action "scream"  (js/alert "aaargghh!")))


