(ns notest.core)

(defn read-curr-test-src []
  (slurp "./test/notest/notest_autogen.clj"))

(defmacro save-spec [the-expression]
  )

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
