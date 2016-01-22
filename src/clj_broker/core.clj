(ns clj-broker.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [zeromq.zmq :as zmq]
            [zeromq.device :as device]
            [zeromq.sendable :as s]
            [clojure.stacktrace]
            )
  (:import (java.net InetAddress))
  (:gen-class)
  )

(def ^:private init-broker-stats {:running nil})

(def ^:private broker-stats (atom init-broker-stats))

(defonce context (zmq/zcontext))

(def cli-options
  [["-fh" "--frontend-host FRONTEND-HOST" "frontend host"
    :default "*"]

   ["-f" "--frontend-port FRONTEND-PORT" "frontend port"
    :default 5003
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]

   ["-bh" "--backend-host BACKEND-HOST" "backend host"
    :default "*"]

   ["-b" "--backend-port BACKEND-PORT" "backend port"
    :default 5001
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-h" "--help"]
   ])

(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn usage [options-summary]
  (->> ["Usage: -m broker.broker [options] "
        ""
        "Options:"
        options-summary
        ""
        " broker frontend port backend port"
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn- start-broker
  [{:keys [frontend-host frontend-port backend-host backend-port] :as options}]
  (println "options" options)
  (when-not (:running @broker-stats)
    (swap! broker-stats assoc :running true)
    (try
      (let [frontend-str (str "tcp://" frontend-host ":" frontend-port)
            backend-str (str "tcp://" backend-host ":" backend-port)]
        (println "start broker frontend-str" frontend-str "backend-str" backend-str)
        (future
          (with-open [frontend (doto (zmq/socket context :xsub)
                                 (zmq/bind frontend-str))
                      backend (doto (zmq/socket context :xpub)
                                (zmq/bind backend-str))]
            (println "start proxy")
            (device/proxy context frontend backend)
            (println "end proxy")
            )
          )
        )

      (catch Exception e
        (.printStackTrace e)
        ))
    ))

(defn -main
  "main class with the broker"
  [& args-command]
  (let [{:keys [options arguments errors summary]} (parse-opts args-command cli-options :in-order true)]
    (cond (:help options)
          (exit 0 (usage summary))
          :else
          (start-broker options))
    )
  )
