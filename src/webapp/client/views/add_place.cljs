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
    )
    (:use-macros
        [webapp.framework.client.eventbus    :only [define-action redefine-action]]
        [webapp.framework.client.coreclient  :only [ns-coils defn-html on-click on-mouseover sql log]]
     )
)
(ns-coils 'webapp.client.views.add-place)











(redefine-action
 "Add place name"
    (if (= (count (message :place-name)) 0)
       (show-popover "place-name-input"
                     "Place name cannot be empty"
                     {:placement "top"})
    )

 )














(redefine-action "add place"
  (let [place-id   (message :place-id)]
    (js/alert place-id)
    (clear "bottom-left")
    (add-to
     "bottom-left"
     (el :div {
               :style "width: 200px; height: 120px;
                       background-color: white;
                       opacity:0.6;
                       margin: 10px; border: 10px;"
               }

         [
             (el :div {:class "form-group"} [
              "<input  id='place-name-input' type='text' class='input-small form-control'
                                           placeholder='Name of place'>"
              ])

          (el :button {
                       :id       "reset-password-button"
                       :type     "button"
                       :class    "btn btn-primary"
                       :style    "margin-left: 10px;"
                       :text     "Add place"
                       :onclick  #(do-action "Add place name"
                                             {
                                                :place-name (value-of "place-name-input")
                                              })})

          (el :button {
                       :type "button"
                       :class "btn btn-info"
                       :style "margin-left: 10px;"
                       :text "Cancel"
                       :onclick #(do-action "Cancel add place")})

        ])
    )
  )
)
;(add-place :place-id 1)
