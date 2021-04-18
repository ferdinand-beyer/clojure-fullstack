(ns backend.websockets
  (:require [immutant.web :as web]
            [ring.middleware.keyword-params]
            [ring.middleware.params]
            #_[ring.middleware.anti-forgery]
            [ring.middleware.session]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.immutant :refer [get-sch-adapter]]))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket!
       (get-sch-adapter)
       ;; TODO: Integrate with Pedestal
       {:csrf-token-fn nil})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(def method-not-allowed
  {:status 405
   :headers {}
   :body "405 Method Not Allowed"})

(def method-handlers
  {:get ring-ajax-get-or-ws-handshake
   :post ring-ajax-post})

(defn sente-handler [req]
  (case (:request-method req)
    :get (ring-ajax-get-or-ws-handshake req)
    :post (ring-ajax-post req)
    method-not-allowed))

(def handler
  (-> sente-handler
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params
      #_ring.middleware.anti-forgery/wrap-anti-forgery
      ring.middleware.session/wrap-session))

(defn configure [options]
  (println "Configuring: " options)
  (web/run handler (assoc options :path "/chsk")))
