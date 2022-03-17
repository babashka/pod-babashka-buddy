(ns pod.babashka.buddy.jwt
  (:require [buddy.core.keys :as keys]
            [buddy.sign.jwt  :as jwt])
  (:import [java.security KeyFactory PrivateKey]
           [java.security.spec PKCS8EncodedKeySpec]))



(defn sign
  "Takes the same claims and opts as buddy.sign.jwt/sign except instead of
   providing a pkey you can provide the following options to load the key:

   To invoke `buddy.core.keys/private-key`:
   ```
   {:private-key \"path\"
    :passphrase  nil}
   ```

   To invoke `buddy.core.keys/jwk->private-key`:
   ```
   {:jwk->private-key {}}
   ```

   To invoke `buddy.core.keys/jwk->private-key`:
   ```
   {:str->private-key \"keydata\"
    :passphrase  nil}
   ```
   "
  [claims {:keys [private-key jwk->private-key str->private-key passphrase] :as opts}]
  (let [private-key (cond
                      private-key      (keys/private-key private-key passphrase)
                      jwk->private-key (keys/jwk->private-key jwk->private-key)
                      str->private-key (keys/str->private-key str->private-key passphrase))]
    (jwt/sign claims private-key opts)))



(defn sign2
  ([claims ^bytes pkey]
   (sign2 claims pkey nil))
  ([claims ^bytes pkey opts]
   ;; TODO: All algorithms
   (let [^KeyFactory kf (KeyFactory/getInstance "RSA" "BC")
         ^PKCS8EncodedKeySpec ks (PKCS8EncodedKeySpec. pkey)
         ^PrivateKey pk (.generatePrivate kf ks)]
     (jwt/sign claims pk opts))))
