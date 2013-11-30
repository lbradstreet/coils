(ns webapp.client.main
    (:require
        [webapp.client.session]
        [webapp.client.model]
        [webapp.client.views.main-view]
        [webapp.client.views.gmap]
        [webapp.client.views.loginpanel]
        [webapp.client.views.markers]
        [webapp.client.views.add-place]
        [webapp.client.views.map-extras]
        [webapp.client.views.spatial]
        [goog.net.cookies :as cookie]
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
  (do-action "show home page")
)


;(do-action "refresh homepage")


;(.get goog.net.cookies "name" )
