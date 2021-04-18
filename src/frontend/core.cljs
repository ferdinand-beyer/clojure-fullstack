(ns frontend.core
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require [reagent.dom]
            [re-frame.core :as rf]
            [frontend.view :as view]

            [cljs.core.async :as async :refer (<! >! put! chan)]
            [taoensso.sente  :as sente :refer (cb-success?)]))

(def ?csrf-token
  (when-let [el (.querySelector js/document "html")]
    (.getAttribute el "data-csrf")))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client!
       "/chsk" ; Note the same path as before
       ?csrf-token
       {:type :auto ; e/o #{:auto :ajax :ws}
        })]

  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )

(go-loop []
 (let [event-msg (<! ch-chsk)]
   (println "Received:" (:event event-msg))
   (recur)))

(defn render! []
  (reagent.dom/render [view/root]
                      (.getElementById js/document "app")))

(defn ^:dev/after-load refresh! []
  (rf/clear-subscription-cache!)
  (render!))

(defn ^:export init! []
  (enable-console-print!)
  (println "Token: " ?csrf-token)
  (render!))