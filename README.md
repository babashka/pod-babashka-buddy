# pod-babashka-buddy

## Usage

``` clojure
(require '[babashka.pods :as pods])

(pods/load-pod 'org.babashka/buddy "0.3.4")

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

## API

This pod uses the namespace scheme `buddy.x` -> `pod.babashka.buddy.x`.
For buddy's documentation, go [here](https://funcool.github.io/buddy-core/latest/api/index.html) and
[here](https://funcool.github.io/buddy-hashers/latest/user-guide.html) for buddy-hashers.

Available namespaces:

- `pod.babashka.buddy.core.crypto`
- `pod.babashka.buddy.core.codecs`
- `pod.babashka.buddy.core.hash`
- `pod.babashka.buddy.core.kdf`
- `pod.babashka.buddy.core.keys`
- `pod.babashka.buddy.core.mac`
- `pod.babashka.buddy.core.nonce`
- `pod.babashka.buddy.sign.jwe`
- `pod.babashka.buddy.sign.jws`
- `pod.babashka.buddy.sign.jwt`
- `pod.babashka.buddy.hashers`

The `.core` part may be left out for brevity as the same namespaces are mirrored under the shorter names as well.

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

### Crypto

Note that `pod.babashka.buddy.core.crypto` deviates from buddy's documented
API because the `buddy.core.crypto/engine` returns an instance of
`org.bouncycastle.crypto.generators.HKDFBytesGenerator` which can't be
serialized back to the pod client. The same happens with several of the
other low-level functions available in that namespace.

So the same approach used by for KDF functions is used in
`pod.babashka.buddy.core.crypto`: compose the problematic functions
together inside the pod function, and always return serializable values.

Just a very small subset of what is available in `buddy.core.crypto`
namespace is exposed. The rationale for this is that a babahska pod user
won't generally need or use the low-level cryptographic primitives, or know
what cryptographic constructs may offer which security properties (like
authenticated encryption, etc).

So the set of exposed functions are those that are sane and secure enough
for most people, while still providing choice:

- the block ciphers with authenticated encryption offered by
  `buddy.core.crypto/encrypt` and `buddy.core.crypto/decrypt`.

- the stream ciphers offered by `buddy.core.crypto/stream-cipher`
  (ChaCha20), even if they don't provide authenticated encryption at the
  moment. But once `buddy.core.crypto` adds support for authenticated
  encryption constructs (e.g., ChaCha20+Poly1305) we could add support for
  them.

In order to use the block ciphers with authenticated encryption you need to
use `pod.babashka.buddy.core.crypto/block-cipher-encrypt` and
`pod.babashka.buddy.core.crypto/block-cipher-decrypt` functions. They
accept the same parameters as the `buddy.core.crypto/encrypt` and
`buddy.core.crypto/decrypt` functions. Both functions return a byte array.

In order to use the stream ciphers, you need to use
`pod.babashka.buddy.core.crypto/stream-cipher-encrypt` and
`pod.babashka.buddy.core.crypto/stream-cipher-decrypt` functions.

They accept an `input` argument (that is passed to
`buddy.core.crypto/process-bytes!` as its `in` argument), a `key` and `iv`
arguments (that are passed to `buddy.core.crypto/init!` as the same key
names in the `params` argument), and an `alg` argument (that is passed to
`buddy.core.crypto/stream-cipher` as its argument with the same name). The
idea is to keep the type of arguments and their ordering as similar to the
block cipher functions as possible.

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

### Hashers
The `derive` and `verify` functions are exposed. Remember to set the `:salt` in
the options map supplied to `derive` or you'll get a different hash each time.

## Build

Run `script/compile`. This requires `GRAALVM_HOME` to be set.

## Test

To test the pod code with JVM clojure, run `clojure -M test.clj`.

To test the native image with bb, run `bb test.clj`.

## License

Copyright © 2020 - 2023 Michiel Borkent

Distributed under the Apache 2.0 License. See LICENSE.
