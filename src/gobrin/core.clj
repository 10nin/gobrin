(ns gobrin.core
  (:require [net.cgrand.enlive-html :as html]
            [compojure.core :refer [defroutes context GET]]
            [compojure.route :as route]
            [ring.adapter.jetty :as server]
            [ring.util.response :as res]
            [selmer.parser :as tmpl]))

(def ^:dynamic *rss-list*
  '({:id "kantei" :rss "https://www.kantei.go.jp/index-jnews.rdf"}
    {:id "mic" :rss "http://www.soumu.go.jp/news.rdf"}))
(defonce server (atom nil))

(defn fetch-url [url]
  "get xml resrouce from url."
  (html/xml-resource (java.net.URL. url)))

(defn head [n s]
  (let [ss (apply str (take n s))]
    (if (< n (count s)) (str ss "...")
        ss)))

;; TODO: titleタグとlinkタグを正しく見て要素の分割を行うように改修する(さらに下請けにしてもいいかも)
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

;; TODO: うまくdivタグ下にリンクがまとまるように修正する
(defn make-div [id links]
  (tmpl/render "<div id=\"{{id}}\">
{% for l in links %}
{{l|safe}}<br>
{% endfor %}
</div>" {:id id, :links links}))

(defn make-title-map [title-elements]
  (map make-title-map- title-elements))

(defn make-hyperlink- [dic]
  "Make <a> tag link from {:text 'caption', :link url} style dictionary."
  (tmpl/render "<a href=\"{{link}}\" target=\"_blank\">{{title}}</a>" dic))

;; TODO: hyperlinkのリストをdivで囲むように修正する(make-div活用)
(defn make-hyperlink [url]
  (map
   make-hyperlink-
   (-> (fetch-url url)
       get-title-elements
       make-title-map)))

(defn render-html- [contents]
  (tmpl/render-file "templates/rss.html" {:contents contents}))

(defn render-html [url-list]
;;  (-> (map #(make-hyperlink (:rss %)) url-list)
  (let [contents (map #(make-hyperlink (:rss %)) url-list)]
    (first (map render-html- contents))))

(defn html [res]
  (assoc res :headers {"Content-Type" "text/html; charset=utf-8"}))

(defn root-view [req]
  (render-html *rss-list*))

(defn root-handler [req]
  (-> (root-view req)
      res/response
      html))

(defroutes handlers
  (GET "/" req root-handler)
  (route/not-found "<h1>HTTP 404 : Not found</h1>"))
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
