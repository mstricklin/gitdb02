// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Function;
import com.google.common.primitives.Ints;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public abstract class Keyed {
    protected Keyed(int key) {
        this.key = key;
        // is this interning a premature optimization?
        path = Path.of(key, getType());

    }

    @JsonIgnore
    public final int key;
    @JsonIgnore
    public final Path path;

    public int key() {
        return key;
    }
    public abstract Keyed immutable();
    public abstract Keyed mutable();
    @JsonIgnore
    public abstract String getType();

    public static Function<Keyed, Keyed> MAKE_IMMUTABLE = new Function<Keyed, Keyed>() {
        @Override
        public Keyed apply(Keyed k) {
            return k.immutable();
        }
    };

    @EqualsAndHashCode
    @ToString
    public static class Path {
        public static Path of(int key, String type) {
            return new Path(key, type);
        }
        Path(int key, String type) {
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

    }
}
