// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.revdb.main;

import edu.utexas.arlut.ciads.revdb.RevDBItem;

public abstract class RevDBProxiedItem<T> implements RevDBItem<T> {
    // for serializer reconstruction
    protected RevDBProxiedItem() {
        id = "";
    }
    protected RevDBProxiedItem(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    @Override
    abstract public String getType();

    @Override
    abstract public T proxyOf(Integer key);

    // named __id for historical reasons
//    @JsonProperty("__id")
    public final String id;
}
