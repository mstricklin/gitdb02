// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IKeyed<T> {
    T key();
    @JsonIgnore
    String getType();
    @JsonIgnore
    Path getPath();
    IKeyed<T> copy();

    interface Path {
        String getPath();
    }
//    @EqualsAndHashCode
//    @ToString
//    class Path {
//        public static Path of(int key, String type) {
//            return new Path(key, type);
//        }
//        Path(int key, String type) {
//            b0 = (byte) ((key & 0xFF000000) >> 24);
//            b1 = (byte) ((key & 0x00FF0000) >> 16);
//            b2 = (byte) ((key & 0x0000FF00) >> 8);
//            b3 = type + '.' + Integer.toHexString(key);
////            path = String.format("%02x/%02x/%02x/%s", b0, b1, b2, b3);
//            path = type + '.' + Integer.toHexString(key);
//        }
//        public final String path;
//        public final byte b0;
//        public final byte b1;
//        public final byte b2;
//        public final String b3;
//
//    }
}
