package edu.utexas.arlut.ciads.revdb;

import java.io.IOException;

public interface DataStore {
    void rename(String newName);

    void shutdown();

    void dump();

    String getName();

    // =================================
    Transaction beginTX();

    // commit top-level transaction
    void commit() throws IOException;

    // rollback top-level transaction
    void rollback();

    // =================================
    <T> T persist(RevDBItem<?> k);

    void remove(RevDBProxyItem p);

    void remove(Integer key);

    RevDBItem<?> getImpl(Integer key, Class<? extends RevDBProxyItem> clazz);

    RevDBItem<?> getImplForMutation(Integer key, Class<? extends RevDBProxyItem> clazz);

    <T> T get(Integer key, Class<? extends RevDBProxyItem> clazz);

    <T> T getForMutation(Integer key, Class<? extends RevDBProxyItem> clazz);

    Iterable<? extends RevDBItem> list();

    Iterable<? extends RevDBItem> list(Class<? extends RevDBItem> clazz);

    // =================================
    interface Transaction extends AutoCloseable {
        @Override
        void close();
        void commit() throws IOException;
        void rollback();
    }
}
