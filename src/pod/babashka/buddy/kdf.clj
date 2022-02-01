(ns pod.babashka.buddy.kdf
  (:require [buddy.core.kdf :as kdf]))

(defn get-bytes-from-engine
  [{:keys [length] :as params}]
  (-> (kdf/engine params)
      (kdf/get-bytes length)))
