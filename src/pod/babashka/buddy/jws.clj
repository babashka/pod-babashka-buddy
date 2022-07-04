(ns pod.babashka.buddy.jws
  (:require [buddy.sign.jws :as jws]
            [pod.babashka.buddy.key-utils :refer [->private-key ->public-key]]))



(defn sign
  [payload pkey & [{:keys [alg header] :or {alg :hs256} :as opts}]]
  (jws/sign payload (->private-key pkey) opts))



(defn unsign
  ([input pkey] (unsign input pkey nil))
  ([input pkey {:keys [alg] :or {alg :hs256} :as opts}]
   (jws/unsign input (->public-key pkey) opts)))
