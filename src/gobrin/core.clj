(ns gobrin.core
  (:require [net.cgrand.enlive-html :as html]
            [selmer.parser :as tmpl]))

(defn fetch-url [url]
  "get xml resrouce from url."
  (html/xml-resource (java.net.URL. url)))

(defn make-title-map [title-elements]
  (let [l (first title-elements)
        t (second title-elements)]
    {(:tag l) (first (:content l)), (:tag t) (first (:content t))}))

(defn get-title-elements [res]
  "get <link> and <title> list from resource."
  (partition 2 (html/select res [:item :> #{:title :link}])))

(defn head [n s]
  (let [ss (apply str (take n s))]
    (if (< n (count s)) (str ss "...")
        ss)))

(defn make-link [dic]
  "Make <a> tag link from {:text 'caption', :link url} style dictionary."
  (map #(tmpl/render "<a href={{:link}} target=\"_blank\">{{:text}}</a>" %) dic))
