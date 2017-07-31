// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Transaction extends Mergeable<Integer, Keyed> implements Closeable {
    Transaction(final DataStore ds) {
        this.ds = ds;
    }

    public void add(Integer key, Keyed val) {
        deleted.remove(key);
        added.put(key, val);
    }

    public void remove(Integer key) {
        added.remove(key);
        deleted.add(key);
    }


    private void reset() {
        added.clear();
        deleted.clear();
    }
    public void commit() {
        ds.merge(added, deleted);
        reset();
    }

    public void rollback() {
        reset();
    }

    @Override
    protected void merge(Map<Integer, Keyed> added, Set<Integer> deleted) {
        log.info("merge into {}", this);
        for (Integer k : deleted)
            this.added.remove(k);
        this.added.putAll(added);
        this.deleted.addAll(deleted);
    }

    @Override
    public <T extends Keyed> T get(Integer key, Class<?> clazz) {
        if (deleted.contains(key))
            return null;
        if (added.containsKey(key))
            return (T) added.get(key);
        return (T) ds._get(key, clazz);
    }

    @Override
    public <T extends Keyed> T getMutable(Integer key, Class<?> clazz) {
        log.info("tx {} getMutable", this);
        Keyed k = get(key, clazz);
        log.info("tx {} getMutable got {}", this, k);
        if (null == k)
            return null;
        Keyed k2 = k.mutable();
        add(key, k2);
        return (T) k2;
    }

    private static Predicate<Keyed> IN(final Set<?> deleted) {
        return new Predicate<Keyed>() {
            @Override
            public boolean apply(Keyed tKeyed) {
                return deleted.contains(tKeyed.key());
            }
        };
    }

    @Override
    public Iterable<Keyed> list() {
        Iterable<Keyed> it = Iterables.filter(ds._list(), IN(deleted));
        return Iterables.concat(it, added.values());
    }

    public void dump() {
        log.info(this.toString());
        dumpMap("\t{} => {}", added);
        log.info("\tX{}", deleted);
    }

    @Override
    public void close() {
        rollback();
    }

    @Override
    public String toString() {
        return "Transaction " + id;
    }

    private static final AtomicInteger cnt = new AtomicInteger(0);
    private final int id = cnt.getAndIncrement();
    private final DataStore ds;
    private final Map<Integer, Keyed> added = newHashMap();
    private final Set<Integer> deleted = newHashSet();


}