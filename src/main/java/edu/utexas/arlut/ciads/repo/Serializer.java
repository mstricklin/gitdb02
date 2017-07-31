// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

public interface Serializer {
    byte[] serialize(Object o);

    <T> T deserialize(byte[] b, Class<T> clazz);

    <T> T deserialize(String s, Class<T> clazz);
}
