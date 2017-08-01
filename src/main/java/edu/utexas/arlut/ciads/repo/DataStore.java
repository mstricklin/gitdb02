// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

@Slf4j
public class DataStore {

    private static LoadingCache<RevCommit, DataStore> stores
            = CacheBuilder.newBuilder()
                          .maximumSize(1000)
                          .build(
                                  new CacheLoader<RevCommit, DataStore>() {
                                      public DataStore load(RevCommit rev) {
                                          return new DataStore(rev);
                                      }
                                  });
    // =================================
    public static DataStore of(final RevCommit rev) {
        checkNotNull(rev);
        try {
            return stores.get(rev);
        } catch (ExecutionException e) {
            log.error("Can't instantiate a datastore for rev {}", rev);
            log.error("", e);
        }
        return null;
    }

    private DataStore(RevCommit rev) {
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
        log.info("\tpending");
        dumpMap("\t{} => {}", added);
        index.dump();
    }

    public static class Transaction implements AutoCloseable {
        private Transaction(DataStore ds) {
            this.ds = ds;
        }
        private final DataStore ds;
        @Override
        public void close() {
            ds.rollback();
        }
        public void commit() {
            ds.commit();
        }
        public void rollback() {
            ds.rollback();
        }
    }
    public Transaction beginTX() {
        return new Transaction(this);
    }
    // =================================
    private void reset() {
        added.clear();
        deleted.clear();
    }
    // commit top-level transaction
    public void commit() {
        try {
            for (Integer k : deleted) {
                index.remove(k);
            }
            for (IKeyed<Integer> k : added.values()) {
                index.add(repo.persist(k), k.key());
            }
            ObjectId treeId = index.commit();
            CommitBuilder commit = new CommitBuilder();
            commit.setCommitter(GitRepository.systemIdent());
            commit.setAuthor(GitRepository.systemIdent());
            commit.setParentIds(revision.getId());
            commit.setTreeId(treeId);
            try (CloseableObjectInserter coi = repo.getInserter()) {
                ObjectId commitId = coi.insert(commit);
                log.info("Commit: {}", commitId);
            }
        } catch (IOException e) {
            log.info("Error commiting", e);
            // TODO: how to reset?
        }


        reset();
    }

    // rollback top-level transaction
    public void rollback() {
        reset();
    }

    private Function<Keyed, Keyed> MAKE_IMMUTABLE = new Function<Keyed, Keyed>() {
        @Override
        public Keyed apply(Keyed k) {
            return k.immutable();
        }
    };
    // merge only (or last remaining) transaction from stack into baseline
    private void merge(Map<Integer, IKeyed> added, Set<Integer> deleted) {
        log.info("merge into baseline");

        // make each immutable
        // put each object in the repo
        // cache each object in the cache
        // delete from index
        // add to index
        try {
            for (IKeyed<Integer> k : added.values()) {
                ObjectId oid = repo.persist(k);
                index.add(oid, k.key());
                log.info("Added {} {}", k, oid.abbreviate(10).name());
            }
//            index.addAll(added);
            index.commit();
        } catch (IOException e) {

        }
    }

    // =================================
    public void add(Proxied p) {
        IKeyed<Integer> impl = p.impl();
        deleted.remove(impl.key());
        added.put(impl.key(), impl);
    }
    public void add(IKeyed<Integer> val) {
        deleted.remove(val.key());
        added.put(val.key(), val);
    }
    public void add(Integer key, IKeyed<Integer> val) {
        deleted.remove(key);
        added.put(key, val);
    }
    public void remove(Integer key) {
        added.remove(key);
        deleted.add(key);
    }

    private String getPath(Integer p) {
        return p.toString();
    }

    public <T extends IKeyed> T get(Integer key, Class<?> clazz) {
        if (deleted.contains(key))
            return null;
        if (added.containsKey(key))
            return (T)added.get(key);
        String p = getPath(key);
        ObjectId oid = null;
        try {
            oid = index.get(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (T)cache.getIfPresent(oid);
    }
    public <T extends IKeyed> T getForMutation(Integer key, Class<?> clazz) {
        log.info("getForMutation {}", key);
        IKeyed<Integer> k = get(key, clazz);
        if (null == k)
            return null;
        IKeyed<Integer> k2 = k.copy();
        add(key, k2);
        return (T)k2;
    }

    public Iterable<Keyed> list() {
        // TODO...
        return Collections.emptyList();
    }
    // =================================

    @Override
    public String toString() {
        return "DataStore from " + revision.abbreviate(10).name();
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
    final RevCommit revision;

    private static final AtomicInteger cnt = new AtomicInteger(0);
    private final int id = cnt.getAndIncrement();

    private final Map<Integer, IKeyed> added = newHashMap();
    private final Set<Integer> deleted = newHashSet();


}
