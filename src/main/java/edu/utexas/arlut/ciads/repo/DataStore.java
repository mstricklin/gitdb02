// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

@Slf4j
public class DataStore extends Mergeable<Integer, Keyed> {

    // =================================

    public DataStore(RevCommit rev) throws GitAPIException {
        // TODO: get the right index...
        log.info("Datastore startup from {}", rev.abbreviate(10).name());
        revision = rev;
        index = Index.of(rev.getTree());
        repo = GitRepository.instance();
    }

    public void shutdown() {
        log.info("Shutting down datastore");
    }
    public void dump() {
        log.info("dump {}", this);
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

    private Function<Keyed, Keyed> MAKE_IMMUTABLE = new Function<Keyed, Keyed>() {
        @Override
        public Keyed apply(Keyed k) {
            return k.immutable();
        }
    };
    // merge only (or last remaining) transaction from stack into baseline
    @Override
    protected void merge(Map<Integer, Keyed> added, Set<Integer> deleted) {
        log.info("merge into baseline");

        // make each immutable
        // put each object in the repo
        // cache each object in the cache
        // delete from index
        // add to index
        try {
            for (Keyed k : added.values()) {
                ObjectId oid = repo.insert(k);
                log.info("Added {} {}", k, oid.abbreviate(10).name());
            }
            index.addAll(added);
        } catch (IOException e) {

        }

//        Index i = Index.of(index);
//        Map<Integer, Keyed> f = Maps.filterKeys(added, not(in(deleted)));
        // we should be able to trust that none of deleted are in added

//        try {
//            for (Keyed k : added.values()) {
//                ObjectId oid = repo.insert(k);
//                i.put(k.path, oid);
//            }
//        } catch (IOException e) {
//            log.error("Error persisting", e);
//        }

//        i.remove(deleted);
//        i.addAll(Maps.transformValues(added, Keyed.MAKE_IMMUTABLE));
//        index = i;



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
    protected <T extends Keyed> T _get(Integer key, Class<?> clazz) {
        index.dump();
        // TODO: handle null oid here...
        Keyed.Path p = Keyed.Path.of(key, clazz.getSimpleName());
        ObjectId oid = null;
        try {
            oid = index.get(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (T) cache.getIfPresent(oid);
    }

    @Override
        public <T extends Keyed> T get(Integer key, Class<?> clazz) {
        return tlTransaction.get().get(key, clazz);
    }
    @Override
    public <T extends Keyed> T getMutable(Integer key, Class<?> clazz) {
        log.info("baseline lookup {}", key);
        return (T)tlTransaction.get().getMutable(key, clazz);
    }

    Iterable<Keyed> _list() {
        return index.list();
    }
    @Override
    public Iterable<Keyed> list() {
        return tlTransaction.get().list();
    }
    // =================================

    @Override
    public String toString() {
        return "DataStore from "+ revision.abbreviate(10).name();
    }

    // =================================

    GitRepository repo;
    private static final AtomicInteger objectCounter = new AtomicInteger(0);

    //    private final Map<String, ObjectId> index = newHashMap();
    Index index;
    // TODO: parameterize maximumSize
    private final Cache<ObjectId, Keyed> cache = CacheBuilder.newBuilder()
                                                             .maximumSize(2000)
                                                             .build();
    private ThreadLocal<Transaction> tlTransaction = new ThreadLocal<Transaction>() {
        @Override
        protected Transaction initialValue() {
            return new Transaction(DataStore.this);
        }
    };
    final RevCommit revision;


}
