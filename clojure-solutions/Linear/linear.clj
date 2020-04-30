(defn checkers [f] (fn [& v] (every? identity (mapv f v))))
(defn checkVectors [& v] (apply == (mapv count v)))
(defn isVector [a] (and (vector? a) (apply (checkers number?) a)))
(defn isMatrix [a] (and (vector? a) (apply (checkers isVector) a) (apply checkVectors a)))
(defn isTensor [& a] (or (apply (checkers number?) a)
                         (and (apply (checkers vector?) a) (apply checkVectors a) (apply isTensor (apply concat [] a)))))
(defn oper [f is v]
  { :pre  [(apply (checkers is) v) (apply checkVectors v)]} (apply mapv f v))
(defn v+ [& v] (oper + isVector v))
(defn v* [& v] (oper * isVector v))
(defn v- [& v] (oper - isVector v))
(defn v*s [a & b]
  { :pre [(isVector a) (apply (checkers number?) b)]}
  (let [c (apply * b)]
    (mapv (fn [x] (* x c)) a)))
(defn scalar [a b]
  { :pre [(isVector a) (isVector b) (checkVectors a b)]}
  (apply + (mapv * a b)))
(defn vect [& v]
  { :pre [(apply (checkers isVector) v) (apply checkVectors v) (== (count (first v)) 3)]}
  (reduce (fn [a b]
            (letfn [(A [i j] (- (* (nth a i) (nth b j)) (* (nth a j) (nth b i))))]
              (vector (A 1 2) (A 2 0) (A 0 1)))) v))
(defn m+ [& m] (oper v+ isMatrix m))
(defn m* [& m] (oper v* isMatrix m))
(defn m- [& m] (oper v- isMatrix m))
(defn m*s [a & b]
  { :pre [(isMatrix a) (apply (checkers number?) b)]}
  (let [c (apply * b)] (mapv (fn [x] (v*s x c)) a)))
(defn m*v [a b]
  { :pre [(isMatrix a) (isVector b) (== (count b) (count (first a)))]}
  (mapv (fn [x] (scalar x b)) a))
(defn transpose [a]
  { :pre [isMatrix a]}
  (apply mapv vector a))
(defn mm [a b]
  (transpose (mapv (fn [x] (m*v a x)) (transpose b))))
(defn m*m [& m] (reduce mm m))
(defn t+ [& t] (if (isVector (first t)) (apply v+ t) (oper t+ isTensor t)))
(defn t- [& t] (if (isVector (first t)) (apply v- t) (oper t- isTensor t)))
(defn t* [& t] (if (isVector (first t)) (apply v* t) (oper t* isTensor t)))
