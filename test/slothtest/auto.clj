(ns slothtest.auto (:require [clojure.test :refer :all] [clojure.core]))

(def metadata {:apiversion 2})

(deftest
 some-suite
 (testing "Some test" (is (= (clojure.core/+ 1 2) '3))))

(def
 test-data
 [{:type :equality,
   :expression '(clojure.core/+ 1 2),
   :result '3,
   :suite "some-suite",
   :description "Some test"}])
