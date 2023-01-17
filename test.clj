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

(require '[pod.babashka.buddy.core.crypto :as crypto])

(let [input "babashka rocks block encrypting!"
      input-bytes (codecs/str->bytes input)
      key "16e333b7321d3ae94299ce0dfdf6b5a5baf61f526337812744ac7aa998dd4b4163994da4d03dc93b477ed72a5627558405a065d47fff20b28d56baf673f16f3c"
      key-bytes(codecs/hex->bytes key)
      iv "81bdb9c095276c5e38a6cf380e4ea59b"
      iv-bytes (codecs/hex->bytes iv)
      options {:alg :aes256-cbc-hmac-sha512}
      output-bytes (crypto/block-cipher-encrypt input-bytes key-bytes iv-bytes options)
      output-hex (codecs/bytes->hex output-bytes)]
  (assert (= "e71d31c9d8a4c6090268048fcfda4db7ea0c2a882c8f28d0f13a0d467720106175231ba414594ff0a7388a3bb3a9c3a375e3033a23e94143f9a352917a1f4995afe94c2722a4e7ed0505e7f2b41a97df"
             output-hex))
  (assert (= (vec (crypto/block-cipher-decrypt output-bytes key-bytes iv-bytes options))
             (vec input-bytes)))
  (prn output-hex))

(let [input "babashka rocks stream encrypting!"
      input-bytes (codecs/str->bytes input)
      key "1c30156b954e528a4775e7c4e25ba9f3111b2f774dd2549bca6a7ba9bb13adfc"
      key-bytes (codecs/hex->bytes key)
      iv "8f9ba138cce6c79b"
      iv-bytes (codecs/hex->bytes iv)
      alg :chacha
      output-bytes (crypto/stream-cipher-encrypt input-bytes key-bytes iv-bytes alg)
      output-hex (codecs/bytes->hex output-bytes)]
  (assert (= "f775ed83775fada9f9db35225f6deff5076f9858e89c8ae9805797657ad71a88e3"
             output-hex))
  (assert (= (vec (crypto/stream-cipher-decrypt output-bytes key-bytes iv-bytes alg))
             (vec input-bytes)))
  (prn output-hex))

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


;; -- Keys Tests --
(require '[pod.babashka.buddy.keys :as keys])

(def ed25519-jwk-key
  {:kty "OKP"
   :crv "Ed25519"
   :d "nWGxne_9WmC6hEr0kuwsxERJxWl7MmkZcDusAxyuf2A"
   :x "11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo"})

(assert (= ed25519-jwk-key (keys/jwk (keys/jwk->private-key ed25519-jwk-key)
                             (keys/jwk->public-key ed25519-jwk-key))))
(assert (= "kPrK_qmxVWaYVA9wwBF6Iuo3vVzz7TxHCTwXBygrS4k"
          (-> ed25519-jwk-key keys/jwk-thumbprint codecs/bytes->b64u codecs/bytes->str)))


(def ed448-jwk-key
  {:kty "OKP"
   :crv "Ed448"
   :d "ZXIvEfZfm7XeT-kYVnwfIftaRZgz8Bo3b19BGMSJ2fdLaW4SRXH2sZjs2Arc8ewN3fuzWRjev3rs"
   :x "l1kFulHU9ZNQSbIxY7sYt6NCmg1S1UMm_LGJhx36fE5koFG_6esy9wX5Pt-J0xsK6aB4JFHxT3CA"})

(assert (= ed448-jwk-key (keys/jwk (keys/jwk->private-key ed448-jwk-key)
                           (keys/jwk->public-key ed448-jwk-key))))


(def rsa2048-jwk-key
  {:kty "RSA",
   :n "ofgWCuLjybRlzo0tZWJjNiuSfb4p4fAkd_wWJcyQoTbji9k0l8W26mPddxHmfHQp-Vaw-4qPCJrcS2mJPMEzP1Pt0Bm4d4QlL-yRT-SFd2lZS-pCgNMsD1W_YpRPEwOWvG6b32690r2jZ47soMZo9wGzjb_7OMg0LOL-bSf63kpaSHSXndS5z5rexMdbBYUsLA9e-KXBdQOS-UTo7WTBEMa2R2CapHg665xsmtdVMTBQY4uDZlxvb3qCo5ZwKh9kG4LT6_I5IhlJH7aGhyxXFvUK-DWNmoudF8NAco9_h9iaGNj8q2ethFkMLs91kzk2PAcDTW9gb54h4FRWyuXpoQ"
   :e "AQAB"
   :d "Eq5xpGnNCivDflJsRQBXHx1hdR1k6Ulwe2JZD50LpXyWPEAeP88vLNO97IjlA7_GQ5sLKMgvfTeXZx9SE-7YwVol2NXOoAJe46sui395IW_GO-pWJ1O0BkTGoVEn2bKVRUCgu-GjBVaYLU6f3l9kJfFNS3E0QbVdxzubSu3Mkqzjkn439X0M_V51gfpRLI9JYanrC4D4qAdGcopV_0ZHHzQlBjudU2QvXt4ehNYTCBr6XCLQUShb1juUO1ZdiYoFaFQT5Tw8bGUl_x_jTj3ccPDVZFD9pIuhLhBOneufuBiB4cS98l2SR_RQyGWSeWjnczT0QU91p1DhOVRuOopznQ"})
(assert (= rsa2048-jwk-key (keys/jwk (keys/jwk->private-key rsa2048-jwk-key)
                             (keys/jwk->public-key rsa2048-jwk-key))))


(def rsa-jwk-pubkey
  {:kty "RSA",
   :n "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMstn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbISD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw"
   :e "AQAB"
   :alg "RS256"
   :kid "2011-04-29"})

(assert (= "NzbLsXh8uDCcd-6MNwXF4W_7noWXFZAfHkxZsRGC9Xs"
          (-> rsa-jwk-pubkey keys/jwk-thumbprint codecs/bytes->b64u codecs/bytes->str)))


(def ec256-jwk-key
  {:kty "EC",
   :crv "P-256",
   :x "f83OJ3D2xF1Bg8vub9tLe1gHMzV76e8Tus9uPHvRVEU",
   :y "x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0",
   :d "jpsQnnGQmL-YBIffH1136cspYG6-0iY7X1fCE9-E9LI"})

(assert (= ec256-jwk-key (keys/jwk (keys/jwk->private-key ec256-jwk-key)
                            (keys/jwk->public-key ec256-jwk-key))))


(def ec521-jwk-key
  {:kty "EC",
   :crv "P-521",
   :x "AekpBQ8ST8a8VcfVOTNl353vSrDCLLJXmPk06wTjxrrjcBpXp5EOnYG_NjFZ6OvLFV1jSfS9tsz4qUxcWceqwQGk",
   :y "ADSmRA43Z1DSNx_RvcLI87cdL07l6jQyyBXMoxVg_l2Th-x3S1WDhjDly79ajL4Kkd0AZMaZmh9ubmf63e3kyMj2",
   :d "AY5pb7A0UFiB3RELSD64fTLOSV_jazdF7fLYyuTw8lOfRhWg6Y6rUrPAxerEzgdRhajnu0ferB0d53vM9mE15j2C"})

(assert (= ec521-jwk-key (keys/jwk (keys/jwk->private-key ec521-jwk-key)
                           (keys/jwk->public-key ec521-jwk-key))))


;; -- JWS Tests --
(require '[pod.babashka.buddy.sign.jws :as jws])

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
  (let [res1 (jws/sign test-data ec-privkey {:alg alg})
        res2 (jws/unsign res1 ec-pubkey {:alg alg})]
    (assert (java.util.Arrays/equals res2 (codecs/to-bytes test-data)))))

;; -- JWT Tests --
(require '[pod.babashka.buddy.sign.jwt :as jwt])

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
