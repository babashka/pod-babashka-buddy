(ns pod.babashka.buddy.keys
  (:require [buddy.core.keys :as keys])
  (:import [java.security PrivateKey PublicKey]))



(defn private-key
  ([path]
   (private-key path nil))
  ([path passphrase]
   (.getEncoded ^PrivateKey (keys/private-key path passphrase))))



(defn public-key
  [path]
  (.getEncoded ^PublicKey (keys/public-key path)))
