// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.revdb.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static edu.utexas.arlut.ciads.revdb.util.Strings.abbreviate;
import static edu.utexas.arlut.ciads.revdb.util.Strings.dumpMap;
import static edu.utexas.arlut.ciads.revdb.util.XPath.extractKeyFromPath;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.*;
import edu.utexas.arlut.ciads.revdb.DataView;
import edu.utexas.arlut.ciads.revdb.RevDBItem;
import edu.utexas.arlut.ciads.revdb.RevDBProxyItem;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;

@Slf4j
class GitDataView implements DataView {

    // one dataview per branch. If "name" isn't already a branch, make it one
    // off of our starting point.

    GitDataView(GitDataView startDS, final String name) throws IOException {
        this.revision = startDS.revision;
        this.name = name;
        gr = GitRepository.instance();
        index = new Index(revision.getTree());
        initialLoad();
    }

    // TODO: pass in a GitRepository to work off of.
    GitDataView(RevCommit revision, final String name) throws IOException {
        this.revision = revision;
        this.name = name;
        gr = GitRepository.instance();
        index = new Index(revision.getTree());
        initialLoad();
    }
    protected void initialLoad() throws IOException {
        log.info("initialLoad {}", revision);
        int max = 0;
        for (DirCacheEntry dce : gr.forAllFiles(revision)) {
            log.info("loading {}", dce);
            Integer key = extractKeyFromPath(dce.getPathString());
            RevDBItem k = gr.readObject(dce.getObjectId(), RevDBItem.class);
            store.put(extractKeyFromPath(dce.getPathString()), k);
            max = Math.max(max, key);
        }
        nextKey.set(max + 1);
        log.info("max key {}", nextKey.get());
    }

    @Override
    public void rename(String newName) {
        // TODO: move ref name
        name = newName;
    }

    RevCommit getCommit() {
        return revision;
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void shutdown() {
        // TODO: commit?
        log.info("Shutting down {}", this);
    }
    @Override
    public void dump() {
        log.info("dump {}", this);
        log.info("  = pending =");
        dumpMap("\t+ {} => {}", changedItems);
        log.info("\t- {}", deletedItems);
        log.info("  = persistent =");
        dumpMap("\t{} => {}", store);
    }

    // =================================
    public static class GitTransaction implements Transaction {
        private GitTransaction(DataView ds) {
            this.ds = ds;
        }
        private final DataView ds;
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
        changedItems.clear();
        deletedItems.clear();
    }
    // commit top-level transaction
    @Override
    public void commit() throws IOException {
        for (Integer key : deletedItems) {
            index.remove(key);
            store.remove(key);
        }
        for (Map.Entry<Integer, RevDBItem<?>> e : changedItems.entrySet()) {
            ObjectId id = gr.persist(e.getValue());
            index.add(id, e.getKey());
        }
        store.putAll(changedItems);
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
    public <T> T persist(RevDBItem<?> k) {
        Integer key = nextKey.getAndIncrement();
        deletedItems.remove(key);
        changedItems.put(key, k);
        return (T)k.proxyOf(key);
    }
    @Override
    public void remove(RevDBProxyItem p) {
        if (null == p)
            return;
        Integer key = p.getKey();
        changedItems.remove(key);
        deletedItems.add(key);
    }
    @Override
    public void remove(Integer key) {
        changedItems.remove(key);
        deletedItems.add(key);
    }
    private RevDBItem<?> _getImpl(Integer key, Class<? extends RevDBProxyItem> clazz) {
        if (deletedItems.contains(key))
            return null;
        if (changedItems.containsKey(key))
            return changedItems.get(key);
        return store.get(key);
    }

    @Override
    public RevDBItem<?> getImpl(Integer key, Class<? extends RevDBProxyItem> clazz) {
        return _getImpl(key, clazz);
    }
    @Override
    public RevDBItem<?> getImplForMutation(Integer key, Class<? extends RevDBProxyItem> clazz) {
        RevDBItem<?> k2 = _getImpl(key, clazz);
        if (null != k2) {
            k2 = k2.copy();
            deletedItems.remove(key);
            changedItems.put(key, k2);
            return k2;
        }
        return null;
    }
    @Override
    public <T> T get(Integer key, Class<? extends RevDBProxyItem> clazz) {
        RevDBItem<?> k2 = _getImpl(key, clazz);
        return (null == k2) ? null : (T)k2.proxyOf(key);
    }
    @Override
    public <T> T getForMutation(Integer key, Class<? extends RevDBProxyItem> clazz) {
        RevDBItem<?> k2 = getImplForMutation(key, clazz);
        return (null == k2) ? null : (T)k2.proxyOf(key);
    }
    @Override
    public Iterable<? extends RevDBItem> list() {
        return store.values();
    }
    @Override
    public Iterable<? extends RevDBItem> list(Class<? extends RevDBItem> clazz) {
        return Iterables.filter(store.values(), clazz);
    }
    // =================================
    @Override
    public String toString() {
        return String.format("GitDataView %s from %s", name, abbreviate(revision));
    }
    // =================================


    protected String name;
    protected GitRepository gr;
    protected RevCommit revision;
    private static final AtomicInteger nextKey = new AtomicInteger(0);

    protected Index index;

    // path => actual object
    protected Map<Integer, RevDBItem<?>> store = newHashMap();

    // transaction workspace
    private final Map<Integer, RevDBItem<?>> changedItems = newHashMap();
    private final Set<Integer> deletedItems = newHashSet();


}
