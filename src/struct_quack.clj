(ns struct-quack)
(import '(java.util Map) 
	'(clojure.lang PersistentStructMap$Def))

(def *attribute-missing-registry* (ref {}))

(defn struct-type-of 
  "Returns the Symbol that identifies this structure's type. This is stored as metadata under :struct-type"
  [a-struct]
  (or (:struct-type ^a-struct)
      (. a-struct keySet)))

(def default-attr-missing 
     (fn[a-struct & keys] 
       (throw (UnsupportedOperationException. 
	       (str "Requested keys " keys  
		    " are not defined in struct-type " (struct-type-of a-struct))))))

(defmulti defines-attributes? (fn[s & _] (class s)))

(defmethod defines-attributes? Map [a-struct & keys]
  (every? #(. a-struct containsKey %) keys))

(defn- find-attr-missing-for [a-struct]
  (or
   (@*attribute-missing-registry* (struct-type-of a-struct))
   default-attr-missing))

(defn- attr-missing-for [a-struct & keys]
  (apply
   (find-attr-missing-for a-struct)
   (cons a-struct keys)))

(defn- keys-in [attrs]
  (map first (partition 2 attrs)))

(defn- duck-type-validated [a-struct attrs]
  (doseq [key (keys-in attrs)]
    (when-not (defines-attributes? a-struct key)
      (attr-missing-for a-struct key))))

(defn- duck-typed-get [a-struct key]
  (if (defines-attributes? a-struct key)
    (a-struct key)
    (attr-missing-for a-struct key)))

(defn- duck-typed-struct-wrapper [a-struct metadata]
  (let [a-struct-with-meta (with-meta a-struct metadata)]
    (proxy [clojure.lang.IObj clojure.lang.IFn Map] []     
      (get [key] 
	   (duck-typed-get a-struct-with-meta key))
      (invoke [key] 
	      (duck-typed-get a-struct-with-meta key))
      (withMeta [new-metadata] 
		(duck-typed-struct-wrapper a-struct-with-meta new-metadata))
      (meta [] 
	    metadata)
      (equals[other-struct] 
	     (= other-struct a-struct-with-meta)))))

(defn- plain-struct-map [struct-name attrs]
  (merge (apply struct-map struct-name attrs)))

(defn register-attr-missing-for [struct-name attr-missing-function]
  (dosync
   (ref-set *attribute-missing-registry* 
	    (merge @*attribute-missing-registry* 
		   {struct-name attr-missing-function}))))

(defn struct-quack-impl [struct-name struct-definition & attrs]
  (let [a-struct (struct-map struct-definition)
	keys (keys-in attrs)]
    (if (apply defines-attributes? a-struct keys)
      (duck-typed-struct-wrapper 
		  (plain-struct-map struct-definition attrs)
		  {:struct-type struct-name})
      (attr-missing-for a-struct keys))))

(defmacro struct-quack
  "Instantiates a duck-typed struct."
  [struct-name & attrs]
  `(struct-quack-impl '~struct-name ~struct-name ~@attrs))

(defmacro defquack
  "The same as defstruct except that you can supply a attribute-missing function to use with struct-quack"
  [struct-name attr-missing & attr-names]
  `(do 
     (register-attr-missing-for '~struct-name ~attr-missing)
     (defstruct ~struct-name ~@attr-names)))