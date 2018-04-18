(ns gobrin.core
  (:require [compojure.core :refer [defroutes context GET]]
            [compojure.route :as route]
            [ring.adapter.jetty :as server]
            [ring.util.response :as res]
            [net.cgrand.enlive-html :as html]
            [selmer.parser :as tmpl]))

(def ^:dynamic *rss-list* '({:id "kantei" :rss "https://www.kantei.go.jp/index-jnews.rdf"}))
(defonce server (atom nil))
(defn html [res]
  (assoc res :headers {"Content-Type" "text/html; charset=utf-8"}))

(defn root-view [req]
  "<p>Soon...</p>")

(defn root-handler [req]
  (-> (root-view req)
      res/response
      html))

(defroutes handlers
  (GET "/" req root-handler)
  (route/not-found "<h1>HTTP 404 : Not found</h1>"))

(defn fetch-url [url]
  "get xml resrouce from url."
  (html/xml-resource (java.net.URL. url)))

(defn head [n s]
  (let [ss (apply str (take n s))]
    (if (< n (count s)) (str ss "...")
        ss)))

(defn make-title-map- [elm]
  (let [l (first elm)
        t (second elm)]
    {(:tag l)
     (->> (:content l)
          first),
     (:tag t)
     (->> (:content t)
          first
          (head 15))}))

(defn get-title-elements [res]
  "get <link> and <title> list from resource."
  (partition 2 (html/select res [:item :> #{:title :link}])))

(defn make-title-map [title-elements]
  (map make-title-map- title-elements))

(defn make-hyperlink- [dic]
  "Make <a> tag link from {:text 'caption', :link url} style dictionary."
  (tmpl/render "<a href=\"{{link}}\" target=\"_blank\">{{title}}</a>" dic))

(defn make-div [id links]
  (tmpl/render "<div id=\"{{id}}\">
{% for l in links %}
{{l|safe}}<br>
{% endfor %}
</div>" {:id id, :links links}))

(defn make-hyperlink [url]
  (map
   make-hyperlink-
   (-> (fetch-url url)
       get-title-elements
       make-title-map)))

(defn render-html- [contents]
  (tmpl/render-file "templates/rss.html" {:contents contents}))

(defn render-html [url-list]
  (let [contents (map #(make-hyperlink (:rss %)) url-list)]
    (map render-html- contents)))

(defn start-server [& {:keys [host port join?]
                       :or {host "localhost" port 3000 join? false}}]
  (let [port (if (string? port) (Integer/parseInt port) port)]
    (when-not @server
      (reset! server (server/run-jetty #'handlers {:host host :port port :join? join?})))))

(defn stop-server []
  (when @server
    (.stop @server)
    (reset! server nil)))

(defn restart-server []
  (when @server
    (stop-server)
    (start-server)))
