// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;


import com.google.common.base.Function;

public abstract class Keyed<T> {

    public abstract T id();
    public abstract Keyed immutable();
    public abstract Keyed mutable();
    public abstract String getType();
    public String path() {
        return getType() + "/" + id(); // joiner.join(getType(), id());
    }

    public static Function<Keyed<?>, Keyed<?>> MAKE_IMMUTABLE = new Function<Keyed<?>, Keyed<?>>() {
        @Override
        public Keyed<?> apply(Keyed<?> k) {
            return k.immutable();
        }
    };
}
