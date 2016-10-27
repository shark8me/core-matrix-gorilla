(ns core-matrix-gorilla.render
  (:require [clojure.data.codec.base64 :as b64]
            [clojure.core.matrix.impl.dataset :as md]
            [clojure.core.matrix.dataset :as cd]
            [clojure.core.matrix :as cm]
            [gorilla-renderable.core :as render]))

(defn list-like
  "util function used in render"
  [data value open close separator]
  {:type :list-like
   :open open
   :close close
   :separator separator
   :items data
   :value value})

(defn slice-rows 
  ([dset] (slice-rows dset 10))
  ([dset n]
  (if (< (-> dset cm/shape first) n)
    dset
    (cd/select-rows dset (range n)))))

 ;extend dataset type to show in gorilla-repl interface
(extend-type clojure.core.matrix.impl.dataset.DataSet
    render/Renderable
    (render [self & {:keys [rows cols except-rows except-cols filter-fn all] :as opts}]
            (let [rendfn (fn [open close sep r] (list-like (map render/render r) (pr-str r) open close sep))
                  rows (map (partial rendfn "<tr><td>" "</td></tr>" "</td><td>") 
                           (cm/to-nested-vectors (slice-rows self)) )
                  heading (if-let [cols (cd/column-names self)]
                            [(rendfn "<tr><th>" "</th></tr>" "</th><th>" cols)]
                            [])
                  body (list-like (concat heading rows) (pr-str self) "<center><table>" "</table></center>" "\n")]
              body)))
