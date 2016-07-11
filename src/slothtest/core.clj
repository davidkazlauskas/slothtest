(ns slothtest.core
  (:require [clojure.pprint :refer [write]]))

(defn- read-project-name-from-lein []
  (str (second (read-string (slurp "./project.clj")))))

(def ^:dynamic *notestns*
  (read-project-name-from-lein))

(defn- test-path []
  (str "./test/" *notestns* "/autogen_test.clj"))

(defn- read-curr-test-src []
  (try
    (slurp (test-path))
    (catch Exception e
      (println "Could not read test file:" e)
      "")))

; 1 - namespace declaration
; 2 - the map of sterf
; 3 - the the deftest sucka
(defn- structure-test [the-struct]
  {:namespace (first the-struct)
   :curr-tests (last (second the-struct))
   :testdef (last the-struct)})

(defn- gen-test-def [the-map]
  `(~'deftest ~'autogen
     (~'testing "Autogenerated"
       ~@(for [[func res] the-map]
          `(~'is (~'= ~(eval func) ~res))))))

(defn- default-struct []
  ; TODO: no hardcoding
  {:namespace `(~'ns ~(symbol (str *notestns* ".autogen_test"))
                 (:require [clojure.test :refer :all]))
   :curr-tests '{}
   :testdef (gen-test-def {})})

(defn- drop-nils-from-map [the-map]
  (into {} (filter second the-map)))

(defn- curr-test-struct []
  (merge
    (default-struct)
    (drop-nils-from-map
      (structure-test
        (read-string
          (str "(" (read-curr-test-src) ")"))))))

(defn- ppr [the-struct]
  (clojure.pprint/write
    the-struct :stream nil))

(defn- struct-to-source [the-struct]
  (clojure.string/join "\n\n"
                       [(ppr (:namespace the-struct))
                        (ppr `(def ~'test-data ~(:curr-tests the-struct)))
                        (ppr (gen-test-def (:curr-tests the-struct)))]))

(defn- save-struct [the-struct]
  (spit (test-path)
        (struct-to-source the-struct)))

(defn- add-test-expr [the-map func the-val]
  (assoc-in the-map [:curr-tests func] the-val))

(defn remove-test-expr [the-map func]
  (assoc-in
    the-map [:curr-tests]
    (dissoc (:curr-tests the-map) func)))

(defn- save-specification [expr result]
  (clojure.java.io/make-parents (test-path))
  (save-struct
    (add-test-expr (curr-test-struct) expr result)))

(defn- drop-specification [expr]
  (save-struct
    (remove-test-expr (curr-test-struct) expr)))

(defmacro save-spec [the-expression]
  (save-specification `'~the-expression (eval the-expression)))

(defmacro expect-spec [the-expression result]
  (save-specification `'~the-expression result))

(defmacro remove-spec [the-expression]
  (drop-specification `'~the-expression))

(defmacro slothtest-ns [new-namespace]
  `(def ^:dynamic *notestns* ~new-namespace))
