#!/usr/bin/env bb

(require '[babashka.pods :as pods])

(if (= "executable" (System/getProperty "org.graalvm.nativeimage.kind"))
  (pods/load-pod "./pod-babashka-buddy")
  (pods/load-pod ["clojure" "-M" "-m" "pod.babashka.buddy"]))

(require '[pod.babashka.buddy.core.hash :as h])

(prn (h/sha256 "foo"))

(require '[pod.babashka.buddy.core.mac :as mac])

(prn (mac/hash "foo bar" {:key "mysecretkey" :alg :hmac+sha256}))

(require '[pod.babashka.buddy.core.nonce :as nonce])

(prn (nonce/random-bytes 64))

(def hash-algorithm :hmac+sha256)
(def secret (nonce/random-bytes 64))

(require '[clojure.string :as string]
         '[pod.babashka.buddy.core.codecs :as codecs])

(let [timestamp (System/currentTimeMillis)
      nonce (nonce/random-bytes 64)
      nonce-hex (codecs/bytes->hex nonce)
      payload (pr-str {:nonce nonce-hex :timestamp timestamp})
      signature (codecs/bytes->hex (mac/hash payload {:alg hash-algorithm :key secret}))]
  (prn (string/join "-" [nonce-hex timestamp signature])))

(require '[pod.babashka.buddy.core.kdf :as kdf])

(let [key-bytes (kdf/get-engine-bytes {:alg :hkdf+sha256 :key "supersecret" :salt "sea"
                                       :info "babashka rocks!" :length 32})
      key-hex (codecs/bytes->hex key-bytes)]
  (assert (= "5189451aed99c4acf6c3573f5eec223a13ab0840bb0138c4080ab87bdf7d0ebc"
             key-hex))
  (prn key-hex))

(require '[pod.babashka.buddy.sign.jwe :as jwe]
         '[cheshire.core :as json])

(let [jwt {:name "Babashka" :role "Ruler of your shell"}
      jwt-json (json/encode jwt)
      key "abcdefghijklmnopqrstuvwxyzABCDEF"
      jwe (jwe/encrypt jwt-json key)
      decrypted-jwe (-> jwe (jwe/decrypt key) codecs/bytes->str)]
  (prn jwe)
  (prn decrypted-jwe)
  (assert (= decrypted-jwe jwt-json)))

(require '[pod.babashka.buddy.sign.jwt :as jwt])
(let [claim {:iss "123456"}
      jwt+rs256 (jwt/sign claim {:alg :rs256 :private-key "keys/private-key.pem"})]
  (prn jwt+rs256))

(require '[pod.babashka.buddy.keys :as keys])
(let [claim {:iss "123456"}
      pkey (keys/private-key "keys/private-key.pem")
      jwt+rs256 (jwt/sign2 claim pkey {:alg :rs256})]
  (prn jwt+rs256))


(when-not (= "executable" (System/getProperty "org.graalvm.nativeimage.kind"))
  (shutdown-agents)
  (System/exit 0))
