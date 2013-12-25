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
        [webapp.client.globals               :only [tracking]]


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
                    :zoom               16
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
                    :mapTypeControl     true
                    :scaleControl       false
                    :streetViewControl  false
                    :overviewMapControl false
                    })








;----------------------------------------------------------------------------------------
(redefine-action "add map left click event"
;----------------------------------------------------------------------------------------
    (comment google.maps.event.addListener
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
    (el "div" {:id id :style ""} [
                         (if html html "<div></div>")
                         ])))





(defn tracking-button-html []
  (el :div {
            :style (str "width2: 30px; height2: 40px;
                        background-color:"
                        (if @tracking "lightgreen" "pink")
                        ";
                        opacity:0.6;
                        margin2: 10px; border2: 20px;")

            :onclick
            (fn[]
              (swap-section
               "bottom-left"
               (str "<div style='border: 10px solid lightgray;background-color: "
                    (if @tracking "pink" "lightgreen")
                    "; padding-bottom:50px; margin: 5px;'>
                    <div>"
                    (if @tracking "Tracking turned off" "Tracking turned on")
                    "</div>
                    </div>
                    "))
                (reset! tracking (not @tracking))
                (swap-section "tracker_button" (tracking-button-html))
              )
            }
      [
       "<h1 style='padding:15px;'>T</h1>"
       ]
      ))

;----------------------------------------------------------------------------------------
(redefine-action
 "add corners"
 ;----------------------------------------------------------------------------------------
 (let
   [
    control-div     (el :div {:text "hello"})
    control-content (el :div {:text "content"})
    ]
   (add-to
    control-div
    control-content)


   (add-corner
    :position   google.maps.ControlPosition.RIGHT_CENTER
    :id         "right-center"
    :html       (el
                 :div {
                       :style "width2: 30px; height2: 40px;
                       background-color: white;
                       opacity:0.6;
                       margin2: 10px; border2: 20px;"

                       :onclick
                       (fn[]
                         (swap-section
                          "bottom-left"
                          "<div style='border: 10px solid lightgray;background-color: white; padding-bottom:50px; margin: 5px;'>
                          <div>Login and Join - coming soon</div>
                          </div>
                          ")                                                   )
                       }
                 [
                  "<h1 style='padding:15px;'>ME</h1>"
                  ]
                 ))


   (add-corner :position   google.maps.ControlPosition.RIGHT_CENTER
               :id         "right-center"
               :html       (el :div {
                                     :style "width2: 30px; height2: 40px;
                                     background-color: white;
                                     opacity:0.6;
                                     margin2: 10px; border2: 20px;"

                                     :onclick
                                     (fn[]
                                       (do-action
                                        "add place"
                                        {:lat        (.lat (. @the-map getCenter))
                                         :lng        (.lng (. @the-map getCenter))
                                         :element    "bottom-left"})                                                    )
                                     }
                               [
                                "<h1 style='padding:15px;'>+</h1>"
                                ]
                               ))


   (add-corner :position   google.maps.ControlPosition.RIGHT_CENTER
               :id         "right-center"
               :html       (el :div {
                                     :style "width2: 30px; height2: 40px;
                                     background-color: white;
                                     opacity:0.6;
                                     margin2: 10px; border2: 20px;"

                                     :onclick
                                     (fn[]
                                       (swap-section
                                        "bottom-left"
                                        "<div style='border: 10px solid lightgray;background-color: white; padding-bottom:50px; margin: 5px;'>
                                        <div>Help:</div>
                                        <div>Center square selects place</div>
                                        <div>Accept a place earns you 1 point</div>
                                        </div>
                                        ")                                                   )
                                     }
                               [
                                "<h1 style='padding:15px;'>?</h1>"
                                ]
                               ))


         (add-corner :position   google.maps.ControlPosition.RIGHT_CENTER
               :id         "right-center"
               :html       (el :div {:id "tracker_button" }
                               [(tracking-button-html)]
                               ))



   (add-corner :position   google.maps.ControlPosition.BOTTOM_CENTER
               :id         "bottom")

   (add-corner :position   google.maps.ControlPosition.BOTTOM_LEFT
               :id         "bottom-left",
               :html       "<h1 style='padding:15px;'></h1>")

   (add-corner :position   google.maps.ControlPosition.TOP_LEFT
               :id         "top-left"
               :html       (el :div {
                                     :style "width: 200px; height: 100px;
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
(defn bounds-changed [bounds]
;----------------------------------------------------------------------------------------
  (go
   (let [
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

         ;else
         (do
           (do-action "update places")))


     ;(clear "top-left")
     (do-action "show center square")
)))



;----------------------------------------------------------------------------------------
(redefine-action
 "add bounds changed event"
 ;----------------------------------------------------------------------------------------
 (go
  (google.maps.event.addListener
    @the-map
    "bounds_changed"
    (fn [] (bounds-changed (. @the-map getBounds)))
    )))




;----------------------------------------------------------------------------------------
(redefine-action
 "add center changed event"
 ;----------------------------------------------------------------------------------------
 (google.maps.event.addListener
  @the-map
  ;"drag"
  "center_changed"
  (fn []
    (go
     (let [center (. @the-map getCenter)]
       (find-places-in-square  center)
       (clear "top-left")
       ;(do-action "add center changed event")
       []
       )))))






;----------------------------------------------------------------------------------------
; DEBUG
;----------------------------------------------------------------------------------------
