(ns notest.core)

(defn test-path []
  "./test/notest/notest_autogen.clj")

(defn read-curr-test-src []
  (try
    ; TODO: no hardcoding
    (slurp (test-path))
    (catch Exception e
      (println "Could not read test file:" e)
      "")))

; 1 - namespace declaration
; 2 - the map of sterf
; 3 - the the deftest sucka
(defn structure-test [the-struct]
  {:namespace (first the-struct)
   :curr-tests (second the-struct)
   :testdef (get the-struct 2)})

(defn default-struct []
  ; TODO: no hardcoding
  {:namespace '(ns notest.autogen_test)
   :curr-tests '(def test-data {})
   :testdef '(deftest autogen
               (testing "Autogenerated"
                 (doseq [[func res] test-data]
                   (is (= (eval func) (eval res))))))})

(defn ppr [the-struct]
  (clojure.pprint/write
    the-struct :stream nil))

(defn struct-to-source [the-struct]
  (clojure.string/join "\n\n"
                       [(ppr (:namespace the-struct))
                        (ppr (:curr-tests the-struct))
                        (ppr (:testdef the-struct))]))

(defn save-struct [the-struct]
  (spit (test-path)
        (struct-to-source the-struct)))

(defmacro save-spec [the-expression]
  )

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
