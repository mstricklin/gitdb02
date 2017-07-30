package edu.utexas.arlut.ciads.repo.nestedtransaction;

import edu.utexas.arlut.ciads.repo.Keyed;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Maps.newHashMap;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;

@Slf4j
public class Index {

    public static Index of() {
        return new Index();
    }
    public static Index of(Integer branch) {
        return new Index(roots.get(branch));
    }
    public static Index of(Index i) {
        return new Index(i);
    }

    public static void dumpAll() {
        log.info("==Index dump ==");
        for (Map.Entry<Integer, Index> e: roots.entrySet()) {
            log.info("\tIndex {} => {}", e.getKey(), e.getValue());
            e.getValue().dump();
        }
    }

    Index() {
        baselineRev = null;
        index = newHashMap(  );
    }
    Index(Index baseline) {
        baselineRev = baseline.baselineRev;
        index = newHashMap( baseline.index );
    }
    void commit() {
        // TODO: make immutable
        roots.put(this.id, this);
    }

    Keyed<Integer> get(Integer key) {
        return index.get(key);
    }

    Keyed<Integer> put(Integer key, Keyed<Integer> val) {
        index.put(key, val);
        return val;
    }

    Index addAll(Map<Integer, Keyed<Integer>> m) {
        for (Keyed k: m.values()) {
            log.info(" path {} {}", k, k.path());
        }

        index.putAll(m);
        return this;
    }
    Index remove(Iterable<Integer> remove) {
        for (Integer id: remove) {
            index.remove(id);
        }
        return this;
    }
    Iterable<Keyed<Integer>> list() {
        return index.values();
    }
    @Override
    public String toString() {
        return "Index " + id;
    }
    public void dump() {
        log.info(" === {} dump ===", this);
        dumpMap("\t{} => {}", index);
    }

    // immutable need to keep path vs. oid
    // mutable need to keep path vs. Keyable


//    public static Index of(ObjectId id) {
//        Tree root = roots.get(id);
//        if (null == root) {
//            Tree t = new Tree();
//            roots.put(id, t);
//        }
//        return new Index(id);
//    }
//
//    Tree root = new Tree(); // varies by context...
//
//    void add(ObjectId oid, String path) {
//
//    }
//    Tree getTree(String path) {
//        return null;
//    }
//
//    public static class Tree {
//        Tree() {
//            parent = null;
//        }
//        Tree(Tree parent) {
//            this.parent = parent;
//        }
//
//        private final Tree parent;
//        private final Map<String, ObjectId> index = newHashMap();
//    }

    //    private static Map<ObjectId, Tree> roots = newHashMap();
    private static final Map<Integer, Index> roots = newHashMap();
    private static final AtomicInteger cnt = new AtomicInteger(0);

    private final Integer baselineRev;
    private final Map<Integer, Keyed<Integer>> index;
    private final int id = cnt.getAndIncrement();

}
