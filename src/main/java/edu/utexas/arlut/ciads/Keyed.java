// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import com.google.common.base.Joiner;

public abstract class Keyed {

    abstract Long id();
    abstract Keyed immutable();
    abstract Keyed mutable();
    abstract String getType();
    String path() {
        return getType() + "/" + id(); // joiner.join(getType(), id());
    }

    private static Joiner joiner = Joiner.on('/');
}
