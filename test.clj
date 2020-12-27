#!/usr/bin/env bb

(require '[babashka.pods :as pods])

(if (= "executable" (System/getProperty "org.graalvm.nativeimage.kind"))
  (pods/load-pod "./pod-babashka-buddy")
  (pods/load-pod ["clojure" "-M" "-m" "pod.babashka.buddy"]))

(require '[pod.babashka.buddy.hash :as h])

(prn (h/sha256 "foo"))

(require '[pod.babashka.buddy.mac :as mac])

(prn (mac/hash "foo bar" {:key "mysecretkey" :alg :hmac+sha256}))

(require '[pod.babashka.buddy.nonce :as nonce])

(prn (nonce/random-bytes 64))

(def hash-algorithm :hmac+sha256)
(def secret (nonce/random-bytes 64))

(require '[clojure.string :as string]
         '[pod.babashka.buddy.codecs :as codecs])

(let [timestamp (System/currentTimeMillis)
      nonce (nonce/random-bytes 64)
      nonce-hex (codecs/bytes->hex nonce)
      payload (pr-str {:nonce nonce-hex :timestamp timestamp})
      signature (codecs/bytes->hex (mac/hash payload {:alg hash-algorithm :key secret}))]
  (prn (string/join "-" [nonce-hex timestamp signature])))

(when-not (= "executable" (System/getProperty "org.graalvm.nativeimage.kind"))
  (shutdown-agents)
  (System/exit 0))
