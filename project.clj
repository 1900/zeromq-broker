(defproject clj-zeromq "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"local" ~(str (.toURI (java.io.File. "repo")))}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [cheshire "5.4.0"]
                 [org.zeromq/cljzmq "0.1.4"]
                 ]

  :jvm-opts ["-Djava.library.path=./resources/"]
  :resource-paths []
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  :java-source-paths ["src/java/"]
  :main ^:skip-aot clj-broker.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})