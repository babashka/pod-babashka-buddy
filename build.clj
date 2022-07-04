(ns build
  (:require [clojure.tools.build.api :as b]))

(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn" :aliases [:native]}))
(def uber-file (format "pod-babashka-buddy.jar"))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/javac {:src-dirs   ["java"]
            :class-dir  class-dir
            :basis      basis
            :javac-opts ["-source" "8" "-target" "8"]})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :ns-compile '[pod.babashka.buddy]})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'pod.babashka.buddy}))
