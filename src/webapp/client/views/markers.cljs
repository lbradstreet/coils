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
                                                    value-of add-to show-popover]]
        [jayq.core                           :only [attr $ css append fade-out fade-in empty]]
        [webapp.framework.client.eventbus    :only [do-action esb undefine-action]]
        [webapp.client.session               :only [the-map]]
        [webapp.client.views.html            :only [map-html]]
    )
    (:use-macros
        [webapp.framework.client.eventbus    :only [define-action redefine-action]]
        [webapp.framework.client.coreclient  :only [ns-coils defn-html on-click on-mouseover sql log]]
     )
)
(ns-coils 'webapp.client.views.markers)


(def markers (atom []))

@markers

(define-action "clear markers"
   (dorun
    (map (fn[marker] (. marker setMap nil)) @markers))
)
;(do-action "clear markers")

(define-action "update places"
  (go
   ;(. @the-map clear)
   (let [
        places
         (into []
               (<! (neo4j/find-names-within-distance
                    "ore2"
                    (.lng (. @the-map getCenter))
                    (.lat (. @the-map getCenter))
                    100)))
        ]
      (do-action "clear markers")
      (doall (map (fn[x]
         (log x)
         (let
         [
           marker   (google.maps.Marker.
                      (clj->js
                      {
                        :position (google.maps.LatLng. (:y x) (:x x))
                        :map       @the-map
                        :title     (:name x)
                    }
                  )
               )
  ]
  (swap! markers conj marker)
  marker
))
 places
)))))








(defn find-places-in-square []
  (go
    (let [
           places
             (into []
               (<! (neo4j/find-names-within-distance
                    "ore2"
                    (.lng (. @the-map getCenter))
                    (.lat (. @the-map getCenter))
                    0.2)))
        ]
      (if (> (count places) 0)
          (do
            (clear "bottom-left")
            (add-to "bottom-left"
                    (el :div {
                         :style "width: 200px; height: 120px;
                                 background-color: white;
                                 opacity:0.6;
                                 margin: 10px; border: 10px;"
                         } [
                    (str "<h2><strong>"
                         (:name (first places))
                         "</strong></h2>")]))
           )
      )
)))
