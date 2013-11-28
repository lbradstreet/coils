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
                                                    value-of add-to show-popover]]
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













(redefine-action
 "show home page"
   (let [
         map-element-id   "map-content"
         place            copenhagen
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
                     (do-action "update places")
                     (do-action "add map left click event")
                     (do-action "add bounds changed event")
))))))



