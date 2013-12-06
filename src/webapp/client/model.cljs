(ns webapp.client.model
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
    )
    (:use-macros
        [webapp.framework.client.eventbus    :only [define-action redefine-action]]
        [webapp.framework.client.coreclient  :only [ns-coils defn-html on-click on-mouseover sql log]]
     )
)
(ns-coils 'webapp.client.model)


(def next-place-id (atom 0))
@next-place-id

(def index-neo4j-id (atom {}))
@index-neo4j-id

(defn get-new-place-id []
  (swap! next-place-id inc)
  @next-place-id)
;(get-new-place-id)



;-------------------------------------------------------
(def places
;-------------------------------------------------------
  (atom {}))

;-------------------------------------------------------
(def place-changes
;-------------------------------------------------------
  (atom 0))



(defn place-is-on-map [place-id]
  (let
    [place   (get @places place-id)]
      (if place
        (:is-on-map? place)
        )
  )
  )

;place-changes
;(first @places)
;@places
;(reset! places {})
;(reset! places @places)
;(do-action "load places" {:x    (copenhagen :lon)    :y    (copenhagen :lat)})

;-------------------------------------------------------
(defn set-marker-for-place
;-------------------------------------------------------
  [& {:keys
        [
             place-id
             marker
        ]
       }
   ]
  (reset!
     places
     (merge
      @places
      {
         place-id
           (merge (get @places place-id)
               {:marker      marker
                :is-on-map?  true})})
   )
)
;(get @places 32)
;(set-marker-for-place :place-id 32 :marker 2)
;@places

;-------------------------------------------------------
(defn add-place-from-server
;-------------------------------------------------------
  [& {:keys
        [
             place-name
             neo4j-id
             x
             y
        ]
       }
   ]
  (let [
        place-already-in-model  (get  @index-neo4j-id  neo4j-id)
        ]

       (cond
        (not place-already-in-model)
         (do
           (let [
                 new-client-place-id  (get-new-place-id)
                 ]
                 (swap!
                    places
                    assoc
                    new-client-place-id
                    {
                      :neo4j-id     neo4j-id
                      :place-name   place-name
                      :x            x
                      :y            y
                      :is-on-map?   false
                    }
                 )
                 (swap!
                    index-neo4j-id
                    assoc
                    neo4j-id
                    new-client-place-id
                 )
           )
         )
       )
    )
)
;(add-place-from-server :neo4j-id 1000 :place-name "Library" :x 1 :y 2)




;-------------------------------------------------------
(redefine-action "load places"
;-------------------------------------------------------
  (go
    (let [
        x                      (message :x)
        y                      (message :y)
        places-from-server     (<! (neo4j/find-names-within-distance
                                      "ore2"
                                      x
                                      y
                                      1.2))
        ]
        (do
          (log "Number of places: " (count places-from-server))
          (log "First place: " (first places-from-server))
          (dorun
           (map
              (fn[place-from-server]
                (add-place-from-server
                 :neo4j-id   (:id     place-from-server)
                 :name       (:name   place-from-server)
                 :x          (:x      place-from-server)
                 :y          (:y      place-from-server)
                )
              )
              places-from-server
          ))
          (swap! place-changes inc)
        )

   )
  )
)

;(second  (first @places))





;-------------------------------------------------------
(defn find-places-in-bounds [this-map]
;-------------------------------------------------------
  (let [
        bounds    (. this-map getBounds)
        ]
             (select-keys
                @places
                (for [[place-id place-details] @places :when
                  (let [
                        place-coordinates    (google.maps.LatLng.
                                              (:y place-details)
                                              (:x place-details)
                                              )
                        ]
                    (if (. bounds contains place-coordinates)
                      true
                      )
                    )] place-id
                  )
              )


  )

)

; (find-places-in-bounds @the-map))

