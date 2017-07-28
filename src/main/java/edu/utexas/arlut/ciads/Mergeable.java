// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import java.util.Map;
import java.util.Set;

abstract class Mergeable<T1, T2> {
    protected abstract void merge(Map<T1, T2> added, Set<T1> deleted);
    protected abstract <T extends T2> T get(T1 key);
    protected abstract <T extends T2> T getMutable(T1 key);

}
