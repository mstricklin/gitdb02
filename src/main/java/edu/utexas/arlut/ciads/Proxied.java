// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.utexas.arlut.ciads.repo.IKeyed;
import edu.utexas.arlut.ciads.repo.Proxy;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public abstract class Proxied<T> implements IKeyed<T> {
    // for serializer reconstruction
    protected Proxied() {
        id = "";
    }
    protected Proxied(String id) {
        this.id = id;
    }
    @Override
    public String getId() {
        return id;
    }
    @Override
    abstract public String getType();

    @Override
    abstract public T proxyOf(String key);

    // named __id for historical reasons
    @JsonProperty("__id")
    public final String id;

    @EqualsAndHashCode
    static class Path  {
        public static Path of(String key, String type) {
            return new Path(key, type);
        }
        Path(String key, String type) {
//            b0 = (byte) ((getKey & 0xFF000000) >> 24);
//            b1 = (byte) ((getKey & 0x00FF0000) >> 16);
//            b2 = (byte) ((getKey & 0x0000FF00) >> 8);
//            b3 = type + '.' + Integer.toHexString(getKey);
//            path = String.format("%02x/%02x/%02x/%s", b0, b1, b2, b3);
            path = type + '/' + key;
        }
        public final String path;
//        public final byte b0;
//        public final byte b1;
//        public final byte b2;
//        public final String b3;
        public String getPath() {
            return path;
        }
        @Override
        public String toString() { return path; }
    }
}
