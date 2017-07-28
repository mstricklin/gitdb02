// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Queues.newArrayDeque;
import static com.google.common.collect.Sets.newHashSet;

import java.io.Closeable;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;

@Slf4j
public class DataStore extends Mergeable<Long, Keyed> {

    DataStore() {
        log.info("Datastore startup");
    }

    public void shutdown() {
        log.info("Shutting down datastore");
    }
    public void dump() {
        log.info("baseline");
        for (Map.Entry<ObjectId, Keyed> e : cache.asMap().entrySet())
            log.info("\t{} => {}", e.getKey().name(), e.getValue());
    }
    public static class NotInTransactionException extends RuntimeException {
    }

    public static class NotInnermostTransactionException extends RuntimeException {
    }
    // start transaction, with new transaction
    public Transaction beginTX() {
        Transaction tx = new Transaction(this, threadTransaction.get().peek());
        threadTransaction.get().push(tx);
        return tx;
    }

    // get top-most transaction
    public Transaction currentTX() {
        if (!threadTransaction.get().isEmpty()) {
            return threadTransaction.get().peek();
        }
        throw new NotInTransactionException();
    }

    // commit top-level transaction
    public void commit() {
        currentTX().commit();
    }

    // rollback top-level transaction
    public void rollback() {
        currentTX().rollback();
    }

    // merge only (or last remaining) transaction from stack into me
    public void merge(Map<Long, Keyed> added, Set<Long> deleted) {
        log.info("merge into baseline");

        cache.invalidateAll(deleted);
        for (Keyed k: added.values()) {
            Keyed ik = k.immutable();

            Serializer s = Serializer.of();
            String ser = s.serialize(ik);
            String path = ik.path();
            ObjectId oid = s.getSHA(ser);
            log.info("ObjectId {}", oid.name());
            index.put(path, oid);
            cache.put(oid, ik);
        }

    }
    private void popTX() {
        threadTransaction.get().pop();
    }
    // =================================
    public void add(Long key, Keyed val) {
        if (threadTransaction.get().isEmpty())
            throw new NotInTransactionException();
        threadTransaction.get().peek().add(key, val);
    }
    public void remove(Long key) {
        if (threadTransaction.get().isEmpty())
            throw new NotInTransactionException();
        threadTransaction.get().peek().remove(key);
    }
    private <T> T _get(Long key) {
        ObjectId oid = index.get(App.K.getPath(key));
        log.info("_get {} {}", App.K.getPath(key), oid);
        return (T)cache.getIfPresent(oid);
    }
    @Override
    protected <T extends Keyed> T get(Long key) {
        if (!threadTransaction.get().isEmpty())
            return currentTX().get(key);
        return _get(key);
    }
    private <T> T _getMutable(Long key) {
        return (T)cache.getIfPresent(key);
    }
    @Override
    protected <T extends Keyed> T getMutable(Long key) {
        log.info("baseline lookup {}", key);
        return currentTX().getMutable(key);
    }
    // =================================

    @Override
    public String toString() {
        return "Baseline";
    }

    public static class Transaction extends Mergeable<Long, Keyed> implements Closeable {
        private Transaction(final DataStore ds, final Mergeable p) {
            this.ds = ds;
            this.parent = p;
        }
        public void add(Long key, Keyed val) {
            added.put(key, val);
        }
        public void remove(Long key) {
            added.remove(key);
            deleted.add(key);
        }
        @Override
        protected <T extends Keyed> T get(Long key) {
            if (deleted.contains(key))
                return null;
            if (added.containsKey(key))
                return (T)added.get(key);
            if (null == parent)
                return ds._get(key);
            return (T)parent.get(key);
        }
        @Override
        protected <T extends Keyed> T getMutable(Long key) {
            log.info("tx {} getMutable", this);
            Keyed k = get(key);
            log.info("tx {} getMutable got {}", this, k);
            if (null == k)
                return null;
            Keyed k2 = k.mutable();
            add(key, k2);
            return (T)k2;
        }
        public void commit() {
            if (closed) return;
            if (ds.currentTX() != this) // yes, !=
                throw new NotInnermostTransactionException();
            if (null == parent)
                ds.merge(added, deleted);
            else
                parent.merge(added, deleted);
            added.clear();
            deleted.clear();
            closed = true;
            ds.popTX();
        }
        public void rollback() {
            if (closed) return;
            if (ds.currentTX() != this) // yes, !=
                throw new NotInnermostTransactionException();
            added.clear();
            deleted.clear();
            closed = true;
            ds.popTX();
        }
        public void merge(Map<Long, Keyed> added, Set<Long> deleted) {
            log.info("merge into {}", this);
            for (Long k : deleted)
                this.added.remove(k);
            this.added.putAll(added);
            this.deleted.addAll(deleted);
        }

        public void dump() {
            log.info("{} [{}]", this, parent);
            for (Map.Entry<Long, Keyed> e : added.entrySet())
                log.info("\t{} => {}", e.getKey(), e.getValue());
            log.info("\tX{}", deleted);
        }
        @Override
        public void close() {
            if (closed) return;
            rollback();
        }
        @Override
        public String toString() {
            return "Transaction " + id;
        }
        private static final AtomicInteger cnt = new AtomicInteger(0);
        private final int id = cnt.getAndIncrement();
        private boolean closed = false;
        private final DataStore ds;
        private Mergeable parent;
        private final Map<Long, Keyed> added = newHashMap();
        private final Set<Long> deleted = newHashSet();
    }

    // =================================

    private static final AtomicInteger objectCounter = new AtomicInteger(0);

    private final Map<String, ObjectId> index = newHashMap();
    // TODO: parameterize maximumSize
    private final Cache<ObjectId, Keyed> cache = CacheBuilder.newBuilder()
                                                             .maximumSize(2000)
                                                             .build();
    private ThreadLocal<Deque<Transaction>> threadTransaction = new ThreadLocal<Deque<Transaction>>() {
        @Override
        protected Deque<Transaction> initialValue() {
            return newArrayDeque();
        }
    };


}
