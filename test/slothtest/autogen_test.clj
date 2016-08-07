(ns
 slothtest.autogen_test
 (:require [clojure.test :refer :all] [slothtest.core] [clojure.core]))

(def metadata {:apiversion 2})

(deftest
 autogen
 (testing
  "Autogenerated"
  (is (= (clojure.core/+ 1 2 3 4) '10))
  (is (= (slothtest.core/rjames 2) '6))
  (is (= (clojure.core/* 1 2) '2))
  (is (= (clojure.core// 1 2) '1/2))
  (is (= (clojure.core/for [i [1 2 3]] (clojure.core/* 2 i)) '(2 4 6)))
  (is
   (=
    (#'slothtest.core/api-v1-to-v2
     {:curr-tests {'(clojure.core/+ 1 2) 3},
      :metadata {:apiversion 1}})
    '{:metadata {:apiversion 2},
      :curr-tests [{:expression (clojure.core/+ 1 2), :result 3}],
      :expr-index {(clojure.core/+ 1 2) 0}})))
 (testing "some rnd name" (is (= (clojure.core/+ 1 2) '3))))

(deftest
 mclassen
 (testing "Autogenerated" (is (= (clojure.core/+ 1 2 3) '6))))

(def
 test-data
 [{:expression '(clojure.core/+ 1 2 3 4), :result '10}
  {:expression '(slothtest.core/rjames 2), :result '6}
  {:expression '(clojure.core/* 1 2), :result '2}
  {:expression '(clojure.core// 1 2), :result '1/2}
  {:expression '(clojure.core/for [i [1 2 3]] (clojure.core/* 2 i)),
   :result '(2 4 6)}
  {:suite "mclassen", :expression '(clojure.core/+ 1 2 3), :result '6}
  {:expression
   '(#'slothtest.core/api-v1-to-v2
     {:curr-tests {'(clojure.core/+ 1 2) 3},
      :metadata {:apiversion 1}}),
   :result
   '{:metadata {:apiversion 2},
     :curr-tests [{:expression (clojure.core/+ 1 2), :result 3}],
     :expr-index {(clojure.core/+ 1 2) 0}}}
  {:type :equality,
   :expression '(clojure.core/+ 1 2),
   :result '3,
   :description "some rnd name"}])