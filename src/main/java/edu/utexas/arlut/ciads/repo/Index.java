package edu.utexas.arlut.ciads.repo;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSetWithExpectedSize;
import static edu.utexas.arlut.ciads.repo.StringUtil.MAKE_PATHS;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;
import static edu.utexas.arlut.ciads.repo.StringUtil.path;

@Slf4j
public class Index {

    public static Index of(RevTree baseline) {
        return new Index(baseline);
    }

    public static void dumpAll() {
        log.info("==Index dump ==");
        for (Map.Entry<Integer, Index> e : roots.entrySet()) {
            log.info("\tIndex {} => {}", e.getKey(), e.getValue());
            e.getValue().dump();
        }
    }

    Index(RevTree baseline) {
        this.baselineRevTree = baseline;
        this.gr = GitRepository.instance();
        try {
            rootTree = new Tree(gr, baselineRevTree.getId());
            trees.put(null, rootTree);
        } catch (IOException e) {
            // TODO - exception
            e.printStackTrace();
        }
    }


    ObjectId commit() throws IOException {
        log.info("Index commit {}", items.keySet());

        Map<String, Tree> parents = newHashMap();
        for (Map.Entry<String, ObjectId> e: items.entrySet()) {
//            TryPath p = Paths.get
        }

//        ImmutableSet<String> paths = FluentIterable.from(items.keySet())
//                                                   .transformAndConcat(MAKE_PATHS)
//                                                   .toSet();

//        Map<String, Tree> trees = newHashMap();
//        for (String p : paths) {
//            ObjectId pathId = gr.lookupPath(baselineRevTree, p);
//            trees.put(p, (null == pathId) ? new Tree(gr)
//                                          : new Tree(gr, pathId));
//        }
        log.info("trees:");
        dumpMap("\t{} =>{}", trees);

        Set<String> s= newLinkedHashSetWithExpectedSize(5);
        for (Map.Entry<String, ObjectId> e: items.entrySet()) {
            log.info("get parent for {}", e.getKey());
        }
        // do delete

//            log.info("commit {} => {}", e.getKey(), e.getValue());
//            for (String subPath : StringUtil.paths(e.getKey())) {
//                if ( ! trees.containsKey(subPath)) {
//                    final ObjectId pID = gr.lookupPath(baselineRevTree, subPath);
//
//                }
//
//                final ObjectId pID = gr.lookupPath(baselineRevTree, subPath);
//                if (null == pID) {
//                    trees.put(subPath, new Tree(gr));
//                } else {
//                    trees.put(subPath, new Tree(gr, pID));
//
//                    try {
//                        trees2.get(subPath, new Callable<Tree>() {
//                            @Override
//                            public Tree call() throws Exception {
//                                return new Tree(gr, pID);
//                            }
//                        });
//                    } catch (ExecutionException ee) {
//                        ee.printStackTrace();
//                    }
//                }
//                log.info("\t{} {}", subPath, pID);
//            }
//        }
        // TODO: this is O(n) on the size of the tree in both space and time
        // a context is about 12201+2756 elements, * 30 bytes = ~500k copied, per commit
        //  doing an actual windowed tree would be more like O(log n) in both space and time

        // one way to do this would be a single persistent DirCache per index, munge it per commit?
        // the best way would be to keep&write only the mutated trees
//        final DirCache inCoreIndex = DirCache.newInCore();
//        final DirCacheBuilder dcBuilder = inCoreIndex.builder();
//        final ObjectReader or = gr.newObjectReader();
//        dcBuilder.addTree(null, 0, or, baselineRevTree.getId());
//        dcBuilder.finish();
//        for (int i = 0; i < inCoreIndex.getEntryCount(); i++) {
//            log.info("Entry {}", inCoreIndex.getEntry(i));
//        }

//        for (Tree.GitTreeEntry e : rootTree) {
//            log.info("Tree.GitTreeEntry {}", e);
//        }
//        try {
//            ObjectId treeId = GitRepository.instance().persistTree(rootTree.format());
//            log.info("treeId {}", treeId.name());
//            return treeId;
//        } catch (IOException e) {
//            // TODO - exception
//            log.error("Error formatting tree {}", rootTree);
//            log.error("", e);
//            return null;
//        }
        return null;
    }

    ObjectId lookup(final Integer id) throws IOException {
        log.info("lookup {}", id);
        return gr.lookupPath(baselineRevTree, path(id));
    }

    Index add(ObjectId id, Integer key) {
        String path = pathCache.getUnchecked(key);
        log.info("add to {} {} {} => {}", this, key, path, id);
        items.put(path, id);
        return this;
    }

    Index remove(final Integer key) {
        String path = pathCache.getUnchecked(key);
        items.remove(path);
        return this;
    }

    Iterable<ObjectId> list() {
        return index.asMap().values();
    }

    @Override
    public String toString() {
        return "Index " + id;
    }

    public void dump() {
        log.info("\t{} dump", this);
        dumpMap("\t{} => {}", index.asMap());
    }

    // =================================
    Tree rootTree;
    private static final Map<Integer, Index> roots = newHashMap();
    private static final AtomicInteger cnt = new AtomicInteger(0);
    private final int id = cnt.getAndIncrement();

    // TODO: add a watcher for evictions, to check if we're too small
    Cache<Integer, ObjectId> index = CacheBuilder.newBuilder()
                                                 .maximumSize(10000)
                                                 .build();
    //    private final DataStore datastore;
    private final RevTree baselineRevTree;
    final GitRepository gr;

    final Map<String, ObjectId> items = newHashMap();
    final Map<String, Tree> trees = newHashMap();

//    Cache<String, Tree> trees2 = CacheBuilder.newBuilder()
//                                          .maximumSize(200)
//                                          .build();

    private LoadingCache<String, Tree> trees2
            = CacheBuilder.newBuilder()
                          .maximumSize(200)
                          .build(
                                  new CacheLoader<String, Tree>() {
                                      public Tree load(String key) {
                                          GitRepository gr2 = GitRepository.instance();
                                          return new Tree(gr2);
                                      }
                                  });
    private static LoadingCache<Integer, String> pathCache
            = CacheBuilder.newBuilder()
                          .maximumSize(1000)
                          .build(
                                  new CacheLoader<Integer, String>() {
                                      public String load(Integer key) {
                                          return path(key);
                                      }
                                  });
}
