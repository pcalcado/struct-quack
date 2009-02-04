(ns quack-struct)
(import '(java.util Map))

(defmulti attributes-in #(isa? (class %) Map) )

(defmethod attributes-in true [struct]
  (. struct keySet))

(defmethod attributes-in false [struct-def]
  (attributes-in (struct-map struct-def)))

(defn- keys-at [arguments]
  (map first (partition 2 arguments)))

(defn- valid-arguments-for? [struct arguments]
  (= 0 (count (remove 
	       #(. (attributes-in struct) contains %)  
	       (keys-at arguments)))))

(defn- access-error! [struct & desired-keys]
  (throw 
   (UnsupportedOperationException. (str "Attributes " desired-keys " don't exit among " (attributes-in struct)))))

(defn- attribute-if-exists [struct key]
  (when-not (. struct containsKey key) (access-error! struct key))
  (struct key))

(defn- duck-typed-struct-wrapper 
  ([struct] 
     (duck-typed-struct-wrapper struct nil)) 
  ([struct metadata]
     (proxy [clojure.lang.IObj clojure.lang.IFn Map] []
       (get [key] 
	    (attribute-if-exists struct key))
       (invoke [key] 
	       (attribute-if-exists struct key))
       (withMeta [new-metadata] 
		 (duck-typed-struct-wrapper struct new-metadata))
       (meta [] metadata))))

(defn- create-quack [struct-name struct-definition  args]
  (with-meta (duck-typed-struct-wrapper (apply struct-map struct-definition args)) {:struct-type struct-name}  ))

(defn struct-quack-impl [struct-name struct-definition & populating]
  (cond
   (valid-arguments-for? struct-definition populating) (create-quack struct-name struct-definition populating)
   :else (access-error! struct-definition (keys-at populating))))

(defmacro struct-quack [struct & populating]
  `(struct-quack-impl '~struct ~struct ~@populating))

