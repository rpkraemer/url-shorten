(ns url-shorten.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as response]
            [selmer.parser :as html]))

(defonce ids (atom 10000))
(defonce urls (atom {}))

(defn shorten [url]
  (let [id (swap! ids inc)
        id (Long/toString id 32)
        url (if (not (.contains url "http://"))
             (str "http://" url)
             url)]
    (swap! urls assoc id url)))

(defn get-url [url-id]
  (get @urls url-id))

(defroutes app-routes
  (GET "/" [_]
    (html/render-file "urls.html" {:urls @urls}))
  (GET "/shorten" [_]
    (html/render-file "form.html" {}))
  (POST "/shorten" [url]
     (shorten url)
     (response/redirect "/"))
  (GET "/:url-id" [url-id]
     (let [url (get-url url-id)]
       (response/redirect url)))
  (GET "/delete-url/:url-id" [url-id]
     (swap! urls dissoc url-id)
     (response/redirect "/"))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes
    (assoc-in site-defaults [:security :anti-forgery] false)))
