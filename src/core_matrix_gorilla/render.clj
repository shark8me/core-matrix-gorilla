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

(defn renderfn
  ([self] (renderfn self {}))
  ([self {:keys [nrows ncols] :or {nrows 10 ncols 10} :as opts}]
  (let [cnames (cd/column-names self)
        dset (if (> (count cnames) ncols)
               (cd/select-columns self (take ncols cnames)) self)
        rendfn (fn [open close sep r] (list-like (map render/render r) (pr-str r) open close sep))
        rows (map (partial rendfn "<tr><td>" "</td></tr>" "</td><td>")
                  (cm/to-nested-vectors (slice-rows dset nrows)))
        heading [(rendfn "<tr><th>" "</th></tr>" "</th><th>" (cd/column-names dset))]
        body (list-like (concat heading rows) (pr-str self) "<center><table>" "</table></center>" "\n")]
    body)))

;;Created another type just to pass parameters such as num rows and num columns to mview function
(defrecord AbridgedMatrixView  [contents opts])

(defn mview
  "view the dataset. Accepts an optional map with :nrows and :ncols keys for 
  number of rols and/or columns to display"
  ([^clojure.core.matrix.impl.dataset.DataSet dset] (mview dset {}))
  ([^clojure.core.matrix.impl.dataset.DataSet dset {:keys [nrows ncols] :or {nrows 10 ncols 10} :as opts}]
   (AbridgedMatrixView. dset opts)))
  
(extend-type AbridgedMatrixView
    render/Renderable
    (render [self & opts] (renderfn (:contents self) (:opts self))))

;extend dataset type to show in gorilla-repl interface
(extend-type clojure.core.matrix.impl.dataset.DataSet
  render/Renderable
  (render [self]
    (renderfn self)))

