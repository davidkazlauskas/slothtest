(ns slothtest.autogen_test (:require [clojure.test :refer :all]))

(def test-data {(+ 1 2) 3, (* 1 2) 2})

(deftest
 autogen
 (testing
  "Autogenerated"
  (doseq [[func res] test-data] (is (= (eval func) (eval res))))))