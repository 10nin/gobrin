(ns gobrin.core
  (:require [net.cgrand.enlive-html :as html]))

(defn fetch-url [url]
  (html/xml-resource (java.net.URL. url)))
