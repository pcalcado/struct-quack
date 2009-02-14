(use 'fato)
(use 'struct-quack)

(sheet "a duck typed struct instance based on defstruct"

       (defstruct my-struct :attr1 :attr2)
       
       (fact "is based on a struct defined using 'defstruct" []
	     (struct-quack my-struct :attr1 1))
       
       (fact "has its defining struct as metadata" []
	     (let [a-struct  (struct-quack my-struct :attr1 1 :attr2 2)]
	       (= 
		'my-struct 
		(:struct-type  ^a-struct) 
		(struct-type-of a-struct))))
       
       (fact "doesn't need to have all slots populated" [slot [:attr1 :attr2]]
	     (struct-quack my-struct slot 'a-value))
       
       (fact "doesn't have to have any slots populated" []
	     (struct-quack my-struct))

       (fact "is equivalent to a non-quack struct with same structure" []
	     (= (struct-quack my-struct :attr1 1 :attr2 2) 
		{:attr1 1 :attr2 2}))

       (fact "is not equivalent to a non-quack struct with different structure" []
	     (false? 
	      (= (struct-quack my-struct :attr1 1 :attr2 2) 
		 {:attr1 1})))

       (fact "is equal other with same structure" []
	     (= (struct-quack my-struct :attr1 1 :attr2 2)
		(struct-quack my-struct :attr1 1 :attr2 2)))

       (fact "is not equal other with different structure" []
	     (false? 
	      (= 
	       (struct-quack my-struct :attr1 1 :attr2 2)
	       (struct-quack my-struct :attr2 2))))

       (fact "a nil key is still accessible" []
	     (nil? (:attr2 (struct-quack my-struct :attr1 1))))
       
       (fact "throw exception when trying to set inexistent slot"
	     [inexistent-slot [:attr-that-does-not-exist]]
	     (try
	      (struct-quack my-struct inexistent-slot 1)
	      false 
	      (catch UnsupportedOperationException e
		true)))
       
       (fact "does not throw exception when accessing existing and populated slot" [slot [:attr1 :attr2] value [1 2]]
	     (= value ((struct-map my-struct :attr1 1 :attr2 2) slot)))

       (fact "does not throw exception when accessing existing and nil slot" []
	     (struct-quack my-struct :attr1 1) 
	     (nil? ((struct-quack my-struct :attr1 1) :attr2)))

       (fact "gets as (:a struct) and as (struct :a)" [attr [:attr1 :attr2] value [1 2]]
	     (let [instance (struct-quack my-struct attr value)]
	       (= (instance attr) (attr instance))))

       (fact "it throws exception by default"
	     [inexistent-slot [:a :b :c :d :e :f]]
	     (try
	      ((struct-quack my-struct) inexistent-slot)
	      false
	      (catch UnsupportedOperationException e
		true))))

(sheet "a duck typed struct instance based on a defquack with attribute-missing"
       (def attr-missing (fn [s a] (list :before (struct-type-of s) :after a)))
       (defquack quacker attr-missing :this-exists)

       (fact "has as its struct-type the defquack'd struct" []
	     (= 'quacker (struct-type-of (struct-quack quacker))))

       (fact "returns the default attribute-missing when creating"
	     (try
	      (struct-quack quacker :inexistent-slot 1)
	      false
	      (catch UnsupportedOperationException e
		true)))

       (fact "returns the defined attribute-missing for a get" []
	     (= (list :before 'quacker :after :inexistent-field) 
		((struct-quack quacker :this-exists 1) :inexistent-field))))