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


;; -- JWS Tests --
(require
  '[pod.babashka.buddy.keys :as keys]
  '[pod.babashka.buddy.sign.jws :as jws])

(def secret "test")
(def test-data "Babashka is cool")
(def rsa-privkey (keys/private-key "keys/privkey.3des.rsa.pem" "secret"))
(def rsa-pubkey (keys/public-key "keys/pubkey.3des.rsa.pem"))
(def ec-privkey (keys/private-key "keys/privkey.ecdsa.pem" "secret"))
(def ec-pubkey (keys/public-key "keys/pubkey.ecdsa.pem"))

;; jws-wrong-key
(let [candidate "foo bar "
      result    (jws/sign candidate ec-privkey {:alg :es512})]
  (try (jws/unsign result secret nil)
    (catch Exception e
      (assert (= :signature (:cause (ex-data e)))))))

(doseq [alg [:hs512 :hs384 :hs256]]
  (let [res1 (jws/sign test-data secret {:alg alg})
        res2 (jws/unsign res1 secret {:alg alg})]
    (assert (java.util.Arrays/equals res2 (codecs/to-bytes test-data)))))

(doseq [alg [:ps512 :ps384 :ps256 :rs512 :rs384 :rs256]]
  (let [res1 (jws/sign test-data rsa-privkey {:alg alg})
        res2 (jws/unsign res1 rsa-pubkey {:alg alg})]
    (assert (java.util.Arrays/equals res2 (codecs/to-bytes test-data)))))

(doseq [alg [:ps512 :ps384 :ps256 :rs512 :rs384 :rs256]]
  (let [header-data {:url "https://github.com/babashka" :nonce "borkdude"}
        res1 (jws/sign test-data rsa-privkey {:alg alg :header header-data})
        res2 (jws/unsign res1 rsa-pubkey {:alg alg})
        header (jws/decode-header res1)]
    (assert (java.util.Arrays/equals res2 (codecs/to-bytes test-data)))
    (assert (= header (merge header-data {:alg alg})))))

(doseq [alg [:es512 :es384 :es256]]
  (let [res1 (pjws/sign test-data ec-privkey {:alg alg})
        res2 (pjws/unsign res1 ec-pubkey {:alg alg})]
    (assert (java.util.Arrays/equals res2 (codecs/to-bytes test-data)))))

;; -- JWT Tests --
(require '[pod.babashka.buddy.jwt :as jwt])

(def secret (codecs/hex->bytes (str "000102030405060708090a0b0c0d0e0f"
                                 "101112131415161718191a1b1c1d1e1f")))
(def key16 (nonce/random-bytes 16))

;; encode decode jws
(doseq [alg [:hs512 :hs256]]
  (let [key "random babashka key"
        data {:a 1 :b 2 :c 3 :d 4}
        res1 (jwt/sign data key {:alg alg})
        res2 (jwt/unsign res1 key {:alg alg})]
    (assert (= res2 data))))

;; encode decode jwe
(doseq [enc [:a128gcm :a192gcm :a256gcm :a128cbc-hs256 :a192cbc-hs384 :a256cbc-hs512]]
  (let [data {:a 1 :b 2 :c 3 :d 4}
        res1 (jwt/encrypt data key16 {:alg :a128kw :enc enc :zip true})
        res2 (jwt/decrypt res1 key16 {:alg :a128kw :enc enc :zip true})]
    (assert (= res2 data))))

(when-not (= "executable" (System/getProperty "org.graalvm.nativeimage.kind"))
  (shutdown-agents)
  (System/exit 0))
