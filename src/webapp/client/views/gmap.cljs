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
    )
    (:use-macros
        [webapp.framework.client.eventbus    :only [define-action redefine-action]]
        [webapp.framework.client.coreclient  :only [ns-coils defn-html on-click on-mouseover sql log]]
     )
)
(ns-coils 'webapp.client.views.gmap)






(defn map-options [x y]
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





(redefine-action "add map left click event"
    (google.maps.event.addListener
     @the-map
     "click"
     (fn [event]
       (go
        (let [
              lat-lng      (.-latLng event)
              lat          (.lat lat-lng)
              lng          (.lng lat-lng)
              place-id     (<! (neo4j/add-to-simple-point-layer
                                {:name "Unnamed" :x lng :y lat} "ore2"))
              ]
          ;(js/alert (str lat "-" lng))

          (. @the-map  panTo (google.maps.LatLng. lat lng))
          (do-action "update-places")
          (do-action "add place"
                     {:place-id place-id})
          )))))




(redefine-action "add corners"
                 (let
                        [
                         control-div     (el :div {:text "hello"})
                         control-content (el :div {:text "content"})
                         ]
                         (add-to
                                control-div
                                control-content
                              )

                              ( .push
                               (get
                                (js->clj
                                (.-controls @the-map)
                                ) google.maps.ControlPosition.TOP_RIGHT)
                               (el "div" {:id "top-right" :text "some"})
                               )

                               ( .push
                                 (get
                                  (js->clj
                                  (.-controls @the-map)
                                  ) google.maps.ControlPosition.BOTTOM_CENTER)
                                 (el "div" {:id "bottom"})
                               )
                               ( .push
                                 (get
                                  (js->clj
                                  (.-controls @the-map)
                                  ) google.maps.ControlPosition.BOTTOM_LEFT)
                                 (el "div" {:id "bottom-left"})
                               )



                       ( google.maps.event.addListener
                        @the-map
                        "bounds_changed"
                        (fn []
                          (do-action "show center square")
                          (comment if (find-el "top-right")
                            (do-action "show login signup panel")

                            )
                          )
                        )
                     ))
