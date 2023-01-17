(ns pod.babashka.buddy.crypto
  (:require [buddy.core.crypto :as crypto]))

(defn block-cipher-encrypt
  "Same as buddy.core.crypto/encrypt"
  (^bytes [input key iv]
   (block-cipher-encrypt input key iv nil))
  (^bytes [input key iv options]
   (crypto/encrypt input key iv options)))

(defn block-cipher-decrypt
  "Same as buddy.core.crypto/decrypt"
  (^bytes [input key iv]
   (block-cipher-decrypt input key iv nil))
  (^bytes [input key iv options]
   (crypto/decrypt input key iv options)))

(defn stream-cipher-encrypt
  "buddy.core.crypto/stream-cipher composed with buddy.core.crypto/init!
  and buddy.core.crypto/process-bytes!.

  `alg` must be a valid algorithm for buddy.core.crypto/stream-cipher.
  `key` and `iv` must be valid values for the selected `alg`"
  ^bytes
  [input key iv alg]
  (let [engine (-> (crypto/stream-cipher alg)
                   (crypto/init! {:key key :iv iv :op :encrypt}))]
    (crypto/process-bytes! engine input)))

(defn stream-cipher-decrypt
  "buddy.core.crypto/stream-cipher composed with buddy.core.crypto/init!
  and buddy.core.crypto/process-bytes!

  `alg` must be a valid algorithm for buddy.core.crypto/stream-cipher.
  `key` and `iv` must be valid values for the selected `alg`"
  ^bytes
  [input key iv alg]
  (let [engine (-> (crypto/stream-cipher alg)
                   (crypto/init! {:key key :iv iv :op :decrypt}))]
    (crypto/process-bytes! engine input)))


