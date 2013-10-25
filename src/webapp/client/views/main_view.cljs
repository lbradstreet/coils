(ns webapp.client.main-view
 (:refer-clojure :exclude [val empty remove find next parents])
    (:require
        [cljs.reader :as reader]
        [crate.core :as crate]
        [cljs.core.async :as async :refer [chan close!]]
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
        [webapp.framework.client.coreclient :only [makeit ns-coils defn-html on-click on-mouseover sql]]
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
   (let [map-id   "map-canvas"]
       (clear :#main-section)
       (swap-section
            ($ :#main-section)
            (map-html map-id)
            #(let [
                map-options  {
                                 :zoom 8
                                 :center (google.maps.LatLng. -34.397, 150.644)
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







