(use 'fato)
(use 'quack-struct)

(sheet "a duck typed struct"

       (defstruct my-struct :attr1 :attr2)
       
       (fact "is based on a struct defined using 'defstruct" []
	     (struct-quack my-struct :attr1 1))
       
       (fact "has its defining struct as metadata" []
	     (= 'my-struct (:struct-type  ^(struct-quack my-struct :attr1 1 :attr2 2))))
       
       (fact "doesn't have to have all slots populated" [slot [:attr1 :attr2]]
	     (struct-quack my-struct slot 'a-value))
       
       (fact "doesn't have to have any slots populated" []
	     (struct-quack my-struct))
       
       (fact "is equivalent to a struct-map" 
	     [slots [[] [:att1 1] [:attr2 2] [:attr1 1 :attr2 2]]]
	     (map (fn [x] (apply x my-struct slots)) (list struct-map struct-quack)))
       
       (fact "throw exception when trying to set inexistent slot"
	     [inexistent-slot [:attr-that-does-not-exist]]
	     (try
	      (struct-quack my-struct inexistent-slot 1)
	      false 
	      (catch UnsupportedOperationException e
		true)))
       
       (fact "throws exception when trying to get inexistent slot"
	     [inexistent-slot [:attr-that-does-not-exist]]
	     (try
	      ((struct-quack my-struct :attr1 1 :attr2 2) inexistent-slot)
	      false
	      (catch UnsupportedOperationException e
		true)))
       (fact "does not throw exception when accessing existing and populated slot" [slot [:attr1 :attr2] value [1 2]]
	     (= value ((struct-map my-struct :attr1 1 :attr2 2) slot)))

       (fact "does not throw exception when accessing existing and nil slot" []
	     (nil? ((struct-map my-struct :attr1 1) :attr2))))