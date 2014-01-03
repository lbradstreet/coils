(ns webapp.framework.server.neo4j-helper

  [:require [clojure.string :as str]]
  (:require [clojurewerkz.neocons.rest])
  (:require [clojurewerkz.neocons.rest.nodes :as nn])
  (:require [clojurewerkz.neocons.rest.relationships :as nrl])
  (:require [clojurewerkz.neocons.rest.cypher :as cy])
  (:require [clojurewerkz.neocons.rest.spatial :as nsp])
  (:require [clojurewerkz.neocons.rest.transaction :as tx])
  (:require [clojurewerkz.neocons.rest.conversion :as nc])
  (:use [webapp-config.settings])
  (:import [java.util.UUID])

  (:require [cheshire.core             :as json]
            [clojurewerkz.neocons.rest :as rest]
            [clojurewerkz.support.http.statuses :refer :all]
            [clojurewerkz.neocons.rest.helpers  :refer :all]
            [clojurewerkz.neocons.rest.records  :refer :all])
  (:import  [java.net URI URL]
            clojurewerkz.neocons.rest.Neo4JEndpoint)

)




;--------------------------------------------------------------
; connect to neo4j if available
;--------------------------------------------------------------
(try
  (clojurewerkz.neocons.rest/connect!
   "http://localhost:7474/db/data/")
  (catch Exception e
    (str "Error connecting to Neo4j: " (.getMessage e))))












;--------------------------------------------------------------
(defn- spatial-location-for
  "Used to talk to the Neo4j spatial server"
  [^Neo4JEndpoint endpoint action]
  ;--------------------------------------------------------------
  (str
   (:uri endpoint)
   "ext/SpatialPlugin/graphdb/"
   action))





;--------------------------------------------------------------
(defn- post-spatial
  "Post a spatial query"
  [item-type body]
  ;--------------------------------------------------------------
  (let [{:keys [status headers body]}
        (rest/POST
         (spatial-location-for  rest/*endpoint* item-type)
         :body (json/encode body))
        payload  (json/decode body true)]
    (map instantiate-node-from payload)))





;--------------------------------------------------------------
(defn
  add-simple-point-layer
  "Adds a layer to the map. The x and y passed in are the names
   to use for the coordinate system"
  [lname]
  ;--------------------------------------------------------------
  (nsp/add-simple-point-layer
   lname "y" "x"))












;--------------------------------------------------------------
(defn
  add-node-to-layer
  "Add a node with the appropriate latitude and longitude
   properties to the given layer"
  [node layer]
;--------------------------------------------------------------
  (first (post-spatial
          "addNodeToLayer"
          {
           :layer       layer
           :node        (node-location-for rest/*endpoint* (:id node))
           })))







;--------------------------------------------------------------
(defn add-to-simple-layer
  "Add a node to the spatial layer"
  [node-name  x  y  layer]
  ;--------------------------------------------------------------
  (let [
        node          (nn/create {:name node-name :x x :y y})
        result        (add-node-to-layer node layer)
        ]
    (:id result)))






;--------------------------------------------------------------
(defn get-layer
  "Get the named spatial layer"
  [layer]
  ;--------------------------------------------------------------
  (first (post-spatial "getLayer" {:layer layer})))





;--------------------------------------------------------------
(defn find-within-distance
  [layer point-x point-y distance-in-km]
  "Find all points in the layer within a given distance of the given point"
  ;--------------------------------------------------------------
  (post-spatial "findGeometriesWithinDistance"
                {
                 :layer           layer
                 :pointX          point-x
                 :pointY          point-y
                 :distanceInKm    distance-in-km}))




;--------------------------------------------------------------
(defn find-within-bounds
  "Find all points in the layer within a given bounds"
  [layer min-x max-x min-y max-y]
  ;--------------------------------------------------------------
  (post-spatial "findGeometriesInBBox"
                {
                 :layer      layer
                 :minx       min-x
                 :maxx       max-x
                 :miny       min-y
                 :maxy       max-y}))



;--------------------------------------------------------------
(defn find-names-within-distance [layer x y dist-km]
;--------------------------------------------------------------
  (map
    (fn [x]
      (let [data    (:data x)]
        {
          :id    (:id x)
          :name  (:name data)
          :x     (:x data)
          :y     (:y data)
         }))

    (find-within-distance layer x y dist-km)))



;--------------------------------------------------------------
(defn find-names-within-bounds [layer minx maxx miny maxy]
;--------------------------------------------------------------
  (map
    (fn [x]
      (let [data    (:data x)]
        {
          :id    (:id x)
          :name (:name data)
          :x    (:x data)
          :y    (:y data)
        }
      )
    )

    (find-within-bounds layer minx maxx miny maxy)))







;----------------------------------------------------------
(defn neo-data
  ""
  [cypher-query-response]
  ;----------------------------------------------------------
  (-> cypher-query-response
      :data
      (first)
      (first))
  )



(defn parse-int [s]
   (Integer. (re-find  #"\d+" s )))


(defn neo-id [node]
    (let [
          s     (:self node)
          io    (.indexOf s "data/node/")
          ss    (.substring s (+ io 10))
          pi    (parse-int ss)
          ]
      pi
      )
)



(defn neo-keys [x] (-> x (neo-data) (keys)))
(defn neo-result [x k] (-> x (neo-data) (get k)))
(defn neo-result-keys [x k] (-> x (neo-data) (get k) (keys)))



;----------------------------------------------------------
(defn neo-node-data
  [cypher-query-response]
  ;----------------------------------------------------------
  (let [n  (neo-data  cypher-query-response)]
    (merge
     (get n :data)
     {:id (neo-id  n) }
)))



;----------------------------------------------------------
(defn neo-data2
  [cypher-query-response]
  ;----------------------------------------------------------
  (let [n  (neo-data  cypher-query-response)]
    n
))



;----------------------------------------------------------
(defn node
;----------------------------------------------------------
  ([neo4j-query]
   (neo-node-data
    (cy/query
     neo4j-query
     {})
    ))

  ([neo4j-query params]
   (neo-node-data
    (cy/query
     neo4j-query
     params)
    ))

  )

;----------------------------------------------------------
(defn get-value [neo4j-query]
  ;----------------------------------------------------------
  (neo-data
   (cy/query
    neo4j-query
    {})
   ))


 (get-value "CREATE (x:User {name: \"Zubair\"}) RETURN count (x);")


(neo-data
(cy/query "CREATE (x:User {name: \"Zubair\"}) RETURN COUNT(x);" {}))

(defn link [start-node   link-details    end-node]
    (nrl/create start-node   end-node   link-details)
)


(defn relationships [node-id]
  (nrl/all-for node-id))

(let [
      user           (node  "CREATE (y:User {name: \"Jack\"}) RETURN y;")
      web-session    (node  "CREATE (x:WebSession {cookie: \"dfggfdfgdgfd\"}) RETURN x;")
      email-login    (node  "CREATE (x:Authorisation {email: \"jack@hotmail.com\"}) RETURN x;")
      email-login2   (node  "CREATE (x:Authorisation {email: \"jack@gmail.com\"}) RETURN x;")
      _              (link  web-session  "for"  user )
      _              (link  user  "has login"  email-login )
      _              (link  user  "has login"  email-login2 )
      ]
  user
  )


(defn get-node [x]
  (try
    (node  "START n = node({node_id}) RETURN n" {:node_id x})
    (catch Exception e
      nil)))


(get-node 34427)


(relationships 34357)


(defn neo-incoming [x k] (-> x (neo-data) (get k) :incoming_relationships))
(defn neo-outgoing [x k] (-> x (neo-data) (get k) :outgoing_relationships))










;----------------------------------------------------------
; debug stuff
;----------------------------------------------------------

;(get-layer "ore2")
;(add-to-simple-layer "McDonalds" -10.1 -1.0 "ore2")




;(nsp/find-within-distance "pl" 51.6306 -0.80029 50000)


(comment neo-node-data
 (cy/tquery "START n = node(17106) RETURN n" {})
 "n")


;(find-names-within-bounds "ore2" 0.0 1.1 50.0 51.5)


;( find-names-within-distance "ore2" -10.1 -1.1 10000.1)




(comment  let [t (tx/begin-tx)]
  (tx/commit t))


(comment  try
     (add-simple-point-layer "ore2")
         (catch Exception e (str "caught exception: " (.getMessage e))))







(defn neo4j-add [table-name properties]
  (neo-node-data
   (cy/tquery (str
               "CREATE (new_record:"
               table-name
               " {props}) RETURN new_record;")
              {:props properties})
   "new_record"))

;(neo4j-add "users" {:name "Zubair"})




(comment neo-node-data
 (cy/tquery "CREATE (me:User {name: \"Zubair\"}) RETURN me;" {})
 "me"
 )





(comment -> (cy/query (str
             "CREATE (me:"
             "user"
             " {props}) RETURN me;")
            {:props {:name "fseed"}})
    :data
(first)
(first)
    )


;(cy/query "CREATE (x:User {name: \"Zubair\"}) RETURN x;")
