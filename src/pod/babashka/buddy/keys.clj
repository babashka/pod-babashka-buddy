(ns pod.babashka.buddy.keys
  (:require [buddy.core.keys :as keys]
            [pod.babashka.buddy.key-utils :refer [->private-key ->public-key]])
  (:import [java.security Key]))



(defn private-key
  ([path]
   (private-key path nil))
  ([path passphrase]
   (.getEncoded ^Key (keys/private-key path passphrase))))



(defn public-key
  [path]
  (.getEncoded ^Key (keys/public-key path)))



(defn str->public-key
  [keydata]
  (.getEncoded ^Key (keys/str->public-key keydata)))



(defn str->private-key
  ([keydata]
   (str->private-key keydata nil))
  ([keydata passphrase]
   (.getEncoded ^Key (keys/str->private-key keydata passphrase))))



(defn jwk->private-key
  [jwk]
  (.getEncoded ^Key (keys/jwk->private-key jwk)))



(defn jwk->public-key
  [jwk]
  (.getEncoded ^Key (keys/jwk->public-key jwk)))



(defn jwk
  [private public]
  (keys/jwk (->private-key private) (->public-key public)))



(defn public-key->jwk
  [public]
  (keys/public-key->jwk (->public-key public)))



(defn jwk-thumbprint
  [jwk]
  (keys/jwk-thumbprint jwk))
