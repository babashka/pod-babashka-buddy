# Pod-babashka-buddy

## API

### pod.babashka.buddy.core.hash

- `sha256`: `(sha-256 input)`. Converts input string to base64 string encoded as
  sha256.

### pod.babashka.buddy.core.mac

- `hash`: `(hash input engine-or-options)`. Generate hmac digest as base64
  string for string input data, a secret key and hash algorithm. If algorithm
  is not supplied, sha256 will be used as default value.

#### pod.babashka.buddy.nonce

- `random-bytes`: `(random-bytes numbytes)`. Returns random bytes as base64 string.

## Example

``` clojure
(require '[babashka.pods :as pods])

(pods/load-pod "./pod-babashka-buddy")

(require '[pod.babashka.buddy.hash :as h])

(prn (h/sha256 "foo"))
;;=> "LCa0a2j/xo/5m0U8HTBBNBNCLXBkg7+g+YpeiGJm564="

(require '[pod.babashka.buddy.mac :as mac])

(prn (mac/hash "foo bar" {:key "mysecretkey" :alg :hmac+sha256}))
;;=> "YYSUSL27Z7OdYJRx7q1mfmWw0bngGxw796pWuD6cgIM="
```

## Build

Run `script/compile`. This requires `GRAALVM_HOME` to be set.

## Test

To test the pod code with JVM clojure, run `clojure -M test.clj`.

To test the native image with bb, run `bb test.clj`.

## License

Copyright © 2020 Michiel Borkent

Distributed under the Apache 2.0 License. See LICENSE.
