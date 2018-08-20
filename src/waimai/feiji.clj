(ns waimai.feiji
  (:require [clojure.data.json :as json]
            digest
            [org.httpkit.client :as httpc])
  (:import [java.net URLEncoder]
           java.util.Base64))

(set! *warn-on-reflection* true)

(defn- ^:String make-sign
  [^String appid ^String secret ^String data]
  (let [md5-str ^String (digest/md5 (str data secret))]
    (.encodeToString
      ^java.util.Base64$Encoder (java.util.Base64/getEncoder) 
      ^bytes (.getBytes md5-str "UTF-8"))))

(defn ^{:static true} request
  [^String cmd params & {:keys [^String appid ^String secret ^String url ^boolean sign?]
                         :or {^String appid (System/getProperty "waimai.feiji.appid")
                              ^String secret (System/getProperty "waimai.feiji.secret")
                              ^String url (or (System/getProperty "waimai.feiji.url") "http://store.feiji-zlsd.com/feiji/")
                              ^boolean sign? true }
                         :as opts}]
  (let [payload (if sign?
                  (let [params-str (json/write-str params)]
                    {:appid appid
                     :sign (make-sign appid secret params-str)
                     :data params-str})
                  (assoc params :appid appid))]
    (httpc/request
      {:method :post
       :url (str url cmd)
       :headers {"content-type" "application/json"}
       :body (json/write-str payload)
       :throw-exceptions false
       :timeout 30000
       :accept :json})))