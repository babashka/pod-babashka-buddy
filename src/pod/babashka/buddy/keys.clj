(ns pod.babashka.buddy.keys
  (:require [buddy.core.keys :as keys])
  (:import [java.security PrivateKey]))



(defn private-key
  ([path]
   (private-key path nil))
  ([path passphrase]
   (.getEncoded ^PrivateKey (keys/private-key path passphrase))))
