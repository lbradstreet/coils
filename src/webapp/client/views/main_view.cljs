(ns webapp.client.main-view
 (:refer-clojure :exclude [val empty remove find next parents])
    (:require
        [cljs.reader :as reader]
        [crate.core :as crate]
        [cljs.core.async :as async :refer [chan close!]]
        [webapp.framework.client.neo4j :as neo4j]
        ;[google.maps]
        ;[google.maps.MapTypeId]
    )

  (:require-macros
    [cljs.core.async.macros :refer [go alt!]])

  (:use
        [webapp.framework.client.coreclient :only [popup do-before-remove-element new-dom-id find-el clj-to-js sql-fn header-text body-text body-html make-sidebar  swap-section  el clear addto remote  add-to]]
        [jayq.core                          :only [attr $ css append fade-out fade-in empty]]
        [webapp.framework.client.help       :only [help]]
        [webapp.framework.client.eventbus   :only [do-action esb undefine-action]]
        [webapp.framework.client.interpreter :only [!fn]]
    )
    (:use-macros
        [webapp.framework.client.eventbus :only [define-action redefine-action]]
        [webapp.framework.client.coreclient :only [makeit ns-coils defn-html on-click on-mouseover sql
                                                   log log-async]]
        [webapp.framework.client.interpreter :only [! !! !!!]]
     )
)

(ns-coils 'webapp.client.main-view)


(defn-html map-html [map-id]
    (el :div {:id "map-content" :data-role "content"
              :style "position: absolute;
                      width: 100% !important;
                      height: 100% !important;
                      padding: 0 !important;
                      top : 0px !important;
                      bottom : 0px !important;
                      right : 0px !important;
                      left : 0px !important;"
              :target "_blank"} [



]))



(def the-map (atom nil))






(defn hide-map []
   (add-to "main" "map-canvas")

   (css
       ($ (find-el "map-canvas"))
        {:visibility "hidden"}
   )

   (css
       ($ (find-el "map-canvas"))
       {:display "none"}
   )
)



(defmethod do-before-remove-element
    "map-content"
    [elem]
        (.log js/console (str "Hiding map '" (attr ($ (find-el elem)) "id") "'") )
        (hide-map)
  )


(defn-html sidebar []
  (el :div {} [
    (make-sidebar
         {:text "Examples" :html (map-html "map-canvas")}
     )
])
)







(redefine-action
 "show home page"
   (let [map-id   "map-canvas"
         x 12.575183
         y 55.622033
         ]
       (clear :#main-section)
       (swap-section
            ($ :#main-section)
            (map-html map-id)
            #(let [
                map-options  {
                                 :zoom 14
                                 :center (google.maps.LatLng.  y x)
                                 :mapTypeId google.maps.MapTypeId.ROADMAP
                             }
               ]
               (do
                 (if @the-map
                   (do
                        (add-to "map-content" "map-canvas")

                         (css
                             ($ (find-el "map-canvas"))
                              {:visibility ""}
                         )

                         (css
                             ($ (find-el "map-canvas"))
                             {:display ""}
                         )
                   )

                   (do

                        (add-to
                         "map-content"
                         (el :div {:id map-id
                                  :style "width: 100% !important;
                                          height: 100% !important;
                                          "
                                  :target "_blank"} [

                          ]))

                         (reset! the-map (google.maps.Map.

                                     (. js/document getElementById map-id)
                                     (clj-to-js  map-options)))


 (go
   (let [
        places  (into [] (<! (neo4j/find-names-within-distance   "ore2"  x  y  100)))
        ]
  (doall (map (fn[x]

         (log x)
         (let
  [
      marker   (google.maps.Marker.
                  (clj->js
                    {
                        :position (google.maps.LatLng. 55.622033 12.575183
          )
                        :map       @the-map
                        :title     "Hello World!"
                    }
                  )
               )
  ]
  marker
))
 places
))))


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
                                ;(js/alert (str lat "-" lng))
                                  (<! (neo4j/add-to-simple-point-layer
                                    {:name "bella centre" :x lng :y lat} "ore2")))



                                  )

                              )
                          )

                   )
                 )

                 ;(google.maps.event/trigger   map-id  "resize")

                 map-options))
            )

           (swap-section
            ($ :#left-navigation)
            (sidebar)
       )
     []



))










 (comment go
(let [places
      (into [] (<! (neo4j/find-names-within-distance   "ore2"  12.575183  55.622033  100)))
      ]
  (doall (map (fn[x] (log x)) places))
  ))
