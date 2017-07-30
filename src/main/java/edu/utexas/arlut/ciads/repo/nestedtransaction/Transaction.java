// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo.nestedtransaction;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import edu.utexas.arlut.ciads.repo.Keyed;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;

@Slf4j
public class Transaction<T> extends Mergeable<T, Keyed<T>> implements Closeable {
    Transaction(final DataStore ds, final Mergeable<T, Keyed<T>> p) {
        this.ds = ds;
        this.parent = p;
    }
    public void add(T key, Keyed<T> val) {
        deleted.remove(key);
        added.put(key, val);
    }
    public void remove(T key) {
        added.remove(key);
        deleted.add(key);
    }


    public void commit() {
        if (closed) return;
//        Transaction<T> tx = ds.currentTX();
        if (ds.currentTX() != this) // yes, !=
            throw new DataStore.NotInnermostTransactionException();
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
            throw new DataStore.NotInnermostTransactionException();
        added.clear();
        deleted.clear();
        closed = true;
        ds.popTX();
    }
    @Override
    protected void merge(Map<T, Keyed<T>> added, Set<T> deleted) {
        log.info("merge into {}", this);
        for (T k : deleted)
            this.added.remove(k);
        this.added.putAll(added);
        this.deleted.addAll(deleted);
    }
    @Override
    public <T1 extends Keyed<T>> T1 get(T key) {
        if (deleted.contains(key))
            return null;
        if (added.containsKey(key))
            return (T1)added.get(key);
        if (null == parent)
            return (T1)ds._get(key);
        return (T1)parent.get(key);
    }
    @Override
    public <T1 extends Keyed<T>> T1 getMutable(T key) {
        log.info("tx {} getMutable", this);
        Keyed k = get(key);
        log.info("tx {} getMutable got {}", this, k);
        if (null == k)
            return null;
        Keyed k2 = k.mutable();
        add(key, k2);
        return (T1)k2;
    }

    private static Predicate<Keyed<?>> IN(final Set<?> deleted) {
        return new Predicate<Keyed<?>>() {
            @Override
            public boolean apply(Keyed<?> tKeyed) {
                return deleted.contains(tKeyed.id());
            }
        };
    }
    @Override
    public Iterable<Keyed<T>> list() {
        Iterable<Keyed<T>> it = Iterables.filter(parent.list(), IN(deleted));
        return Iterables.concat(it, added.values());
    }

    public void dump() {
        log.info("{} [parent {}]", this, parent);
        dumpMap("\t{} => {}", added);
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
    private Mergeable<T, Keyed<T>> parent;
    private final Map<T, Keyed<T>> added = newHashMap();
    private final Set<T> deleted = newHashSet();


}