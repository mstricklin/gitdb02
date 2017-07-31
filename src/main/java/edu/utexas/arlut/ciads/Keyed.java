// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.utexas.arlut.ciads.repo.IKeyed;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public abstract class Keyed implements IKeyed<Integer> {
    protected Keyed(int key) {
        this.key = key;
        path = ConcretePath.of(key, getType());
    }
    @Override
    public Integer key() {
        return key;
    }
    @Override
    abstract public String getType();

    // TODO: pull this out to GitRepo tools
    @Override
    public Path getPath() {
        return path;
    }

    @JsonIgnore
    public final int key;
    @JsonIgnore
    public final IKeyed.Path path;

    @EqualsAndHashCode
    @ToString
    static class ConcretePath implements Path {
        public static IKeyed.Path of(int key, String type) {
            return new ConcretePath(key, type);
        }
        ConcretePath(int key, String type) {
            b0 = (byte) ((key & 0xFF000000) >> 24);
            b1 = (byte) ((key & 0x00FF0000) >> 16);
            b2 = (byte) ((key & 0x0000FF00) >> 8);
            b3 = type + '.' + Integer.toHexString(key);
//            path = String.format("%02x/%02x/%02x/%s", b0, b1, b2, b3);
            path = type + '.' + Integer.toHexString(key);
        }
        public final String path;
        public final byte b0;
        public final byte b1;
        public final byte b2;
        public final String b3;
        public String getPath() {
            return path;
        }
    }
}
