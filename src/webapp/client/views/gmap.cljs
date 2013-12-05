(ns webapp.client.views.gmap
 (:refer-clojure :exclude [val empty remove find next parents])
    (:require
        [cljs.reader                     :as reader]
        [crate.core                      :as crate]
        [cljs.core.async                 :as async :refer [chan close!]]
        [webapp.framework.client.neo4j   :as neo4j]
    )

  (:require-macros
    [cljs.core.async.macros :refer [go alt!]])

  (:use
        [webapp.framework.client.coreclient  :only [popup do-before-remove-element new-dom-id find-el clj-to-js sql-fn
                                                    header-text body-text
                                                    body-html make-sidebar swap-section  el clear remote
                                                    value-of add-to show-popover]]
        [jayq.core                           :only [attr $ css append fade-out fade-in empty]]
        [webapp.framework.client.eventbus    :only [do-action esb undefine-action]]
        [webapp.client.session               :only [the-map]]
        [webapp.client.views.html            :only [map-html]]
        [webapp.client.views.spatial         :only []]
        [webapp.client.views.markers         :only [find-places-in-square]]

    )
    (:use-macros
        [webapp.framework.client.eventbus    :only [define-action redefine-action]]
        [webapp.framework.client.coreclient  :only [ns-coils defn-html on-click on-mouseover sql log]]
     )
)
(ns-coils 'webapp.client.views.gmap)








;----------------------------------------------------------------------------------------
(defn map-options [x y]
;----------------------------------------------------------------------------------------
                   {
                    :zoom               14
                    :center             (google.maps.LatLng.  y x)
                    :mapTypeId          google.maps.MapTypeId.ROADMAP
                    :styles
                    [
                     {
                      :featureType "poi"
                      :stylers     [{ :visibility "off" }]
                      }
                     ]
                    :panControl         false
                    :zoomControl        false
                    :mapTypeControl     false
                    :scaleControl       false
                    :streetViewControl  false
                    :overviewMapControl false
                    })








;----------------------------------------------------------------------------------------
(redefine-action "add map left click event"
;----------------------------------------------------------------------------------------
    (google.maps.event.addListener
       @the-map
       "click"
       (fn [event]
           (go
               (let [
                     lat-lng      (.-latLng event)
                     lat          (.lat lat-lng)
                     lng          (.lng lat-lng)
                    ]
                    (. @the-map  panTo (google.maps.LatLng. lat lng))
                    (do-action "add place"
                               {:lat        lat
                                :lng        lng
                                :element    "bottom-left"})
)))))







;----------------------------------------------------------------------------------------
(defn add-corner [& {:keys [id position html]}]
;----------------------------------------------------------------------------------------
    ( .push
                               (get
                                (js->clj
                                (.-controls @the-map)
                                ) position)
                               (el "div" {:id id } [
                                                    (if html html "<div></div>")
                                                    ])))





;----------------------------------------------------------------------------------------
(redefine-action "add corners"
;----------------------------------------------------------------------------------------
                 (let
                        [
                         control-div     (el :div {:text "hello"})
                         control-content (el :div {:text "content"})
                         ]
                         (add-to
                                control-div
                                control-content)

                              (add-corner :position   google.maps.ControlPosition.TOP_RIGHT
                                          :id         "top-right")

                              (add-corner :position   google.maps.ControlPosition.BOTTOM_CENTER
                                          :id         "bottom")

                              (add-corner :position   google.maps.ControlPosition.BOTTOM_LEFT
                                          :id         "bottom-left")

                              (add-corner :position   google.maps.ControlPosition.TOP_LEFT
                                          :id         "top-left"
                                          :html       (el :div {
                                                               :style "width: 100%; height: 100%;
                                                                       background-color: white;
                                                                       opacity:0.6;
                                                                       margin: 10px; border: 10px;"
                                                               }
                                                          [
                                                              "<h1>Drag the map</h1>"
                                                          ]
                                                      ))
))




;----------------------------------------------------------------------------------------
(redefine-action "add bounds changed event"
;----------------------------------------------------------------------------------------
    (go
       ( google.maps.event.addListener
                        @the-map
                        "bounds_changed"
                        (fn []
                          (let [
                            x (.lng (. @the-map getCenter))
                            y (.lat (. @the-map getCenter))
                                ]
                          (do-action "load places" {:x x   :y y})
                          (do-action "show center square")
                          (find-places-in-square)
                          (clear "top-left")
                          ))
                        )))



;(do-action "update places")
