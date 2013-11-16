(ns webapp.client.gui-actions
    (:require
        [cljs.reader :as reader]
    )
    (:use
        [webapp.framework.client.coreclient :only [clear remote add-to]]
        [webapp.framework.client.eventbus   :only [do-action esb undefine-action]]
    )
    (:use-macros
        [webapp.framework.client.eventbus :only [define-action]]
     )
)





(define-action
    "clear homepage"
    (clear :#main))


