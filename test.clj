#!/usr/bin/env bb

(require '[babashka.pods :as pods])

(pods/load-pod "./pod-babashka-buddy" #_["clojure" "-M" "-m" "pod.babashka.buddy"])

(require '[pod.babashka.buddy.core.hash :as h])

(prn (h/sha256 "foo")) ;;=> "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae"
