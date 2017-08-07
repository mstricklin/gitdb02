// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static edu.utexas.arlut.ciads.repo.ExceptionHelper.createDataStoreCreateAccessException;
import static edu.utexas.arlut.ciads.repo.ExceptionHelper.createRefNotFoundException;
import static edu.utexas.arlut.ciads.repo.util.Strings.abbreviate;
import static edu.utexas.arlut.ciads.repo.util.Strings.dumpMap;
import static edu.utexas.arlut.ciads.repo.util.XPath.extractKeyFromPath;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;

@Slf4j
class GitDataStore implements DataStore {


    GitDataStore(RevCommit revision, final String name) throws IOException {
        this.revision = revision;
        this.name = name;
        gr = GitRepository.instance();
        index = new Index(revision.getTree());
        initialLoad();
    }
    private void initialLoad() throws IOException {
        int max = 0;
        for (DirCacheEntry dce: gr.forAllFiles(revision)) {
            Integer key = extractKeyFromPath(dce.getPathString());
            Keyed k = gr.readObject(dce.getObjectId(), Keyed.class);
            store.put(extractKeyFromPath(dce.getPathString()), k);
            max = Math.max(max, key);
            log.info("load {}", k);
        }
        nextKey.set(max+1);
        log.info("Max key {}", nextKey.get());
    }

    @Override
    public void rename(String newName) {
        // TODO: move ref name
        name = newName;
    }

    @Override
    public void shutdown() {
        log.info("Shutting down {}", this);
    }
    @Override
    public void dump() {
        log.info("dump {}", this);
        log.info("  = pending =");
        dumpMap("\t+ {} => {}", added);
        log.info("\t- {}", deleted);
        log.info("  = persistent =");
        dumpMap("\t{} => {}", store);
    }

    // =================================
    public static class GitTransaction implements Transaction {
        private GitTransaction(DataStore ds) {
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
    @Override
    public Transaction beginTX() {
        return new GitTransaction(this);
    }
    private void reset() {
        added.clear();
        deleted.clear();
    }
    // commit top-level transaction
    @Override
    public void commit() throws IOException {
        for (Integer key: deleted) {
            index.remove(key);
            store.remove(key);
        }
        for (Map.Entry<Integer, Keyed<?>> e: added.entrySet()) {
            ObjectId id = gr.persist(e.getValue());
            index.add(id, e.getKey());
        }
        store.putAll(added);
        reset();

        ObjectId commitTreeId = index.commit();
        RevCommit oldCommit = revision;
        revision = gr.commitAndUpdate(name, revision, commitTreeId);
        log.info("roll {} from {} to {}", name, abbreviate(oldCommit), abbreviate(revision));
    }

    // rollback top-level transaction
    @Override
    public void rollback() {
        reset();
    }
    // =================================
    @Override
    public <T> T persist(Keyed<?> k) {
        Integer key = nextKey.getAndIncrement();
        deleted.remove(key);
        added.put(key, k);
        return (T)k.proxyOf(key);
    }
    @Override
    public void remove(Proxy p) {
        if (null == p)
            return;
        Integer key = p.getKey();
        added.remove(key);
        deleted.add(key);
    }
    @Override
    public void remove(Integer key) {
        added.remove(key);
        deleted.add(key);
    }
    private Keyed<?> _getImpl(Integer key, Class<? extends Proxy> clazz) {
        if (deleted.contains(key))
            return null;
        if (added.containsKey(key))
            return added.get(key);
        return store.get(key);
    }

    @Override
    public Keyed<?> getImpl(Integer key, Class<? extends Proxy> clazz) {
        return _getImpl(key, clazz);
    }
    @Override
    public Keyed<?> getImplForMutation(Integer key, Class<? extends Proxy> clazz) {
        Keyed<?> k2 = _getImpl(key, clazz);
        if (null != k2) {
            k2 = k2.copy();
            deleted.remove(key);
            added.put(key, k2);
            return k2;
        }
        return null;
    }
    @Override
    public <T> T get(Integer key, Class<? extends Proxy> clazz) {
        Keyed<?> k2 = _getImpl(key, clazz);
        return (null == k2) ? null : (T)k2.proxyOf(key);
    }
    @Override
    public <T> T getForMutation(Integer key, Class<? extends Proxy> clazz) {
        Keyed<?> k2 = getImplForMutation(key, clazz);
        return (null == k2) ? null : (T)k2.proxyOf(key);
    }
    @Override
    public Iterable<? extends Keyed> list() {
        return store.values();
    }
    @Override
    public Iterable<? extends Keyed> list(Class<? extends Keyed> clazz) {
        return Iterables.filter(store.values(), clazz);
    }
    // =================================
    @Override
    public String toString() {
        return String.format("GitDataStore %s from %s", name, abbreviate(revision));
    }
    // =================================


    String name;
    GitRepository gr;
    RevCommit revision;
    private static final AtomicInteger nextKey = new AtomicInteger(0);

    Index index;

    // path => actual object
    Map<Integer, Keyed<?>> store = newHashMap();

    // transaction workspace
    private final Map<Integer, Keyed<?>> added = newHashMap();
    private final Set<Integer> deleted = newHashSet();


}
