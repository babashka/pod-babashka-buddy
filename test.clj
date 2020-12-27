#!/usr/bin/env bb

(require '[babashka.pods :as pods])

(if (= "executable" (System/getProperty "org.graalvm.nativeimage.kind"))
  (pods/load-pod "./pod-babashka-buddy")
  (pods/load-pod ["clojure" "-M" "-m" "pod.babashka.buddy"]))

(require '[pod.babashka.buddy.core.hash :as h])

(prn (h/sha256 "foo")) ;;=> "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae"

(require '[pod.babashka.buddy.core.mac :as mac])

(prn (mac/hash "foo bar" {:key "mysecretkey" :alg :hmac+sha256}))

(when-not (= "executable" (System/getProperty "org.graalvm.nativeimage.kind"))
  (shutdown-agents)
  (System/exit 0))
