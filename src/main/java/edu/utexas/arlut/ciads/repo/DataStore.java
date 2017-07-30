// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;

@Slf4j
public class DataStore extends Mergeable<Integer, Keyed<Integer>> {

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
        Transaction tx = tlTransaction.get();
        if (null == tx) {
            tx = new Transaction(this);
            tlTransaction.set(tx);
        }
        return tx;
    }

    // get top-most transaction
    public Transaction currentTX() {
        return tlTransaction.get();
    }

    // commit top-level transaction
    public void commit() {
        currentTX().commit();
    }

    // rollback top-level transaction
    public void rollback() {
        currentTX().rollback();
    }

    private Function<Keyed<Integer>, Keyed<Integer>> MAKE_IMMUTABLE = new Function<Keyed<Integer>, Keyed<Integer>>() {
        @Override
        public Keyed<Integer> apply(Keyed<Integer> k) {
            return k.immutable();
        }
    };
    // merge only (or last remaining) transaction from stack into baseline
    @Override
    protected void merge(Map<Integer, Keyed<Integer>> added, Set<Integer> deleted) {
//    public void merge(Map<Integer, Keyed> added, Set<Integer> deleted) {
        log.info("merge into baseline");

        Index i = Index.of();
        Map<Integer, Keyed<Integer>> f = Maps.filterKeys(added, not(in(deleted)));

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
    // =================================
//    public void add(T key, Keyed<T> val) {
//        if (tlTransaction.get().isEmpty())
//            throw new NotInTransactionException();
//        tlTransaction.get().peek().add(key, val);
//    }
//    public void remove(T key) {
//        if (tlTransaction.get().isEmpty())
//            throw new NotInTransactionException();
//        tlTransaction.get().peek().remove(key);
//    }
    protected <T extends Keyed<Integer>> T _get(Integer key) {
//        dumpMap("{} => {}", index);
        index.dump();
//        String path = "K/" + key;
//        ObjectId oid = index.get(path);
//        log.info("_get {} {}", path, oid);
        // TODO: handle null oid here...
//        return (T1)cache.getIfPresent(oid);
        return (T)index.get(key);
    }

    @Override
        public <T extends Keyed<Integer>> T get(Integer key) {
        return tlTransaction.get().get(key);
    }
    @Override
    public <T extends Keyed<Integer>> T getMutable(Integer key) {
        log.info("baseline lookup {}", key);
        return (T)tlTransaction.get().getMutable(key);
    }

    Iterable<Keyed<Integer>> _list() {
        return index.list();
    }
    @Override
    public Iterable<Keyed<Integer>> list() {
//        if (!tlTransaction.get())
//            return currentTX().list();
        return tlTransaction.get().list();
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
    private ThreadLocal<Transaction> tlTransaction = new ThreadLocal<Transaction>() {
        @Override
        protected Transaction initialValue() {
            //return Transaction();
            return new Transaction(DataStore.this);
        }
    };


}
