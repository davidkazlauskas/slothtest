# slothtest

Is this you?

![Sloth](http://pmdvod.nationalgeographic.com/NG_Video/742/275/140715-baby-sloth-orphanage-rescue-vin_640x360_304699971673.jpg)

Then you've found a perfect testing solution for clojure!

We tend to do a lot of testing in clojure when we evaluate repl expressions.
Now you have power to save your evaluated repl commands without doing anything.

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

Default test namespace is read from project.clj (as project name),
however, it may be changed with:
```clojure
(slothtest-ns somenamespace)
```

Happy time spent developing instead of writing tedious repetitive tests!
