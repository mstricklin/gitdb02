// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IKeyed<T> {
    String getId();

    @JsonIgnore
    String getType();

    IKeyed<T> copy();

    T proxyOf(Integer key);


//    @EqualsAndHashCode
//    @ToString
//    class TryPath {
//        public static TryPath of(int getKey, String type) {
//            return new TryPath(getKey, type);
//        }
//        TryPath(int getKey, String type) {
//            b0 = (byte) ((getKey & 0xFF000000) >> 24);
//            b1 = (byte) ((getKey & 0x00FF0000) >> 16);
//            b2 = (byte) ((getKey & 0x0000FF00) >> 8);
//            b3 = type + '.' + Integer.toHexString(getKey);
////            path = String.format("%02x/%02x/%02x/%s", b0, b1, b2, b3);
//            path = type + '.' + Integer.toHexString(getKey);
//        }
//        public final String path;
//        public final byte b0;
//        public final byte b1;
//        public final byte b2;
//        public final String b3;
//
//    }
}
