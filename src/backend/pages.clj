(ns backend.pages
  (:require [hiccup.page :as page]
            [ring.util.response :as r]))

(defn home [req]
  (r/response
   (page/html5
    {:lang "en"}
    [:head
     [:meta {:name "csrf"
             :content (:io.pedestal.http.csrf/anti-forgery-token req)}]
     [:title "Hello, world!"]]
    [:body
     [:div#app]
     (page/include-js "/js/main.js")])))
