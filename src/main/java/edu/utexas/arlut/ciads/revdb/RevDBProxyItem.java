// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.revdb;

public interface RevDBProxyItem {
    RevDBItem impl();
    RevDBItem mutable();
    Class<?> proxiedClass(); // TODO: rm?
    String getType();
    Integer getId();
}
