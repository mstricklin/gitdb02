// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

public interface Proxy {
    IKeyed impl();
    IKeyed mutable();
    Class<?> proxiedClass(); // TODO: rm?
    String getType();
    String getKey();
    String getPath();
}
