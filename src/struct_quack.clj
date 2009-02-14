(ns struct-quack)
(import '(java.util Map) 
	'(clojure.lang PersistentStructMap$Def))

(defmulti defines-attributes? (fn[s & _] (class s)))

(defmethod defines-attributes? Map [a-struct & keys]
  (every? #(. a-struct containsKey %) keys))

(defn- attr-missing-for [a-struct & keys]
  (throw (UnsupportedOperationException. (str "Requested keys " keys  " are not part of the struct " a-struct))))

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

(defn- duck-typed-struct-wrapper 
  ([a-struct] 
     (duck-typed-struct-wrapper a-struct nil)) 
  ([a-struct metadata]
     (proxy [clojure.lang.IObj clojure.lang.IFn Map] []
       (get [key] 
	    (duck-typed-get a-struct key))
       (invoke [key] 
	       (duck-typed-get a-struct key))
       (withMeta [new-metadata] 
		 (duck-typed-struct-wrapper a-struct new-metadata))
       (meta [] metadata)
       (equals[other-struct] true))))

(defn- plain-struct-map [struct-name attrs]
  (merge (apply struct-map struct-name attrs)))

(defn struct-quack-impl [struct-name struct-definition & attrs]
  (let [a-struct (struct-map struct-definition)
	keys (keys-in attrs)]
    (if (apply defines-attributes? a-struct keys)
      (with-meta (duck-typed-struct-wrapper 
		  (plain-struct-map struct-definition attrs))
		 {:struct-type struct-name})
      (attr-missing-for a-struct keys))))

(defmacro struct-quack [struct-name & attrs]
  `(struct-quack-impl '~struct-name ~struct-name ~@attrs))
