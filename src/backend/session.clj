(ns backend.session
  "Create a custom session store to share between Pedestal and our
   websocket Ring handler."
  (:require [ring.middleware.session.memory :refer [memory-store]]))

(def session-data (atom {}))
(def session-store (memory-store session-data))
(def session-options {:store session-store})