// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.revdb.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static edu.utexas.arlut.ciads.revdb.util.Strings.abbreviate;
import static edu.utexas.arlut.ciads.revdb.util.Strings.dumpMap;
import static edu.utexas.arlut.ciads.revdb.util.XPath.extractIDFromPath;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.*;
import edu.utexas.arlut.ciads.revdb.DataView;
import edu.utexas.arlut.ciads.revdb.RevDBItem;
import edu.utexas.arlut.ciads.revdb.RevDBProxyItem;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;

@Slf4j
class GitDataView implements DataView {

    // one dataview per branch.
    // TODO: throw if "name" isn't a branch

    GitDataView(GitDataView startDV, final String name) throws IOException {
        checkNotNull(startDV);
        this.revision = startDV.revision;
        this.name = name;
        gr = startDV.gr;
        index = new Index(revision.getTree());
        initialLoad();
    }

    // TODO: pass in a GitRepository to work off of.
    GitDataView(GitRepository gr, RevCommit revision, final String name) throws IOException {
        this.revision = revision;
        this.name = name;
        this.gr = gr;
        index = new Index(revision.getTree());
        initialLoad();
    }
    protected void initialLoad() throws IOException {
        log.info("initialLoad {}", revision);
        int max = 0;
        for (DirCacheEntry dce : gr.forAllFiles(revision)) {
            log.info("loading {}", dce);
            Integer id = extractIDFromPath(dce.getPathString());
            RevDBItem k = gr.readObject(dce.getObjectId(), RevDBItem.class);
            store.put(extractIDFromPath(dce.getPathString()), k);
            max = Math.max(max, id);
        }
        nextId.set(max + 1);
        log.info("max id {}", nextId.get());
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
        for (Integer id : deletedItems) {
            index.remove(id);
            store.remove(id);
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
    public <T> T managedInstance(Class<T> clazz) {
        Integer id = nextId.getAndIncrement();
        try {
            T t = clazz.newInstance();
            deletedItems.remove(id);
            changedItems.put(id, k);
            return (T)k.proxyOf(id);
        } catch (InstantiationException|IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public <T> T persist(RevDBItem<?> k) {
        Integer id = nextId.getAndIncrement();
        deletedItems.remove(id);
        changedItems.put(id, k);
        return (T)k.proxyOf(id);
    }
    @Override
    public void remove(RevDBProxyItem p) {
        if (null == p)
            return;
        Integer id = p.getId();
        changedItems.remove(id);
        deletedItems.add(id);
    }
    @Override
    public void remove(Integer id) {
        changedItems.remove(id);
        deletedItems.add(id);
    }
    private RevDBItem<?> _getImpl(Integer id, Class<? extends RevDBProxyItem> clazz) {
        if (deletedItems.contains(id))
            return null;
        if (changedItems.containsKey(id))
            return changedItems.get(id);
        return store.get(id);
    }

    @Override
    public RevDBItem<?> getImpl(Integer id, Class<? extends RevDBProxyItem> clazz) {
        return _getImpl(id, clazz);
    }
    @Override
    public RevDBItem<?> getImplForMutation(Integer id, Class<? extends RevDBProxyItem> clazz) {
        RevDBItem<?> k2 = _getImpl(id, clazz);
        if (null != k2) {
            k2 = k2.copy();
            deletedItems.remove(id);
            changedItems.put(id, k2);
            return k2;
        }
        return null;
    }
    @Override
    public <T> T get(Integer id, Class<? extends RevDBProxyItem> clazz) {
        RevDBItem<?> k2 = _getImpl(id, clazz);
        return (null == k2) ? null : (T)k2.proxyOf(id);
    }
    @Override
    public <T> T getForMutation(Integer id, Class<? extends RevDBProxyItem> clazz) {
        RevDBItem<?> k2 = getImplForMutation(id, clazz);
        return (null == k2) ? null : (T)k2.proxyOf(id);
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
    private static final AtomicInteger nextId = new AtomicInteger(0);

    protected Index index;

    // path => actual object
    protected Map<Integer, RevDBItem<?>> store = newHashMap();

    // transaction workspace
    private final Map<Integer, RevDBItem<?>> changedItems = newHashMap();
    private final Set<Integer> deletedItems = newHashSet();


}
