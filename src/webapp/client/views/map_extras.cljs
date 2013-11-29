(ns webapp.client.views.map-extras
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
(ns-coils 'webapp.client.views.map-extras)












(redefine-action "show center square"
  (add-to "main"
        (str
            "<div"
            "    style='"
            "           left:        calc(50% - 2em);"
            "           top:         calc(50% - 3.5em);"
            "           position:    absolute;"
            "           z-index:     400;"
            "           height:      4em;"
            "           width:       4em;"
            "           border:      2px dotted black;"
            "'>"
            "</div>"
         )
  )
)
;(show-center-square)


;                     (do-action "add default stuff to map")



;(add-to (find-el "top-left") "<div>dfsfs</div>")



