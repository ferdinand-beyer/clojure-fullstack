(ns backend.core
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [mount.core :as mount]

            [backend.pages]
            [backend.websockets :as ws]))

(defn hello-world [_]
  {:status 200
   :body "Hello, world!"})

(def routes
  (route/expand-routes
   #{["/" :get [http/html-body backend.pages/home] :route-name :home]
     ["/greet" :get hello-world :route-name :greet]
     }))

(def service
  {::http/routes routes
   ::http/resource-path "/public"

   ;; TODO: Verify this
   ::http/secure-headers
   {:content-security-policy-settings {:object-src "none"}}

   ::http/type :immutant
   ::http/container-options {:context-configurator ws/configure}
   ::http/join? false
   ::http/port 8080})

(mount/defstate server
  :start (-> service
             http/default-interceptors
             http/create-server
             http/start)
  :stop (http/stop server))

(comment
  (route/try-routing-for routes :prefix-tree "/greet" :get)
  )