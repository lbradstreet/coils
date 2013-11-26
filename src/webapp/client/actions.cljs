(ns webapp.client.action
    (:use
        [webapp.framework.client.eventbus  :only [do-action esb undefine-action]]
    )
    (:use-macros
        [webapp.framework.client.eventbus :only [define-action redefine-action]]
    )

)



;(do-action "add place") ;
;(do-action "show center square") ;
;(do-action "show home page") ; shows the map - can only be called once
;(do-action "update places") ;shows the markers on the map

