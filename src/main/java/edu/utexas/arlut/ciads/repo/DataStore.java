// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Maps.filterValues;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Queues.newArrayDeque;
import static com.google.common.collect.Sets.newHashSet;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;

import java.io.Closeable;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import edu.utexas.arlut.ciads.App;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;

@Slf4j
public class DataStore<T> extends Mergeable<T, Keyed<T>> {

    public static class NotInTransactionException extends RuntimeException {
    }

    public static class NotInnermostTransactionException extends RuntimeException {
    }
    // =================================

    public DataStore(ObjectId head) {
        // TODO: get the right index...
        log.info("Datastore startup");
    }

    public void shutdown() {
        log.info("Shutting down datastore");
    }
    public void dump() {
        log.info("baseline");
        index.dump();
        for (Map.Entry<ObjectId, Keyed> e : cache.asMap().entrySet())
            log.info("\t{} => {}", e.getKey().name(), e.getValue());
    }

    // start transaction, with a new transaction
    public Transaction beginTX() {
        Transaction tx = new Transaction(this, threadTransaction.get().peek());
        threadTransaction.get().push(tx);
        return tx;
    }

    // get top-most transaction
    public Transaction<T> currentTX() {
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

    private Function<Keyed<T>, Keyed<T>> MAKE_IMMUTABLE = new Function<Keyed<T>, Keyed<T>>() {
        @Override
        public Keyed<T> apply(Keyed<T> k) {
            return k.immutable();
        }
    };
    // merge only (or last remaining) transaction from stack into baseline
    @Override
    protected void merge(Map<T, Keyed<T>> added, Set<T> deleted) {
//    public void merge(Map<T, Keyed> added, Set<T> deleted) {
        log.info("merge into baseline");

        Index i = Index.of(index);
        Map<T, Keyed<T>> f = Maps.filterKeys(added, not(in(deleted)));

        i.remove(deleted);
        i.addAll(Maps.transformValues(added, Keyed.MAKE_IMMUTABLE));
        index = i;



//        cache.invalidateAll(deleted);
//        for (Keyed k : added.values()) {
//            Keyed ik = k.immutable();
//
//            Serializer s = Serializer.of();
//            String ser = s.serialize(ik);
//            String path = ik.path();
//            ObjectId oid = s.getSHA(ser);
//            log.info("ObjectId {}", oid.name());
//            index.put(path, oid);
//            cache.put(oid, ik);
//        }

    }
    void popTX() {
        threadTransaction.get().pop();
    }
    // =================================
//    public void add(T key, Keyed<T> val) {
//        if (threadTransaction.get().isEmpty())
//            throw new NotInTransactionException();
//        threadTransaction.get().peek().add(key, val);
//    }
//    public void remove(T key) {
//        if (threadTransaction.get().isEmpty())
//            throw new NotInTransactionException();
//        threadTransaction.get().peek().remove(key);
//    }
    protected <T1 extends Keyed<T>> T1 _get(T key) {
//        dumpMap("{} => {}", index);
        index.dump();
//        String path = "K/" + key;
//        ObjectId oid = index.get(path);
//        log.info("_get {} {}", path, oid);
        // TODO: handle null oid here...
//        return (T1)cache.getIfPresent(oid);
        return (T1)index.get(key);
    }

    @Override
    public <T1 extends Keyed<T>> T1 get(T key) {
        if (!threadTransaction.get().isEmpty())
            return (T1)currentTX().get(key);
        return _get(key);
    }
    @Override
    public <T1 extends Keyed<T>> T1 getMutable(T key) {
        log.info("baseline lookup {}", key);
        return (T1)currentTX().getMutable(key);
    }

    private Iterable<Keyed<T>> _list() {
        return index.list();
    }
    @Override
    public Iterable<Keyed<T>> list() {
        if (!threadTransaction.get().isEmpty())
            return currentTX().list();
        return _list();
    }
    // =================================

    @Override
    public String toString() {
        return "Baseline";
    }

    // =================================

    private static final AtomicInteger objectCounter = new AtomicInteger(0);

    //    private final Map<String, ObjectId> index = newHashMap();
    Index index = Index.of();
    // TODO: parameterize maximumSize
    private final Cache<ObjectId, Keyed> cache = CacheBuilder.newBuilder()
                                                             .maximumSize(2000)
                                                             .build();
    private ThreadLocal<Deque<Transaction<T>>> threadTransaction = new ThreadLocal<Deque<Transaction<T>>>() {
        @Override
        protected Deque<Transaction<T>> initialValue() {
            return newArrayDeque();
        }
    };


}
