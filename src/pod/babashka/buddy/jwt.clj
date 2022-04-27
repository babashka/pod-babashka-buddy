(ns pod.babashka.buddy.jwt
  (:require [buddy.sign.jwt :as jwt]
            [pod.babashka.buddy.key-utils :refer [->private-key ->public-key]]))



(defn sign
  ([claims pkey]
   (sign claims pkey nil))
  ([claims pkey opts]
   (jwt/sign claims (->private-key pkey) opts)))



(defn unsign
  ([message pkey] (unsign message pkey {}))
  ([message pkey {:keys [skip-validation] :or {skip-validation false} :as opts}]
   (jwt/unsign message (->public-key pkey) opts)))



(defn encrypt
  ([claims pkey] (encrypt claims pkey nil))
  ([claims pkey opts]
   (jwt/encrypt claims (->private-key pkey) opts)))



(defn decrypt
  ([message pkey] (decrypt message pkey nil))
  ([message pkey {:keys [skip-validation] :or {skip-validation false} :as opts}]
   (jwt/decrypt message (->public-key pkey) opts)))
