(ns slothtest.core
  (:require [clojure.pprint :refer [write]]))

(defn- read-project-name-from-lein []
  (str (second (read-string (slurp "./project.clj")))))

(def ^:dynamic *notestns*
  (read-project-name-from-lein))

(def ^:dynamic *testfileclass*
  "autogen_test")

(defn- notestns []
  *notestns*)

(defn- testfileclass []
  *testfileclass*)

(defn- underscores-instead-dashes [the-str]
  (.replaceAll the-str "\\-" "_"))

(defn- test-path []
  (str "./test/" (underscores-instead-dashes (notestns))
       "/" (underscores-instead-dashes (testfileclass)) ".clj"))

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
  (let [metadata (last (nth the-struct 1))
        ver (:apiversion metadata)]
    (case ver
      1 {:curr-tests (last (nth the-struct 2))
         :metadata metadata
         :testdef (last the-struct)}
      2 {:curr-tests (last (last the-struct))
         :metadata metadata
         :testdef (butlast (drop 2 the-struct))})))

(defn- test-suite [the-node]
  (:suite the-node "autogen"))

(defn- testing-desc [the-node]
  (:description the-node "Autogenerated"))

(defn- defsuite []
  "autogen")

(defn- defdesc []
  "Autogenerated")

(defn- current-ordering-map [the-arr key-fn]
  (into {}
    (reverse
      (map-indexed
        (fn [idx theval] [(key-fn theval) idx])
        the-arr))))

(defn- group-up-tests [the-arr]
  (let [ord-map (current-ordering-map the-arr test-suite)
        grouped-suites (into {}
                         (map
                           (fn [[gkey the-vec]]
                             [gkey (current-ordering-map
                                     the-vec testing-desc)])
                           (group-by test-suite the-arr)))
        perfect-sort (sort-by
                       (fn [{:keys [suite description]
                             :or {suite (defsuite)
                                  description (defdesc)}}]
                         (+
                           (* 10000 (get ord-map suite))
                           (get-in grouped-suites [suite description])))
                       < the-arr)]
        ; layering: suites, descriptions
    (loop [therest perfect-sort acc []]
      (let [curr (first therest)
            tail (rest therest)]
        (if curr
          (let [{:keys [suite description]
                 :or {suite (defsuite)
                      description (defdesc)}
                 :as the-node} curr]
            (recur
              tail
             (-> acc
              ((fn [this]
                (if (= (test-suite curr) (:sname (last this)))
                  this
                  (conj this {:sname suite
                              :desc []}))))
              ((fn [this]
                (if (= (testing-desc curr)
                       (:dname (last (:desc (last this)))))
                  this
                  (update-in this [(dec (count this))
                                   :desc]
                             (fn [descnodes]
                               (conj descnodes
                                     {:dname description
                                      :nodes []}))))))
              ((fn [this]
                (update-in this [(dec (count this))
                                 :desc
                                 (dec (count (:desc (last this))))]
                           (fn [toup]
                             (assoc toup
                                    :nodes
                                    (conj (:nodes toup) the-node)))))))))
          acc)))))

(defn- render-is-clauses [the-nodes]
  (for [i the-nodes]
    (case (:type i :equality)
      :equality
      `(~'is (~'= ~(eval (:expression i)) ~(:result i)))
      :function ; TODO: is this correct?
      `(~'is (~(:function i) ~(eval (:expression i)))))))

(defn- render-testing-node [inner-arr]
  (for [i inner-arr]
    `(~'testing
       ~(:dname i)
       ~@(render-is-clauses (:nodes i)))))

(defn- render-suites [the-arr]
  (for [i the-arr]
    `(~'deftest ~(symbol (:sname i))
       ~@(render-testing-node (:desc i)))))

(defn- gen-test-def-v2 [the-arr]
  (let [grouped (group-up-tests the-arr)]
    ; grouped:
    ;  - array of suites
    ;    - has key :sname
    ;    - has key :desc (for descriptions)
    ;      - has key :dname
    ;      - has key :nodes
    ;        - has the expressions
    (render-suites grouped)))

(defn- default-ns-decl [reqlist]
  `(~'ns ~(symbol (str (notestns) "." (testfileclass)))
     (:require [clojure.test :refer :all]
               ~@reqlist)))

(defn- default-struct []
  {:curr-tests '[]
   :metadata {:apiversion 2}})

(defn- pull-namespaces [symb-list]
  (cond
    (nil? symb-list)
      #{}
    (or (vector? symb-list) (list? symb-list) (map? symb-list) (seq? symb-list))
      (into #{} (reduce concat (map pull-namespaces symb-list)))
    (symbol? symb-list)
      (if-let [the-n (namespace symb-list)]
        #{the-n}
        #{})
    :else
      #{}))

(defn- symbol-set [the-struct]
  (into #{}
   (reduce concat
    (for [i (:curr-tests the-struct)]
      (let [{:keys [expression result function]} i]
        (-> #{}
          (into (pull-namespaces expression))
          (into (pull-namespaces result))
          (into (pull-namespaces function))))))))

(defn- drop-nils-from-map [the-map]
  (into {} (filter second the-map)))

(defn- v1-node-to-v2 [[expr res]]
  {:expression expr
   ; additional fields (first default):
   ; :type [:equality :function]
   ; :result - when type is equality
   ; :expression - expression used to gen result
   ; :function - when type is function
   ; :suite - the test suite this belongs to
   ; :description - the description
   :result res})

(defn- build-node-index [node-arr]
  (into {}
        (map-indexed
          (fn [i v] [(:expression v) i])
          node-arr)))

(defn- api-v1-to-v2 [the-struct]
  (let [{:keys [metadata curr-tests]} the-struct
        nodes (mapv v1-node-to-v2 curr-tests)]
    {:metadata (assoc metadata :apiversion 2)
     :curr-tests nodes
     :expr-index (build-node-index nodes)}))

; TODO: breakage viewer
; TODO: option to write different file

(defn- add-node-index [the-struct]
  (let [nodes (:curr-tests the-struct)
        index (build-node-index nodes)]
    (loop [curr-set #{}
           nidx []
           remnd (reverse nodes)] ; last one wins
      (let [i (first remnd)
            tail (rest remnd)
            expr (:expression i)]
       (if (nil? i)
         (-> the-struct
             (assoc :curr-tests (into [] (reverse nidx)))
             (#(assoc % :expr-index
                      (build-node-index (:curr-tests %)))))
         (if (not (curr-set expr))
           (recur (conj curr-set expr)
                  (conj nidx i)
                  tail)
           (recur curr-set
                  nidx
                  tail)))))))

(defn- upgrade-test-struct [the-struct]
  (case (get-in the-struct [:metadata :apiversion])
    1 (api-v1-to-v2 the-struct)
    2 (add-node-index the-struct)))

(defn- curr-test-struct []
  (upgrade-test-struct
    (merge
      (default-struct)
      (drop-nils-from-map
        (structure-test
          (read-string
            (str "(" (read-curr-test-src) ")")))))))

(defn- ppr [the-struct]
  (clojure.pprint/write
    the-struct :stream nil))

(defn- gen-ns-decl [the-struct]
  (default-ns-decl
    (map
      #(vector (symbol %))
      (symbol-set the-struct))))

(defn- two-nl-join [arr]
  (clojure.string/join "\n\n" arr))

(defn- struct-to-source-v2 [the-struct]
  (two-nl-join
    (concat
      [(ppr (gen-ns-decl the-struct))
       (ppr `(def ~'metadata ~(:metadata the-struct)))]
      (map ppr (gen-test-def-v2 (:curr-tests the-struct)))
      [(ppr `(def ~'test-data ~(:curr-tests the-struct)))])))

(defn- save-struct [the-struct]
  (spit (test-path)
        (struct-to-source-v2 the-struct)))

(defn- ensure-correct-suite-name [the-name]
  (if (not (re-matches #"^[-_a-zA-Z][-_a-zA-Z0-9]*$" the-name))
    (throw (RuntimeException.
             (str "Slothtest testsuite name must"
                  " be a valid symbol name without"
                  " namespace qualifiers and cannot"
                  " start with a number. Invalid name: "
                  "\"" the-name "\".")))))

(defn- add-test-expr [the-map func the-val
                      & {:keys [suite description]
                         :or {suite nil
                              description nil}}]
  (if suite (ensure-correct-suite-name suite))
  (if-let [idx (get-in the-map [:expr-index func])]
    (update-in the-map [:curr-tests idx]
               (fn [curr]
                 (-> curr
                     (assoc :expression func)
                     (assoc :result the-val)
                     ((fn [now]
                        (if suite
                          (if (= suite (defsuite))
                            (dissoc now :suite)
                            (assoc now :suite suite))
                          now)))
                     ((fn [now]
                        (if description
                          (if (= description (defdesc))
                            (dissoc now :description)
                            (assoc now :description description))
                          now))))))
    (update-in the-map [:curr-tests]
               (fn [currv]
                 (conj currv
                       {:type :equality
                        :expression func
                        :result the-val})))))

(defn- remove-test-expr [the-map func]
  (assoc-in
    the-map [:curr-tests]
    (dissoc (:curr-tests the-map) func)))

(defn- ns-resolve-list [expr]
  (eval (read-string (str "`'" expr))))

(defn- save-specification
  [expr result extra-args]
  (clojure.java.io/make-parents (test-path))
  (save-struct
    (apply
      add-test-expr (upgrade-test-struct (curr-test-struct))
                    expr result extra-args)))

; testing function namespace resolution
(defn rjames [x]
  (* 2 x))

(defn- drop-specification [expr]
  (save-struct
    (remove-test-expr (curr-test-struct) expr)))

(defmacro save-spec [the-expression & extraargs]
  "Use this for simple evaluations with namespace resolution.
  Like: (save-spec (+ 1 2 3))
  "
  (save-specification
    (ns-resolve-list the-expression)
    `'~(eval (eval (ns-resolve-list the-expression)))
    (map eval extraargs)))

(defmacro save-spec-sq
  "Use this for syntax quoted blocks (if you need locals and stuff)."
  [the-expression & extraargs]
  (save-specification
    `'~(eval the-expression)
    `'~(eval (eval the-expression))
    (map eval extraargs)))

(defmacro expand-spec-sq [the-expression]
  {:expression `'~(eval the-expression)
   :result `'~(eval (eval the-expression))})

(defmacro expand-spec [the-expression]
  {:expression (ns-resolve-list the-expression)
   :result `~(eval (ns-resolve-list the-expression))})

(defmacro expect-spec [the-expression result]
  (save-specification (ns-resolve-list the-expression) `'~result))

(defmacro remove-spec [the-expression]
  (drop-specification (ns-resolve-list the-expression)))

(defmacro slothtest-ns [new-namespace]
  `(def ^:dynamic *notestns* ~new-namespace))

(defmacro slothtest-class [new-class]
  `(def ^:dynamic *testfileclass* ~new-class))

(comment
  "Execute this test suite, generated sources should be identical."

  (do
    (save-spec (+ 1 2 3 4))
    (save-spec (rjames 2))

    (save-spec (+ 1 2))
    (save-spec (* 1 2))
    (save-spec (/ 1 2))

    (save-spec-sq
      `(for [~'i [1 2 3]] (* 2 ~'i)))
    )
  )

(comment
  "Examples, evaluate for validation:"
  (=
   (expand-spec-sq
    `(for [~'i [1 2 3]] (* 2 ~'i)))
   {:expression
      '(clojure.core/for [i [1 2 3]]
        (clojure.core/* 2 i))
    :result '(2 4 6)})

  (=
   (expand-spec (rjames 4))
   {:expression '(slothtest.core/rjames 4)
    :result '8}))
