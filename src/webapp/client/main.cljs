(ns webapp.client.main
    (:require
        [webapp.client.views.loginpanel]
        [webapp.client.views.settings]
        [webapp.client.session]
        [webapp.framework.client.debugger-3d]
        [goog.net.cookies :as cookie]
        [webapp.framework.client.neo4j]
    )
    (:use
        [webapp.framework.client.eventbus  :only [do-action esb]]
    )
)



(defn ^:export resizeScreenFn [w h]
    ;(.log js/console "RESIZE")
    nil
)




(defn ^:export main []
  ;(do-action "create blank page structure")
  ;(do-action "show top nav")
  (do-action "show home page")
)




;(do-action "refresh homepage")


;(.get goog.net.cookies "name" )
