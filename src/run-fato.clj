(ns fato-run (:import (java.io File)))

(defn- url-from [file]  (. file  toURL))
(defn- file [path] (File. path))
(defn- path-from [file] (. file getAbsolutePath))

(defn- project-classpath []
  (let [deps-classpath (map url-from (.  (File. "deps") listFiles))
        src-classpath (list (url-from (file "src")))
	test-classpath (list (url-from (file "test")))]
    (concat deps-classpath src-classpath test-classpath)))

(defn- target-type [target-path] (if (. (File. target-path) isDirectory) :dir :file))

(defn- is-clojure-source? [file]
  (and (. file isFile)
       (re-find #"_facts\.clj$"  (. file getName))))

(defn- all-source-files-from [dir]
  (filter is-clojure-source? (file-seq dir)))

(defmulti load-facts-from target-type)

(defmethod load-facts-from :file [file] (load-file file) :file)

(defmethod load-facts-from :dir [dir]
  (let [src-files (all-source-files-from (File. dir))]
  (doseq [f src-files]
    (load-file (path-from f)))) :dir)

(doseq [artifact (project-classpath)]
  (add-classpath artifact))

;script
(use '(fato verify))
(use '[clojure.contrib.except :only (throw-if)]) 

;;common print
(def ansi-red     "\033[31m")
(def ansi-green   "\033[32m")
(def ansi-brown   "\033[33m")
(def ansi-default "\033[0m")
(def ansi-purple  "\033[35m")
(def ansi-light-gray "\033[37m")

(defmacro print-string[& t] `(print (str ~@t)))
(defmacro println-string[& t] `(println (str ~@t)))

(defn- print-problem[description ansi-color cause problematic-facts]
  (println-string ansi-color "\n======================" description "======================")
  (doseq [problematic problematic-facts]
    (println-string "\n=> " description " in [" (:name (:sheet (:fact problematic))) ", "(:name (:fact problematic)) "]:")
      (doseq [problematic-variant (filter #(= (:result %) cause) (:variant-results problematic))]
	(println-string "(fn " (:args problematic-variant) " "  (:code (:fact problematic)) ")")
	(if (:exception problematic-variant) (. (:exception problematic-variant) printStackTrace))))
    (println ansi-default))

(defn- batch-print-fact-result[result]
   (cond 
    (= :success (:status result)) (print-string ansi-green ".")
    (= :failure (:status result)) (print-string ansi-red "F")
    (= :pending (:status result)) (print-string ansi-brown "P")
    :else (print-string ansi-purple "E"))
   (print-string ansi-default)
   (flush))

(throw-if (or (nil? *command-line-args*)
	      (= 0 (count *command-line-args*)))
	  IllegalArgumentException "No path to facts was supplied")
	 
(defn- current-time[] (System/currentTimeMillis))	 
(defn- start-timer[]
    (let [start-time (current-time)]
    #(/ (- (current-time) start-time) 1000.0)))

(defn- run-batch[]
  (println-string ansi-light-gray "Verifying facts:" ansi-default)
  (let [
	stop-timer (start-timer)
	run-result (run-fato batch-print-fact-result)
	all-facts (all-fact-results-from run-result)
	total-variants (all-variants-from run-result)
	
	succeeded-facts (successes-from run-result)
	failed-facts	(failures-from run-result)
	exception-facts (exceptions-from run-result)
	pending-facts   (pending-from run-result)
	]
    
    (println-string "\nFacts: " (count all-facts) " (Variants:" (count total-variants) ")")
    (println-string ansi-green "Success: " (count succeeded-facts)
		  ansi-red " Failures: "  (count failed-facts) 
		  ansi-purple  " Exceptions: "  (count exception-facts)
		  ansi-brown " Pending: " (count pending-facts)
		  ansi-default)
    (println-string  "Finished in " (stop-timer) " seconds")

    (if (seq failed-facts) (print-problem "Failure" ansi-red :failure failed-facts))
    (if (seq exception-facts) (print-problem "Exception" ansi-purple :exception exception-facts))
    (+ (count failed-facts) (count exception-facts))))

(load-facts-from (first *command-line-args*))
(System/exit (run-batch))