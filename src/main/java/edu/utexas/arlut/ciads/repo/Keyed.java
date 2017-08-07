// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Keyed<T> {

    @JsonIgnore
    String getType();

    Keyed<T> copy();

    T proxyOf(Integer key);
}
