// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static edu.utexas.arlut.ciads.repo.ExceptionHelper.createDataStoreCreateAccessException;
import static edu.utexas.arlut.ciads.repo.ExceptionHelper.createRefNotFoundException;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;

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

    private DataStore(RevCommit rev, final String name) {
        log.info("Datastore startup from {}", rev.abbreviate(10).name());
        this.name = name;
        revision = rev;
        repo = GitRepository.instance();
    }
    private void load() {
        // TODO: load up the index from the persistent store.
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
        dumpMap("\t{} => {}", index);
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
    // =================================
    public Transaction beginTX() {
        return new Transaction(this);
    }
    private void reset() {
        added.clear();
        deleted.clear();
    }
    // commit top-level transaction
    public void commit() {
        for (String path: deleted) {
            index.remove(path);
        }
        // TODO: persist to underlying store...
        for (Map.Entry<String, IKeyed<?>> e: added.entrySet()) {
        }
        index.putAll(added);
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

    // =================================
    public <T> T add(IKeyed<?> k) {
        String key = Integer.toString( nextKey.getAndIncrement() );
        String path = StringUtil.path(k, key);
        deleted.remove(path);
        added.put(path, k);
        return (T)k.proxyOf(key);
    }
    public void remove(Proxy p) {
        String path = StringUtil.path(p.getClass(), p.getKey());
        added.remove(path);
        deleted.add(path);
    }
    public void remove(String path) {
        added.remove(path);
        deleted.add(path);
    }
    // TODO: move to e.g. Tools.java

    private IKeyed<?> _getImpl(String path) {
        if (deleted.contains(path))
            return null;
        if (added.containsKey(path))
            return added.get(path);
        return index.get(path);
    }
    private IKeyed<?> _getImpl(String key, Class<? extends Proxy> clazz) {
        final String path = StringUtil.path(clazz, key);
        return _getImpl(path);
    }

    public IKeyed<?> getImpl(String key, Class<? extends Proxy> clazz) {
        return _getImpl(key, clazz);
    }
    public IKeyed<?> getImplForMutation(String key, Class<? extends Proxy> clazz) {
        final String path = StringUtil.path(clazz, key);
        IKeyed<?> k2 = _getImpl(path);
        if (null != k2) {
            k2 = k2.copy();
            deleted.remove(path);
            added.put(path, k2);
            return k2;
        }
        return null;
    }
    public <T> T get(String key, Class<? extends Proxy> clazz) {
        IKeyed<?> k2 = _getImpl(key, clazz);
        return (null == k2) ? null : (T)k2.proxyOf(key);
    }
    public <T> T getForMutation(String key, Class<? extends Proxy> clazz) {
        IKeyed<?> k2 = getImplForMutation(key, clazz);
        return (null == k2) ? null : (T)k2.proxyOf(key);
    }

    public Iterable<IKeyed<?>> list() {
        return index.values();
    }
    static Predicate<String> startsWith(final String prefix) {
        return new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.startsWith(prefix);
            }
        };
    }
    public Iterable<IKeyed<?>> list(IKeyed<?> type) {
        Map<String, IKeyed<?>> f = Maps.filterKeys(index, startsWith(type.getType()));
        return f.values();
    }
    // =================================
    @Override
    public String toString() {
        return "DataStore from " + revision.abbreviate(10).name();
    }
    // =================================


    String name;
    GitRepository repo;
    RevCommit revision;
    private static final AtomicInteger nextKey = new AtomicInteger(0);

    //    private final Map<String, ObjectId> index = newHashMap();
//    Index index;

    // path => actual object
    Map<String, IKeyed<?>> index = newHashMap();

    // TODO: parameterize maximumSize
    private final Cache<ObjectId, IKeyed<?>> cache = CacheBuilder.newBuilder()
                                                              .maximumSize(2000)
                                                              .build();
    private final Map<String, IKeyed<?>> added = newHashMap();
    private final Set<String> deleted = newHashSet();


}
