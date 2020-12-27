(ns pod.babashka.buddy
  (:refer-clojure :exclude [read read-string hash])
  (:require [bencode.core :as bencode]
            [buddy.core.codecs :as codecs]
            [buddy.core.hash :as hash]
            [buddy.core.mac :as mac]
            [buddy.core.nonce :as nonce]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.walk :as walk])
  (:import [java.io PushbackInputStream])
  (:gen-class))

(set! *warn-on-reflection* true)

(def stdin (PushbackInputStream. System/in))
(def stdout System/out)

(def debug? false)

(defn debug [& strs]
  (when debug?
    (binding [*out* (io/writer System/err)]
      (apply prn strs))))

(defn write
  ([v] (write stdout v))
  ([stream v]
   (debug :writing v)
   (bencode/write-bencode stream v)
   (flush)))

(defn read-string [^"[B" v]
  (String. v))

(defn read [stream]
  (bencode/read-bencode stream))

(defn sha256 [s]
  (debug :x256 (codecs/bytes->b64 (hash/sha256 s)))
  (String. ^bytes (codecs/bytes->b64 (hash/sha256 s)) "utf-8"))

(defn hash [s opts]
  (String. ^bytes (codecs/bytes->b64 (mac/hash s opts)) "utf-8"))

(defn random-bytes [i]
  (String. ^bytes (codecs/bytes->b64 (nonce/random-bytes i)) "utf-8"))

(def lookup*
  {'pod.babashka.buddy.hash
   {'sha256           sha256}
   'pod.babashka.buddy.mac
   {'hash             hash}
   'pod.babashka.buddy.nonce
   {'random-bytes     random-bytes}})

(defn lookup [var]
  (let [var-ns (symbol (namespace var))
        var-name (symbol (name var))]
    (get-in lookup* [var-ns var-name])))

(def describe-map
  (walk/postwalk
   (fn [v]
     (if (ident? v) (name v)
         v))
   `{:format :edn
     :namespaces [{:name pod.babashka.buddy.hash
                   :vars ~(mapv (fn [[k _]]
                                  {:name k})
                                (get lookup* 'pod.babashka.buddy.hash))}
                  {:name pod.babashka.buddy.mac
                   :vars ~(mapv (fn [[k _]]
                                  {:name k})
                                (get lookup* 'pod.babashka.buddy.mac))}
                  {:name pod.babashka.buddy.nonce
                   :vars ~(mapv (fn [[k _]]
                                  {:name k})
                                (get lookup* 'pod.babashka.buddy.nonce))}]}))


(defn -main [& _args]
  (loop []
    (let [message (try (read stdin)
                       (catch java.io.EOFException _
                         ::EOF))]
      (when-not (identical? ::EOF message)
        (let [op (get message "op")
              op (read-string op)
              op (keyword op)
              id (some-> (get message "id")
                         read-string)
              id (or id "unknown")]
          (case op
            :describe (do (write stdout describe-map)
                          (recur))
            :invoke (do (try
                          (let [var (-> (get message "var")
                                        read-string
                                        symbol)
                                args (get message "args")
                                args (read-string args)
                                args (edn/read-string args)]
                            (if-let [f (lookup var)]
                              (let [value (pr-str (apply f args))
                                    reply {"value" value
                                           "id" id
                                           "status" ["done"]}]
                                (write stdout reply))
                              (throw (ex-info (str "Var not found: " var) {}))))
                          (catch Throwable e
                            (debug e)
                            (let [reply {"ex-message" (ex-message e)
                                         "ex-data" (pr-str
                                                    (assoc (ex-data e)
                                                           :type (class e)))
                                         "id" id
                                         "status" ["done" "error"]}]
                              (write stdout reply))))
                        (recur))
            :shutdown (System/exit 0)
            (do
              (let [reply {"ex-message" "Unknown op"
                           "ex-data" (pr-str {:op op})
                           "id" id
                           "status" ["done" "error"]}]
                (write stdout reply))
              (recur))))))))
