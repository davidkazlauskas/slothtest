(ns slothtest.autogen_test (:require [clojure.test :refer :all]))

(def test-data {'(* 1 2) 2, '(+ 1 2) 3, '(/ 1 2) 1/2})

(deftest
 autogen
 (testing
  "Autogenerated"
  (is (= (* 1 2) 2))
  (is (= (+ 1 2) 3))
  (is (= (/ 1 2) 1/2))))