(ns webapp.client.globals
    (:require
        [webapp.client.session]
        [webapp.client.model]
        [webapp.client.views.main-view]
        [webapp.client.views.gmap]
        [webapp.client.views.html]
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



(def map-provider "google")
;(def map-provider "openstreetmap")
