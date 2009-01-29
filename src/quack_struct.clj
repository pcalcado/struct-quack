(ns quack-struct)
(import '(java.util Map))

(defmulti attributes-in #(isa? (class %) Map) )

(defmethod attributes-in true [struct]
  (. struct keySet))

(defmethod attributes-in false [struct-def]
  (attributes-in (struct-map struct-def)))

(defn- keys-at [arguments]
  (map first (partition 2 arguments)))

(defn- coll-contains [element coll]
  (> 0 (count (filter #(= % element) coll))))

(defn- valid-arguments-for? [struct arguments]
  (= 0 (count (remove 
	       #(. (attributes-in struct) contains %)  
	       (keys-at arguments)))))

(defn- access-error! [struct & desired-keys]
  (throw 
   (UnsupportedOperationException. (str "Attributes " desired-keys " don't exit among " (attributes-in struct)))))

(defn- verified-call-to [attribute struct-instance]
  (coll-contains attribute (. struct-instance keySet)))

(defn- attribute-if-exists [struct key]
  (println (coll-contains key (attributes-in struct)))
  (when-not (coll-contains key (attributes-in struct)) (access-error! struct key))
  (struct key))

(defn- duck-typed-struct-wrapper [struct]
  (proxy [Map clojure.lang.IFn] [] 
    (get [key] (attribute-if-exists struct key))
    (invoke [key] (attribute-if-exists struct key))))

(defn- create-quack [struct args]
  (duck-typed-struct-wrapper (apply struct-map struct args)))

(defn struct-quack [struct & populating]
  (cond
   (valid-arguments-for? struct populating) (create-quack struct populating)
   :else (access-error! struct (keys-at populating))))