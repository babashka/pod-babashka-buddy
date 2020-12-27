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

(when-not (= "executable" (System/getProperty "org.graalvm.nativeimage.kind"))
  (shutdown-agents)
  (System/exit 0))
