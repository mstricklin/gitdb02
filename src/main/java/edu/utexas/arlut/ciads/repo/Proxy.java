// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

public interface Proxy {
    Keyed impl();
    Keyed mutable();
    Class<?> proxiedClass(); // TODO: rm?
    String getType();
    Integer getKey();
}
