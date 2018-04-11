(ns gobrin.core
  (:require [net.cgrand.enlive-html :as html]
            [selmer.parser :as tmpl]))

(defn fetch-url [url]
  "get xml resrouce from url."
  (html/xml-resource (java.net.URL. url)))

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

(defn head [n s]
  (let [ss (apply str (take n s))]
    (if (< n (count s)) (str ss "...")
        ss)))

(defn make-hyperlink- [dic]
  "Make <a> tag link from {:text 'caption', :link url} style dictionary."
  (tmpl/render "<a href=\"{{link}}\" target=\"_blank\">{{title}}</a>" dic))

(defn make-hyperlink [url]
  (map
   make-hyperlink
   (-> (fetch-url url)
       get-title-elements
       make-title-map)))
      
