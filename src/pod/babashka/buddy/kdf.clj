(ns pod.babashka.buddy.kdf
  (:require [buddy.core.kdf :as kdf]))

(defn get-engine-bytes
  "buddy.core.kdf/enging composed with buddy.core.kdf/get-bytes.
  Takes the same map argument as buddy.core.kdf/engine plus a :length key for
  the get-bytes argument."
  [{:keys [length] :as params}]
  (-> (kdf/engine params)
      (kdf/get-bytes length)))
