(ns user
  (:require [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :as ns-tools]
            [io.pedestal.http :as http]
            [mount.core :as mount]

            [backend.core :as backend]
            
            [build]))

(ns-tools/set-refresh-dirs "src" "dev")

(mount/defstate dev-server
  :start (-> backend/service
             http/default-interceptors
             http/dev-interceptors
             http/create-server
             http/start)
  :stop (http/stop dev-server))

(defn start []
  (build/start)
  (mount/start [#'dev-server]))

(defn stop []
  (mount/stop #'dev-server))

(defn reset []
  (stop)
  (ns-tools/refresh :after 'user/start))
