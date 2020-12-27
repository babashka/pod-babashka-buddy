# pod-babashka-buddy

## API

This pod uses the namespace scheme `buddy.x` -> `pod.babashka.buddy.x`.
For buddy's documentation, go [here](https://funcool.github.io/buddy-core/latest/api/index.html).

Available namespaces:

- `pod.babashka.buddy.hash`
- `pod.babashka.buddy.mac`
- `pod.babashka.buddy.nonce`

If you are missing functionality, please create an issue.

## Example

``` clojure
(require '[babashka.pods :as pods])

(pods/load-pod "./pod-babashka-buddy")

(require '[clojure.string :as str]
         '[pod.babashka.buddy.codecs :as codecs]
         '[pod.babashka.buddy.mac :as mac]
         '[pod.babashka.buddy.nonce :as nonce])

(def hash-algorithm :hmac+sha256)
(def secret (nonce/random-bytes 64))

(let [timestamp (System/currentTimeMillis)
      nonce (nonce/random-bytes 64)
      nonce-hex (codecs/bytes->hex nonce)
      payload (pr-str {:nonce nonce-hex :timestamp timestamp})
      signature (codecs/bytes->hex (mac/hash payload {:alg hash-algorithm :key secret}))]
  (prn (str/join "-" [nonce-hex timestamp signature])))
```

## Build

Run `script/compile`. This requires `GRAALVM_HOME` to be set.

## Test

To test the pod code with JVM clojure, run `clojure -M test.clj`.

To test the native image with bb, run `bb test.clj`.

## License

Copyright © 2020 Michiel Borkent

Distributed under the Apache 2.0 License. See LICENSE.
