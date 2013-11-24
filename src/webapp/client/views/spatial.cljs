(ns webapp.client.views.spatial
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
(ns-coils 'webapp.client.views.spatial)




(defn london     [] (google.maps.LatLng. 51.5072 0.1275))
(defn copenhagen [] (google.maps.LatLng. 55.622033 12.575183))


;(. @the-map  panTo (london))



;(. @the-map  panTo (google.maps.LatLng. 0 0))



(comment go
(let [places
      (into [] (<! (neo4j/find-names-within-distance   "ore2"  12.575183  55.622033  100)))
      ]
  (doall (map (fn[x] (log x)) places))
  ))
