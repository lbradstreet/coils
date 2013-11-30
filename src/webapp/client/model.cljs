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




;-------------------------------------------------------
(def places
;-------------------------------------------------------
  (atom {}))

;(js->clj (:marker (first @markers)))




;-------------------------------------------------------
(redefine-action "load places"
;-------------------------------------------------------
  (go
    (let [
        x          (message :x)
        y          (message :y)
        places     (<! (neo4j/find-names-within-distance
                            "ore2"
                            x
                            y
                            1.2))
        ]
        (do
          (log "Number of places: " (count places))
          (log "First place: " (first places))
        )

   )
  )
)
(do-action "load places"
           {
             :x    (copenhagen :lon)
             :y    (copenhagen :lat)
           })

