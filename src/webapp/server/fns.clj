(ns webapp.server.fns
  [:require [clojure.string :as str]]
  [:use [korma.db]]
  [:use [webapp.framework.server.systemfns]]
  [:use [webapp.framework.server.email-service]]
  [:use [webapp.framework.server.encrypt]]
  [:use [korma.core]]
  [:use [clojure.core.async]]
  [:use [clojure.repl]]

  (:use [webapp-config.settings])
  (:import [java.util.UUID])
       (:use clojure.pprint)

)




(defentity test_table)



(defn test-call
  "Tests to see if the server was called ok"
  [params]
  (get-in params [:a :d ]))



(defn get-db-data [params]
  (select test_table))

;(get-db-data nil)

(comment insert test_table
  (values [{:id          (java.util.UUID/randomUUID)
            :name        "shopping"
            :description "Get the beef"
           }]))

(comment select test_table)

(defn say-hello [params]
    {:value (into []
[])})


(defn get-environment [params]
  {:value *environment*}
)

(defn get-show-debug [params]
  {:value *show-code*}
)



(comment let [ch (chan)]
  (go (while true
        (let [v (<! ch)]
          (println "Read: " v))))
  (go (>! ch "hi")
      (<! (timeout 5))
      (>! ch "there")))


(defn add-user [{user-name :user-name password :password}]
  user-name
)






(defn find-user-by-username [{username :username}]
          (let [r (exec-raw
               ["SELECT id, user_name FROM users where user_name = ?"
                [username]]
                :results)]

            (first r)
            )
)


(defn find-user-by-id [{id :id}]
          (let [r (exec-raw
               ["SELECT id, user_name FROM users where id = ?"
                [id]]
                :results)]

            (first r)
            )
)


;(find-user-by-username {:username "zq@nemcv.com2"})

(defn send-password [{email :email}]
    (println "fn email:" email)
    (let [user (find-user-by-username  {:username email})
         request-id (str (java.util.UUID/randomUUID))]
      (if user
        (do
            (exec-raw
                 ["insert into password_reset_requests (fk_user_id, status, request_id) values (?, ?, ?)"
                  [(:id user)
                   "sent"
                   request-id]])

            (send-email
                  :message    (str
                                 "Please reset your password by clicking here:"
                                 *web-server*
                                 "/main.html?reset_request_id="
                                 request-id
                              )
                  :subject    "Coils.cc reset password request"
                  :from-email "help@coils.cc"
                  :to-email   email
            )
            {:status :sent})


        {:status :doesnt-exist}
      )
    )
)

;(send-password {:email "zq@nemcv.com"})

(defn login-user [{username :username password :password}]
       (let [user
         (first (exec-raw
               ["SELECT id, user_name, password FROM users where user_name = ? and password = ?"
                [username password]]
                :results))

             user-without-password (dissoc user :password)
             ]

         {:value user-without-password}
         )
)




(defn reset-password [{reset-request-id   :reset-request-id
                       password           :password}]
  (let [
        reset-request
                      (first
                          (exec-raw
                           ["select fk_user_id from password_reset_requests where request_id = ?"
                            [reset-request-id]] :results ))

        user          (find-user-by-id {:id (:fk_user_id reset-request)})


        ]


         (exec-raw
               ["update users set password = ? where id = ?"
                   [  password  (:id user) ]
               ]
                )
          [:status "ok"]
    )
)

(comment reset-password
               {
                    :reset-request-id      "50315c74-ce31-4547-8479-5d1cdb8bae95"
                    :password              "duck"})

;(login-user {:username "z" :password "s"})




(exec-raw
               ["SELECT * FROM yazz_login_details where user_name = ?"
                ["zubairq@hotmail.com"]]
                :results)


(defn db-count-records [table-name]
    (:count
     (first
     (exec-raw
                   [(str "SELECT count(*) FROM " table-name)
                    []]
                    :results)
    )))

(defn db-table-fields [table-name]
    (keys
     (first
     (exec-raw
                   [(str "SELECT * FROM " table-name " limit 1")
                    []]
                    :results)
    )))


 (db-count-records "learno_tests")

(pprint

 (db-table-fields "learno_tests"))

(pprint
(exec-raw
                   [(str "SELECT name,rating FROM learno_tests limit 100")
                    []]
                    :results))
