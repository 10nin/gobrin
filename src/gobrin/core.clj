(ns gobrin.core
  (:require [net.cgrand.enlive-html :as html]))

(defn fetch-url [url]
  "get xml resrouce from url."
  (html/xml-resource (java.net.URL. url)))

(defn get-title [res]
  "get <title> list from resource."
  (map html/text (html/select res [:item :title])))

(defn head [n s]
  (let [ss (apply str (take n s))]
    (if (< n (count s)) (str ss "...")
        ss)))
