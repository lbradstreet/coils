(ns webapp.client.views.main-view
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
                                                    value-of add-to show-popover
                                                    is-debug?]]
        [jayq.core                           :only [attr $ css append fade-out fade-in empty]]
        [webapp.framework.client.eventbus    :only [do-action esb undefine-action]]
        [webapp.client.session               :only [the-map]]
        [webapp.client.views.html            :only [map-html]]
        [webapp.client.views.gmap            :only [map-options]]
        [webapp.client.views.spatial         :only [copenhagen london]]
    )
    (:use-macros
        [webapp.framework.client.eventbus    :only [define-action redefine-action]]
        [webapp.framework.client.coreclient  :only [ns-coils defn-html on-click on-mouseover sql log]]
     )
)
(ns-coils 'webapp.client.views.main-view)









;-------------------------------------------------------
(defn get-start-location
  "Gets the start location of the map, using geocoding if needed"
  []
;-------------------------------------------------------
     (let
     [
        ch            (chan 1)
        ]
        (if (.-geolocation js/navigator )
             (. (.-geolocation js/navigator ) getCurrentPosition
                (fn[p]
                      (go
                         (>! ch {:lat (.-latitude (.-coords p)) :lon (.-longitude (.-coords p))})
                         (close! ch)
                      )
                )

                 (fn [] (do
                          (js/alert "location denied, using Copenhagen")
                          (go
                             (>! ch copenhagen)
                             (close! ch)
                          )
                        )
                 )
              )

             (go
                (>! ch copenhagen)
                (close! ch)
             )
        )
       ch
     )

  )






;-------------------------------------------------------
(redefine-action "show home page"
;-------------------------------------------------------
(go
   (let [
         map-element-id   "map-content"
         debug-mode       (:value (<! (is-debug?)))
         place            (if (not (= debug-mode "prod")) copenhagen (<! (get-start-location)))
         x                (:lon place)
         y                (:lat place)
         ]

         (clear :#main)

         (swap-section
            ($ :#main)
            (map-html  map-element-id)
            #(do
                 (if (not @the-map)
                   (do
                     (reset!
                        the-map
                        (google.maps.Map.
                          (. js/document getElementById map-element-id)
                          (clj-to-js  (map-options x y))))


                     (do-action "add corners")
                     (do-action "load places" {:x x   :y y})
                     (do-action "add map left click event")
                     (do-action "add bounds changed event")
                     (do-action "update places")
)))))))





;-------------------------------------------------------
; debug stuff
;-------------------------------------------------------
;(do-action "load places")
(comment  go
  (log (str (<! (is-debug?)))))
(comment  go
  (log (str (<! (get-start-location)))))
