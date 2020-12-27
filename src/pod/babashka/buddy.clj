(ns pod.babashka.buddy
  (:refer-clojure :exclude [read read-string hash])
  (:require [bencode.core :as bencode]
            [buddy.core.codecs :as codecs]
            [buddy.core.hash :as hash]
            [buddy.core.mac :as mac]
            [buddy.core.nonce :as nonce]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [cognitect.transit :as transit])
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

(def lookup*
  {'pod.babashka.buddy.hash
   {'ripemd256 hash/ripemd256
    'sha3-512 hash/sha3-512
    'blake2b hash/blake2b
    'md5 hash/md5
    'skein-1024 hash/skein-1024
    'blake2b-128 hash/blake2b-128
    'ripemd128 hash/ripemd128
    'ripemd320 hash/ripemd320
    'skein-256 hash/skein-256
    'skein-512 hash/skein-512
    'blake2b-512 'hash/blake2b-512
    'sha3-384 hash/sha3-384
    'whirlpool hash/whirlpool
    'blake2b-256 hash/blake2b-256
    'digest hash/digest
    'sha3-256 hash/sha3-256
    'sha1 hash/sha1
    'sha384 hash/sha384
    'skein hash/skein
    'sha512 hash/sha512
    'ripemd160 hash/ripemd160
    'sha256 hash/sha256}
   'pod.babashka.buddy.mac
   {'hash mac/hash
    'verify mac/verify}
   'pod.babashka.buddy.nonce
   {'random-bytes nonce/random-bytes
    'random-nonce nonce/random-nonce}
   'pod.babashka.buddy.codecs
   {'bytes->hex codecs/bytes->hex
    'bytes->long codecs/bytes->long
    'bytes->str codecs/bytes->str
    'hex->bytes codecs/hex->bytes
    'long->bytes codecs/long->bytes
    'str->bytes codecs/str->bytes
    'to-bytes codecs/to-bytes}})

(defn lookup [var]
  (let [var-ns (symbol (namespace var))
        var-name (symbol (name var))]
    (get-in lookup* [var-ns var-name])))

(def describe-map
  (walk/postwalk
   (fn [v]
     (if (ident? v) (name v)
         v))
   `{:format :transit+json
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
                                (get lookup* 'pod.babashka.buddy.nonce))}
                  {:name pod.babashka.buddy.codecs
                   :vars ~(mapv (fn [[k _]]
                                  {:name k})
                                (get lookup* 'pod.babashka.buddy.codecs))}]}))

(defn read-transit [^String v]
  (transit/read
   (transit/reader
    (java.io.ByteArrayInputStream. (.getBytes v "utf-8"))
    :json)))

(defn write-transit [v]
  (let [baos (java.io.ByteArrayOutputStream.)]
    (transit/write (transit/writer baos :json) v)
    (.toString baos "utf-8")))

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
                                args (read-transit args)]
                            (if-let [f (lookup var)]
                              (let [value (write-transit (apply f args))
                                    reply {"value" value
                                           "id" id
                                           "status" ["done"]}]
                                (write stdout reply))
                              (throw (ex-info (str "Var not found: " var) {}))))
                          (catch Throwable e
                            (debug e)
                            (let [reply {"ex-message" (ex-message e)
                                         "ex-data" (write-transit
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
