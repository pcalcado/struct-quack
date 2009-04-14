(use 'struct-quack)

(defn assert-equals [expected found]
  (if (not (= found expected))
    (throw (Exception. (str "Expected: [" expected  "] Found: [" found "]")))))
    
(defn fail-test []
  (throw (Exception. "Test failed")))

;is based on a struct defined using 'defstruct'
(defstruct user :login :password)
(let [my-user (struct-quack user :login "pcalcado" :password "123")]
  (assert-equals "pcalcado" (:login my-user))
  (assert-equals "123" (:password my-user)))
      
;has its defining struct as metadata
(defstruct car :color :year)
(let [my-car (struct-quack car :color :blue :year 1999)]
      (assert-equals (symbol "car") (:struct-type ^my-car)))
      
;throws exception when trying to get inexistent slot
(defstruct dog :name)
(let [my-dog (struct-quack dog :name "burrito")]
  (try
   (my-dog :breed)
   (fail-test)
   (catch UnsupportedOperationException e
     ;success
     )))
 


;throws exception when trying to instantiate with inexistent slot
(defstruct cat :color)
(try
 (struct-quack cat :wheels 2)
 (fail-test)
   (catch UnsupportedOperationException e
     ;success
     ))

;has a proper toString()
(defstruct spider :poison?)
(let [s (struct-quack spider :poison? true)]
  (assert-equals "struct-quack [:struct-type spider {:poison? true}]" (str s)))