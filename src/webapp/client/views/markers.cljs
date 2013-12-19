(ns webapp.client.views.markers
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
        [webapp.client.views.spatial         :only [copenhagen]]
        [webapp.client.model                 :only [find-places-in-bounds
                                                    places
                                                    place-changes
                                                    place-added-to-google-map
                                                    place-has-been-added-to-google-map?
                                                    highlight-place
                                                    commit-place-changes
                                                    place-has-changed?
                                                    remove-place-from-google-map
                                                    get-place-id-from-neo4j-index
                                                    find-places-in-rectangle]]
    )
    (:use-macros
        [webapp.framework.client.eventbus    :only [define-action redefine-action]]
        [webapp.framework.client.coreclient  :only [ns-coils defn-html on-click on-mouseover sql log]]
     )
)
(ns-coils 'webapp.client.views.markers)





;-------------------------------------------------------
(def markers
;-------------------------------------------------------
  (atom []))





;-------------------------------------------------------
(defn colored-marker [color]
;-------------------------------------------------------
    {
     :url (str "http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" color)
     :size (google.maps.Size. 20 30)
     }  )

(defn red-marker [] (colored-marker "FE7569"))
(defn blue-marker [] (colored-marker "FE75FF"))
(defn green-marker [] (colored-marker "FEFF69"))







;-------------------------------------------------------
(redefine-action "color marker"
;-------------------------------------------------------
  (let [
        id (message :id)
        ]
   (dorun
    (map
         (fn[place]
           (do
             (log (str "marker x:" id))
             (if (= (:id place) id)
               (do
                 (comment . (:marker place) setMap nil)
                 (comment let
                     [
                          marker   (google.maps.Marker.
                                        (clj->js
                                          {
                                            :position (google.maps.LatLng. (:y place) (:x place))
                                            :map       @the-map
                                            :title    (:name place)
                                            icon: {
                                                   url: ""
                                                   size: (google.maps.Size. 10 10)
                                            }
                                          }))
                      ]
                      (swap! markers conj {:id (:id x) :marker marker :y (:y x) :x (:x x)})
                      marker
                      )
                   []
                 ))))

         @markers))
))








;-------------------------------------------------------
(redefine-action "clear markers"
;-------------------------------------------------------
   (dorun
    (map (fn[marker]
           (. (:marker marker) setMap nil)
           ) @markers))
)






;-------------------------------------------------------
(add-watch place-changes :events-change
;-------------------------------------------------------
    (fn [key a old-val new-val]
      (doall
       (. js/console log (pr-str "Events changed"))
       (do-action "update places")
       )))






;-------------------------------------------------------
(defn show-place-on-google-map [& {:keys [
                                          place-id
                                          map-arg
                                         ]}]
;-------------------------------------------------------
  (let
                    [
                       place-details  (get @places place-id)
                       marker         (google.maps.Marker.
                                        (clj->js
                                        {
                                         :position  (google.maps.LatLng.
                                                     (:y place-details)
                                                     (:x place-details))
                                          :map       map-arg
                                          :title     (:name place-details)
                                          :icon      (if (:highlighted place-details)
                                                          (green-marker)
                                                          (red-marker)
                                                          )
                                        }))
                     ]
                     (place-added-to-google-map  :place-id place-id    :marker marker)
                     marker
                   ))




;-------------------------------------------------------
(redefine-action "update places"
;-------------------------------------------------------
  (go
    (let [
          places-in-view       (find-places-in-bounds @the-map)
          ]
          (doall
            (map
              (fn[place-id]
                (cond
                  (not (place-has-been-added-to-google-map?   place-id))
                    (show-place-on-google-map :place-id place-id
                                              :map-arg @the-map)

                 (and
                  (place-has-changed?   place-id)
                  (place-has-been-added-to-google-map?   place-id))
                   (let
                     [
                       place-details  (get places-in-view place-id)
                      ]
                       (log "remove place")
                       (remove-place-from-google-map  place-id)
                     (show-place-on-google-map :place-id place-id
                                              :map-arg @the-map)

                     )

              ))

           (keys places-in-view)
         )
         ))))








;-------------------------------------------------------
(defn find-places-in-square [center]
;-------------------------------------------------------
  (go
      (add-to "top-left" (str "<div>1</div>"))
    (do
      (add-to "top-left" (str "<div>2</div>"))
      (add-to "top-left" (str "<div>3</div>"))
      (let [
             places   (find-places-in-rectangle
                              :start-x (- (.lng center) 0.001)
                              :end-x   (+ (.lng center) 0.001)
                              :start-y (- (.lat center) 0.001)
                              :end-y   (+ (.lat center) 0.001)
                              )
           ]
           (add-to "top-left" (str "<div>4</div>"))
           (if (> (count places) 0)
             (do
               (add-to "top-left" (str "<div>5</div>"))
               (clear "bottom-left")
               (add-to "bottom-left"
                      (el :div {
                           :style "width: 200px; height: 120px;
                                   background-color: white;
                                   opacity:0.6;
                                   margin: 10px; border: 10px;"
                           } [
                              (str "<h2><strong>"
                                   (:place-name (get places (first (keys places))))
                                   "</strong></h2>")]))
               (add-to "top-left" (str "<div>6</div>"))
               (highlight-place
                :place-id (first (keys places)))
               (commit-place-changes)
               (add-to "top-left" (str "<div>7</div>"))
               (add-to "top-left" (str "<div>8</div>"))
               []

            ;(do-action "color marker" {:id (:id (first places))})
))))))






;-------------------------------------------------------
; DEBUG
;-------------------------------------------------------
(comment def a ( find-places-in-rectangle
                            :start-x  (- (.lng (. @the-map getCenter)) 0.0005)
                            :end-x    (+ (.lng (. @the-map getCenter)) 0.0005)
                            :start-y  (- (.lat (. @the-map getCenter)) 0.0005)
                            :end-y    (+ (.lat (. @the-map getCenter)) 0.0005)
                            ))

  (comment go
    (let [
           places    (into []
                       (<! (neo4j/find-names-within-distance
                            "ore2"
                            (.lng (. @the-map getCenter))
                            (.lat (. @the-map getCenter))
                            0.2)))
;          ii (get-place-id-from-neo4j-index (:id (first places)))
          ]
      (log (str (:id (first places))))))

  ;get-place-id-from-neo4j-index

  ;(place-has-been-added-to-google-map? 1)



; Adds a marker with text
(comment js/MarkerWithLabel.
    (clj->js
    {
          :labelContent        "Test"
          :position            (google.maps.LatLng.  (copenhagen :lat)  (copenhagen :lon))
          :map                 @the-map
          :labelStyle          {:opacity      0.75
                                :font-size    24}
          :labelClass "labels"
        })
 )


;(do-action "clear markers")

;find-places-in-bounds
;@the-map
;(do-action "load places")
;(find-places-in-bounds @the-map)


;(js->clj (:marker (first @markers)))
