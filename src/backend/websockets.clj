(ns backend.websockets
  "Integrates Pedestal and Sente on top of an Immutant server."
  (:require [backend.session :refer [session-options]]
            [immutant.web :as web]
            [ring.middleware.keyword-params]
            [ring.middleware.params]
            [ring.middleware.session]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.immutant :refer [get-sch-adapter]]))

;; TODO: Clean up!
(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter))]

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

(defn sente-handler
  "Ring handler for Sente, routes based on HTTP method."
  [req]
  (case (:request-method req)
    :get (ring-ajax-get-or-ws-handshake req)
    :post (ring-ajax-post req)
    method-not-allowed))

(def handler
  (-> sente-handler
      (ring.middleware.keyword-params/wrap-keyword-params)
      (ring.middleware.params/wrap-params)
      (ring.middleware.session/wrap-session session-options)))

(defn configure
  "Configure an Immutant server for web sockets.  Put this in your Pedestal
   service map:

   {::http/container-options {:context-configurator configure}}

   This works similarly to io.pedestal.immutant.websockets/add-ws-endpoints,
   but delegates to Sente."
  [options]
  (web/run handler (assoc options :path "/chsk")))
