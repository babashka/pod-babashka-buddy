(ns pod.babashka.buddy.key-utils
  (:import [java.security KeyFactory PrivateKey PublicKey]
           [java.security.spec X509EncodedKeySpec PKCS8EncodedKeySpec]
           [org.bouncycastle.crypto.util PrivateKeyFactory PublicKeyFactory]))



(defprotocol Algo
  (algo [this]))



(extend-protocol Algo
  org.bouncycastle.crypto.params.RSAKeyParameters
  (algo [_] "RSA")
  org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
  (algo [_] "RSA")
  org.bouncycastle.crypto.params.ECPrivateKeyParameters
  (algo [_] "EC")
  org.bouncycastle.crypto.params.ECPublicKeyParameters
  (algo [_] "EC"))



(defn ->private-key
  ^PrivateKey [pkey]
  (try
    (let [key-params (PrivateKeyFactory/createKey pkey)
          ^KeyFactory kf (KeyFactory/getInstance (algo key-params) "BC")
          ^PKCS8EncodedKeySpec ks (PKCS8EncodedKeySpec. pkey)]
      (.generatePrivate kf ks))
    (catch Exception _ pkey)))



(defn ->public-key
  ^PublicKey [pkey]
  (try
    (let [key-params (PublicKeyFactory/createKey pkey)
          ^KeyFactory kf (KeyFactory/getInstance (algo key-params) "BC")
          ^X509EncodedKeySpec ks (X509EncodedKeySpec. pkey)]
      (.generatePublic kf ks))
    (catch Exception _ pkey)))