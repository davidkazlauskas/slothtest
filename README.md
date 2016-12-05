# slothtest

[![Clojars Project](https://img.shields.io/clojars/v/slothtest.svg)](https://clojars.org/slothtest)

Is this you?

![Sloth](http://pmdvod.nationalgeographic.com/NG_Video/742/275/140715-baby-sloth-orphanage-rescue-vin_640x360_304699971673.jpg)

Then you've found a perfect testing solution for clojure!

We tend to do a lot of testing in clojure when we evaluate repl expressions.
Now you have power to save your evaluated repl commands without doing anything.

## Example

Say, you want to ensure that (+ 1 2) is always 3 but you don't want to write test. How to do that?

Evaluate expression:
```clojure
(+ 1 2)
```

If you like the result simply evaluate it with :

```clojure
(ns mynamespace.core
  (:require [slothtest.core :refer :all]))

(save-spec (+ 1 2))
```

This will evaluate (+ 1 2) and save the result in source
test/mynamespace/autogen_test.clj. Of course, it is a good idea
to only test pure functions with sloth test.

You did not even have to specify the result. However, if you
wanted you could:

```clojure
(ns mynamespace.core
  (:require [slothtest.core :refer :all]))

(expect-spec (+ 1 2) 3)
```

## What about duplicates?

If you evaluate (+ 1 2) list multiple times the last one will be saved.
(+ 1 2) is saved as a simple list, therefore, has unique key in the hash map.

## Removing expression

If something got stale and you don't want to open test generation file remove it with:

```clojure
(remove-spec (+ 1 2))
```

Symbols don't have to exist, so, if function is gone, you can just remove typing
non-existant expression.

## Syntax quoted tests

If you need evaluate expression with local variables it could be done like so:

```clojure
(save-spec-sq
  `(for [~'i [1 2 3]] (* 2 ~'i)))
```

This will save result as (2 4 6). However, ***avoid using # notation for
locals as these will always be unique and will not be replaced on multiple evaluations.***

## Namespaces

Default test namespace is read from project.clj (as project name),
however, it may be changed with:
```clojure
(slothtest-ns somenamespace)
```

## Test class name

Default test source name is "autogen_test", this
can be changed with
```clojure
(slothtest-class someclassname)
```

## Breakage viewer

Starting with slothtest 0.3.0 breakage viewer
is added. Say you break some tests and want
to review where. You don't have to leave repl.
You can just run:
```clojure
(update-breakage)
```

If repl returns anything above 0, some breakage was introduced.

To see the next expression of what broke call:
```clojure
(next-expression)
```

You can diff it with:
```clojure
(diff-next-breakage)
```

This calls external diff command to differentiate output as text.
Default diff command might be replaced with environment variable:
```sh
# default
export SLOTHTEST_DIFFCMD="diff -y '%1$s' '%2$s'"

# using python cdiff tool, copy temporary files as sloth.a and sloth.b and view it as colored diff
# then you can watch sloth.diff in the next window seeing colored output.
# BY THE WAY, If someone knows how to make vim-fireplace plugin show ansi-colored text in evaluation output
# please tell me.
export SLOTHTEST_DIFFCMD="cp '%1\$s' ./sloth.a && cp '%2\$s' ./sloth.b && diff -u '%1\$s' '%2\$s' | cdiff -c always -s -w 50 | tee ./sloth.diff"
```

To get all the data about next breakage call:
```clojure
(next-breakage)
```

Say you have decided if breakage is appropriate. Now you have three choices:
- Skip breakage - will skip it, and will move to the next one, it will appear again calling update-breakage
- Try skip breakage - run next breakage test again and if it succeeds skip it
- Approve breakage - agree with it and update the test result
- Delete breakage - delete the entire test that broke

Skip:
```clojure
(skip-next-breakage)
```

Try to skip breakage if the test passes gain:
```clojure
(try-skip-next-breakage)
```

Approve:
```clojure
(approve-next-breakage)
```

Delete:
```clojure
(delete-next-breakage)
```

## Capture expression in flight

Ever had some deep nested code and would like to take a snapshot
of a function call with arguments in specific time and specific place?
Now you can do that. Simply wrap any expression in your code with
capture-function and view it later, if you like the result - save it.

```clojure
; seamless: program flow is uninterrupted
; and result saved.
(do
  (* 3
    (capture-function (+ (- 3 2) 2))))
```

View the results:
```clojure
; view last result
(is (= (last-capture) {:inputs (clojure.core/+ 1 2), :outputs 3}))

; pretty print last result
(view-last-capture)

; Check if calling function again with
; same parameters still yields the same result
(last-capture-still-valid)

; Call last capture and return the result
(call-last-capture)

; Save last capture to test suite.
; Optional test suite name and specification
; description arguments are supported.
(save-last-capture :suite "some_suite")
```

## Selecting test suite name and specification description

Default test suite name will be "autogen" and default description
will be "Autogenerated". Someone might want to split these up,
make test suite name descriptive. They can do so with:
```clojure
(save-spec (+ 1 2) :suite "some_valid_symbol" :description "This tests basic arithmetic")
```
## Renaming symbol in tests automatically

Feared of renaming a function? Rename them in tests with one call to rename-symbol

```clojure
; find every symbol in tests to 'some.namespace/foo
; and replace it with 'some.namespace/bar
(rename-symbol 'some.namespace/foo 'some.namespace/bar)
```

## Nested unwanted values in map

From time to time test maps have some indeterministic values, like
random numbers. They can be removed simply by dissociating
next expression when updating breakages. More than one key can be dissociated
from a map.

```clojure
; expression with random number
(save-spec {:a 1 :b {:c (rand-int 100)}})

; update breakage - chances are, this function is breaking when run again
(update-breakage)

; dissociate the key that you want to remove in map (multiple keys can be added)
(dissoc-next-expression :b :c)

; once it is good enough approve the breakage
(approve-next-breakage)

```

Happy time spent developing instead of writing tedious repetitive tests!
