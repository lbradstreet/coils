(ns webapp-config.settings)






(defonce ^:dynamic *mandrill-api-key* "enter_api_key_for_mandrill_here")

(defonce ^:dynamic *environment* "dev")

(defonce ^:dynamic *web-server* "127.0.0.1:3000")

(defonce ^:dynamic *database-server* "127.0.0.1")

(defonce ^:dynamic *database-user* "postgres")

(defonce ^:dynamic *database-password* "manager")

(defonce ^:dynamic *database-name* "yazz")

(defonce ^:dynamic *sql-encryption-password* "animal")

(defonce ^:dynamic *show-code* true)
