(use 'fato)
(use 'struct-quack)

(sheet "a duck typed struct instance based on defstruct"

       (defstruct my-struct :attr1 :attr2)
       
       (fact "is based on a struct defined using 'defstruct" []
	     (struct-quack my-struct :attr1 1))
       
       (fact "has its defining struct as metadata" []
       	     (= 'my-struct (:struct-type  ^(struct-quack my-struct :attr1 1 :attr2 2))))
       
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

       (fact "a nil key is still accessible")

       (fact "is equivalent to a struct-map" )
;	     [slots [[] [:att1 1] [:attr2 2] [:attr1 1 :attr2 2]]]
;	     (map (fn [x] (apply x my-struct slots)) (list struct-map struct-quack)))
       
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

;(sheet "a struct created using defquack"
 ;             (defquack my-own-struct :a :b)
;
 ;      (fact "contains metadata" []
	;     (= {:struct-type 'struct-quack} ^my-own-struct)))
;
;(sheet "a duck typed struct instance based on defquack"
;       (fact "it rejects a missing-attribute that does not receive one parameter")
;
;       (fact "it throws exception by default"
;	     [inexistent-slot [:a :b :c :d :e :f]]
;	     (try
;	      ((struct-quack my-struct) inexistent-slot)
;	      false
;	      (catch UnsupportedOperationException e
;		true))))
;
;(sheet "a duck typed struct instance based on a defquack with attribute-missing"
;       (println (macroexpand-1 '(defquack quacker (fn[s k] (list :struct s :key k)) :this-exists)))
;       (defquack quacker #(list :before % :after) :this-exists)
;
;       (fact "it returns the defined attribute-missing function if supplied"
;	     [attribute [:a :b :c] ]
;	     (= (list :before 'quacker :key attribute) 
;		((struct-quack quacker :this-exists 1) :inexistent-field))));;;;;;;;;