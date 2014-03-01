;;   Copyright (c) Nicola Mometto, Rich Hickey & contributors.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

(ns clojure.tools.analyzer.passes.jvm.annotate-internal-name
  (:require [clojure.tools.analyzer.ast :refer [update-children]]))

(defmulti annotate-internal-name :op)

(defn propagate-internal-name
  [ast internal-name]
  (update-children ast (fn [ast] (assoc-in ast [:env :internal-name] internal-name))))

(defmethod annotate-internal-name :default
  [{:keys [env] :as ast}]
  (if-let [internal-name (:internal-name env)]
    (propagate-internal-name ast internal-name)
    ast))

(defmethod annotate-internal-name :def
  [{:keys [name] :as ast}]
  (propagate-internal-name ast (str name)))

(defmethod annotate-internal-name :fn
  [{:keys [env local] :as ast}]
  (let [internal-name (str (when-let [n (:internal-name env)]
                             (str n "$"))
                           (or (:name local) "fn")
                           (gensym "__"))]
    (propagate-internal-name ast internal-name)))

(defmethod annotate-internal-name :binding
  [{:keys [name env] :as ast}]
  (let [internal-name (str (when-let [n (:internal-name env)]
                             (str n "$"))
                           name)]
   (propagate-internal-name ast internal-name)))
