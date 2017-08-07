// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.repo.Keyed;

public abstract class Proxied<T> implements Keyed<T> {
    // for serializer reconstruction
    protected Proxied() {
        id = "";
    }
    protected Proxied(String id) {
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
