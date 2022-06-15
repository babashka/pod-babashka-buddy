# pod-babashka-buddy

## API

This pod uses the namespace scheme `buddy.x` -> `pod.babashka.buddy.x`.
For buddy's documentation, go [here](https://funcool.github.io/buddy-core/latest/api/index.html).

Available namespaces:

- `pod.babashka.buddy.core.hash`
- `pod.babashka.buddy.core.mac`
- `pod.babashka.buddy.core.nonce`
- `pod.babashka.buddy.core.kdf`
- `pod.babashka.buddy.sign.jwe`

If you are missing functionality, please create an issue.

### KDF

Note that `pod.babashka.buddy.core.kdf` deviates from buddy's documented API
because the `buddy.core.kdf/engine` fn returns an instance of
`org.bouncycastle.crypto.generators.HKDFBytesGenerator` which can't be
serialized back to the pod client.

Instead we wrap engine results in a call to `buddy.core.kdf/get-bytes` and
return the byte array.

The fn that does this is named `pod.babashka.buddy.core.kdf/get-engine-bytes`
and that is all that this pod exposes from that namespace.

You call it with a map just like `engine`, but you need to add a `:length` key
that gets passed to `buddy.core.kdf/get-bytes`.

### Keys

Note that `pod.babashka.buddy.core.keys` deviates from buddy's documented API
because the function originally would return a Bouncy Castle key which can't
be serialized back to the pod client.

Instead the following functions will return the encoded byte-array of the key:
  - `private-key`
  - `public-key`
  - `str->public-key`
  - `str->private-key`
  - `jwk->private-key`
  - `jwk->public-key`

### JWS

Note that `pod.babashka.buddy.sign.jws` has some functions that have a key
as a parameter, these deviate from buddy's documented API and now will expect
a encoded byte-array of the key. The affected functions are:
  - `sign`
  - `unsign`

### JWT
Note that `pod.babashka.buddy.sign.jwt` has some functions that have a key
as a parameter, these deviate from buddy's documented API and now will expect
a encoded byte-array of the key. The affected functions are:
  - `sign`
  - `unsign`
  - `encrypt`
  - `decrypt`

## Example

``` clojure
(require '[babashka.pods :as pods])

(pods/load-pod 'org.babashka/buddy "0.1.0")

(require '[clojure.string :as str]
         '[pod.babashka.buddy.core.codecs :as codecs]
         '[pod.babashka.buddy.core.mac :as mac]
         '[pod.babashka.buddy.core.nonce :as nonce])

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
