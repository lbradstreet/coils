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
                                                    value-of add-to show-popover log]]
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
(def bounds-loaded
;----------------------------------------------------------------------------------------
  (atom []))




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






(defn does-bounds-contain-bounds? [larger smaller]
  (cond
     (< (. (. smaller getSouthWest) lng)   (. (. larger getSouthWest) lng)) false
     (< (. (. smaller getSouthWest) lat)   (. (. larger getSouthWest) lat)) false

     (> (. (. smaller getNorthEast) lng)   (. (. larger getNorthEast) lng)) false
     (> (. (. smaller getNorthEast) lat)   (. (. larger getNorthEast) lat)) false
     :else true
  )
)



(defn is-loaded? [b2]
    (some
     (fn [b] (does-bounds-contain-bounds? b b2))
     @bounds-loaded))




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
                            bounds (. @the-map getBounds)
                            sw     (. bounds  getSouthWest )
                            ne     (. bounds  getNorthEast )
                            min-x  (. sw lng)
                            max-x  (. ne lng)
                            max-y  (. ne lat)
                            min-y  (. sw lat)
                            width  (- max-x min-x)
                            height (- max-y min-y)

                                new-min-x (- min-x width)
                                new-max-x (+ max-x width)
                                new-min-y (- min-y height)
                                new-max-y (+ max-y height)

                                ]
                          (log "***********min-x" min-x)
                          (log "***********max-x" max-x)
                          (log "***********min-y" min-y)
                          (log "***********max-y" max-y)
                          (log "***********width" height)
                          (log "***********height" height)
                          (log "***********new-min-x" new-min-x)
                          (log "***********new-min-y" new-min-y)
                          (log "***********new-max-x" new-max-x)
                          (log "***********new-max-y" new-max-y)


                          (if (not (is-loaded? bounds))
                            (do
                              (do-action "load places" {
                                                      :min-y new-min-y
                                                      :min-x new-min-x

                                                      :max-y new-max-y
                                                      :max-x new-max-x
                                                      })
                              (swap! bounds-loaded conj
                                     (google.maps.LatLngBounds.
                                       (google.maps.LatLng. new-min-y
                                                            new-min-x)
                                       (google.maps.LatLng. new-max-y
                                                            new-max-x)

                                      )))


                              (do
                                (do-action "update places"))
                            )
                          (clear "top-left")
                          (do-action "show center square")
                          )))
                        ))



(def drag (atom 1))

;----------------------------------------------------------------------------------------
(redefine-action "add center changed event"
;----------------------------------------------------------------------------------------
       ( google.maps.event.addListener
                        @the-map
                        "drag"
                        (fn []
                             (dorun
                              (do
                                (clear "top-right")
                                (swap! drag inc)
                                (add-to "top-right" (str "<div>Drag " @drag "</div>"))
                                (find-places-in-square  (. @the-map getCenter))
                                []
                                )
                          )
                          )))






;----------------------------------------------------------------------------------------
; DEBUG
;----------------------------------------------------------------------------------------
;(do-action "update places")
;@bounds-loaded


(comment .
 (. @the-map getBounds)
 getSouthWest)

(comment .
 (. @the-map getBounds)
 getNorthEast)


;( def a (. @the-map getBounds))

;a

;(is-loaded? a)

(comment def b
  (google.maps.LatLngBounds.
   (google.maps.LatLng. (- (. (. a getSouthWest) lat) 0.01)
                        (+ (. (. a getSouthWest) lng)) 0.01)
   (google.maps.LatLng. (+ (. (. a getNorthEast) lat) 0.01)
                        (- (. (. a getNorthEast) lng) 0.01)
  )))
;(does-bounds-contain-bounds? a b)

