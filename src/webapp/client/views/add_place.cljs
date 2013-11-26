(ns webapp.client.views.add-place
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
        [webapp.client.views.spatial         :only [copenhagen london]]
        [webapp.client.views.markers         :only [markers]]
    )
    (:use-macros
        [webapp.framework.client.eventbus    :only [define-action redefine-action]]
        [webapp.framework.client.coreclient  :only [ns-coils defn-html on-click on-mouseover sql log]]
     )
)
(ns-coils 'webapp.client.views.add-place)











(redefine-action
 "add place name"
   (go
     (let [
           lat         (message :lat)
           lng         (message :lng)
           place-name  (message :place-name)
          ]

        (if (= (count (message :place-name)) 0)
         (show-popover "place-name-input"
                       "Place name cannot be empty"
                       {:placement "top"})

        (do
             (<! (neo4j/add-to-simple-point-layer
                   {:name place-name :x lng :y lat} "ore2"))
             (do-action "update places")
             (clear "bottom-left")
         )

      )
    )
   )
 )









(redefine-action
 "Cancel add place"
  (do
   (do-action "update places")
   (clear "bottom-left")
  )
 )







(redefine-action "add place"
  (let [
           element    (message :element)
           lat        (message :lat)
           lng        (message :lng)
           marker     (google.maps.Marker.
                      (clj->js
                      {
                        :position (google.maps.LatLng. lat lng)
                        :map       @the-map
                        :title     (:name "")
                    }))
       ]
    (clear   element)

    (add-to
     element
     (el :div {
               :style "width: 200px; height: 120px;
                       background-color: white;
                       opacity:0.6;
                       margin: 10px; border: 10px;"
               }

         [
             (el :div {:class "form-group"} [
              "<input  id='place-name-input'
                       type='text'
                       class='input-small form-control'
                       placeholder='Name of place'>"
              ])

          (el :button {
                       :id       "add-place-button"
                       :type     "button"
                       :class    "btn btn-primary"
                       :style    "margin-left: 10px;"
                       :text     "Add place"
                       :onclick  #(do-action "add place name"
                                             {
                                                :place-name (value-of "place-name-input")
                                                :lat        lat
                                                :lng        lng
                                              })})

          (el :button {
                       :type "button"
                       :class "btn btn-info"
                       :style "margin-left: 10px;"
                       :text "Cancel"
                       :onclick #(do-action "Cancel add place")})

        ])
    )
    (swap! markers conj marker)
    marker
  )
)
;(clear "bottom-left")
;(do-action "add place"    {:place-id 1    :element  "bottom-left"})
