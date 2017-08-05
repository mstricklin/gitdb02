// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static edu.utexas.arlut.ciads.repo.ExceptionHelper.createDataStoreCreateAccessException;
import static edu.utexas.arlut.ciads.repo.ExceptionHelper.createRefNotFoundException;
import static edu.utexas.arlut.ciads.repo.StringUtil.abbreviate;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

@Slf4j
public class DataStore {

    private static Cache<String, DataStore> stores = CacheBuilder.newBuilder()
                                                                 .maximumSize(100)
                                                                 .build();
    // =================================
    // create from baseline repo, or return existing
    public static DataStore of(final RevCommit baseline, final String name) throws ExceptionHelper.DataStoreCreateAccessException {
        checkNotNull(baseline);
        checkNotNull(name);

        try {
            return stores.get(name, new Callable<DataStore>() {
                @Override
                public DataStore call() throws IOException {
                    final GitRepository gr = GitRepository.instance();
                    Ref branch = gr.getBranch(name);
                    if (null == branch)
                        branch = gr.branch(baseline, name);

                    RevCommit commit = gr.getCommit(branch);
                    return new DataStore(commit, name);
                }
            });
        } catch (ExecutionException e) {
            throw createDataStoreCreateAccessException(name, e.getCause());
        }
    }
    // create detached, or return existing
    public static DataStore detached(final String name) throws ExceptionHelper.DataStoreCreateAccessException, IOException {
        checkNotNull(name);
        DataStore ds = stores.getIfPresent(name);
        if (null == ds) {
            final GitRepository gr = GitRepository.instance();
            RevCommit empty = gr.getCommit("root^{}");
            return of(empty, name);
        }
        return ds;
    }
    // return existing
    public static DataStore existing(final String name) throws RefNotFoundException {
        checkNotNull(name);
        DataStore ds = stores.getIfPresent(name);
        if (null == ds)
            throw createRefNotFoundException(name);
        return ds;
    }

    // new DataStore, from baseline, called name
    //
    // get DataStore called name
    //
    // rename DataStore to newName

    private DataStore(RevCommit revision, final String name) {
        this.revision = revision;
        this.name = name;
        gr = GitRepository.instance();
        index = new Index(revision.getTree());
        try {
            for (String s: gr.forAllFiles(revision, f))
                log.info("add file {}", s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("DataStore startup {}", toString());
    }
    private Function<TreeWalk, String> f = new Function<TreeWalk, String>() {
        @Override
        public String apply(TreeWalk tw) {
            return tw.getPathString();
        }
    };
    private void load() {
        // TODO: load up the store from the persistent store.
    }

    public void rename(String newName) {
        // TODO: move ref name

        stores.invalidate(newName);
        stores.put(newName, this);
        name = newName;
    }

    public void shutdown() {
        log.info("Shutting down datastore {}", this);
    }
    public void dump() {
        log.info("dump {}", this);
        log.info("  = pending =");
        dumpMap("\t+ {} => {}", added);
        log.info("\t- {}", deleted);
        log.info("  = persistent =");
        dumpMap("\t{} => {}", store);
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
        public void commit() throws IOException {
            ds.commit();
        }
        public void rollback() {
            ds.rollback();
        }
    }
    // =================================
    public Transaction beginTX() {
        return new Transaction(this);
    }
    private void reset() {
        added.clear();
        deleted.clear();
    }
    // commit top-level transaction
    public void commit() throws IOException {
        for (Integer key: deleted) {
            store.remove(key);
        }
        for (Map.Entry<Integer, IKeyed<?>> e: added.entrySet()) {
            ObjectId id = gr.persist(e.getValue());
            index.add(id, e.getKey());
//            store.add(e.getKey(),e.getValue());
        }
        store.putAll(added);
        reset();
        index.commit();
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

    // =================================
    public <T> T add(IKeyed<?> k) {
        Integer key = nextKey.getAndIncrement();
        deleted.remove(key);
        added.put(key, k);
        return (T)k.proxyOf(key);
    }
    public void remove(Proxy p) {
        Integer key = p.getKey();
        added.remove(key);
        deleted.add(key);
    }
    public void remove(Integer key) {
        added.remove(key);
        deleted.add(key);
    }
    private IKeyed<?> _getImpl(Integer key, Class<? extends Proxy> clazz) {
        if (deleted.contains(key))
            return null;
        if (added.containsKey(key))
            return added.get(key);
        return store.get(key);
    }

    public IKeyed<?> getImpl(Integer key, Class<? extends Proxy> clazz) {
        return _getImpl(key, clazz);
    }
    public IKeyed<?> getImplForMutation(Integer key, Class<? extends Proxy> clazz) {
        IKeyed<?> k2 = _getImpl(key, clazz);
        if (null != k2) {
            k2 = k2.copy();
            deleted.remove(key);
            added.put(key, k2);
            return k2;
        }
        return null;
    }
    public <T> T get(Integer key, Class<? extends Proxy> clazz) {
        IKeyed<?> k2 = _getImpl(key, clazz);
        return (null == k2) ? null : (T)k2.proxyOf(key);
    }
    public <T> T getForMutation(Integer key, Class<? extends Proxy> clazz) {
        IKeyed<?> k2 = getImplForMutation(key, clazz);
        return (null == k2) ? null : (T)k2.proxyOf(key);
    }

    public Iterable<? extends IKeyed> list(Class<? extends IKeyed> clazz) {
        return Iterables.filter(store.values(), clazz);
    }
    static Predicate<String> startsWith(final String prefix) {
        return new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.startsWith(prefix);
            }
        };
    }
    public Iterable<IKeyed<?>> list(String type) {
        IKeyed k = Iterables.getFirst(store.values(), null);
        k.getType();
//        Map<String, IKeyed<?>> f = Maps.filterKeys(store, startsWith(type.getType()));
//        return f.values();
        return Collections.emptyList();
    }
    // =================================
    @Override
    public String toString() {
        return String.format("DataStore %s from %s", name, abbreviate(revision));
    }
    // =================================


    String name;
    GitRepository gr;
    RevCommit revision;
    private static final AtomicInteger nextKey = new AtomicInteger(0);

    Index index;

    // path => actual object
    Map<Integer, IKeyed<?>> store = newHashMap();

    // TODO: parameterize maximumSize
//    private final Cache<ObjectId, IKeyed<?>> cache = CacheBuilder.newBuilder()
//                                                              .maximumSize(2000)
//                                                              .build();
    private final Map<Integer, IKeyed<?>> added = newHashMap();
    private final Set<Integer> deleted = newHashSet();


}
