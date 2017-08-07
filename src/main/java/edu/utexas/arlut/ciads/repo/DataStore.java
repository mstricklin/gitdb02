package edu.utexas.arlut.ciads.repo;

import java.io.IOException;

public interface DataStore {
    void rename(String newName);

    void shutdown();

    void dump();

    // =================================
    Transaction beginTX();

    // commit top-level transaction
    void commit() throws IOException;

    // rollback top-level transaction
    void rollback();

    // =================================
    <T> T persist(Keyed<?> k);

    void remove(Proxy p);

    void remove(Integer key);

    Keyed<?> getImpl(Integer key, Class<? extends Proxy> clazz);

    Keyed<?> getImplForMutation(Integer key, Class<? extends Proxy> clazz);

    <T> T get(Integer key, Class<? extends Proxy> clazz);

    <T> T getForMutation(Integer key, Class<? extends Proxy> clazz);

    Iterable<? extends Keyed> list();

    Iterable<? extends Keyed> list(Class<? extends Keyed> clazz);

    // =================================
    interface Transaction extends AutoCloseable {
        @Override
        void close();
        void commit() throws IOException;
        void rollback();
    }
}
