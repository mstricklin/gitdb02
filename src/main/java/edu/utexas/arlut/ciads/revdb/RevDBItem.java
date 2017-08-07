// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.revdb;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface RevDBItem<T> {

    @JsonIgnore
    String getType();

    RevDBItem<T> copy();

    T proxyOf(Integer key);
}
