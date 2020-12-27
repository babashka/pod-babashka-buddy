# Pod-babashka-buddy

## API

### pod.babashka.buddy.core.hash

- `sha256`: converts string to base64 string encoded as sha256

## Example

``` clojure
(require '[babashka.pods :as pods])

(pods/load-pod "./pod-babashka-buddy") ;; for testing use: ["clojure" "-M" "-m" "pod.babashka.buddy"]

(require '[pod.babashka.buddy.core.hash :as h])

(prn (h/sha256 "foo")) ;;=> "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae"
```

## Build

Run `script/compile`. This requires `GRAALVM_HOME` to be set.

## License

Copyright © 2020 Michiel Borkent

Distributed under the Apache 2.0 License. See LICENSE.
