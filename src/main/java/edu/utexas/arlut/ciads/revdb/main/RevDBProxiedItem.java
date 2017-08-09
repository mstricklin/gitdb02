// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.revdb.main;

import edu.utexas.arlut.ciads.revdb.RevDBItem;

public abstract class RevDBProxiedItem<T> implements RevDBItem<T> {
    // for serializer reconstruction
    protected RevDBProxiedItem() {
        id = -1;
    }
    protected RevDBProxiedItem(Integer id) {
        this.id = id;
    }
    public Integer getId() {
        return id;
    }
    @Override
    abstract public String getType();

    @Override
    abstract public T proxyOf(Integer id);

    public final Integer id;
}
