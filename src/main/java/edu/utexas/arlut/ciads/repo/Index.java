package edu.utexas.arlut.ciads.repo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.*;
import edu.utexas.arlut.ciads.repo.util.XPath;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static edu.utexas.arlut.ciads.repo.EntryUtil.toEntry;
import static edu.utexas.arlut.ciads.repo.StringUtil.*;

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

    private Map<XPath, Tree> getTrees(Iterable<XPath> paths) throws IOException {
        // All the parent trees of added and removed items will be modified,
        // get mutable versions of the Tree's for each parent.

        // all parent paths of added and deleted
        ImmutableSet<XPath> parentPaths = FluentIterable.from(paths)
                                                        .transformAndConcat(XPath.MAKE_PATHS)
                                                        .toSet();
        log.trace("parent paths {}", parentPaths);
        Map<XPath, Tree> m = newHashMap();
        for (XPath p : parentPaths) {
            final ObjectId id = lookup(p);
            m.put(p, (null == id) ? new Tree(gr)
                                  : new Tree(gr, id));
        }

        return m;
    }

    // TODO: this is O(n) on the size of the tree in both space and time
    // a context is about 12201+2756 elements * 30 bytes = ~500k copied, per commit.
    //  doing an actual windowed tree would be more like O(log n) in both space and time

    // one way to do this would be a single persistent DirCache per Index/DataStore,
    // then munge it per commit?
    // the best way would be to keep&write only the mutated trees
    ObjectId commit() throws IOException {
        log.info("Index commit add {}", addItems.keySet());
        log.info("Index commit rm  {}", rmItems);

        DirCache inCoreIndex = DirCache.newInCore();
        DirCacheBuilder dcb = inCoreIndex.builder();
        final TreeWalk tw = new TreeWalk(gr.newObjectReader());
        tw.addTree(baselineRevTree.getId());
        tw.setRecursive(true);
        while (tw.next()) {
            XPath xp = new XPath(tw.getPathString());
            if (rmItems.contains(xp)) {
                continue;
            }
            if (addItems.containsKey(xp))
                continue;
            dcb.add(toEntry(tw));
        }
        for (Map.Entry<XPath, ObjectId> e : addItems.entrySet())
            dcb.add(toEntry(e.getKey().toString(), e.getValue()));


        dcb.finish();
        ObjectInserter oi = gr.newObjectInserter();
        return gr.persist(inCoreIndex);
    }

    ObjectId lookup(final XPath p) throws IOException {
        return gr.lookupPath(baselineRevTree, p);
    }

    ObjectId lookup(final Integer id) throws IOException {
        log.info("lookup {}", id);
        return gr.lookupPath(baselineRevTree, path(id));
    }

    Index add(ObjectId id, Integer key) {
        XPath p = pathCache.getUnchecked(key);
        log.info("add to  {} {} {} => {}", this, key, p, id);
        addItems.put(p, id);
        return this;
    }

    Index remove(final Integer key) {
        XPath p = pathCache.getUnchecked(key);
        log.info("rm from {} {} {}", this, key, p);
        rmItems.add(p);
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

    final Map<XPath, ObjectId> addItems = newHashMap();
    final Set<XPath> rmItems = newHashSet();

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
    private static LoadingCache<ObjectId, Tree> trees3
            = CacheBuilder.newBuilder()
                          .maximumSize(1000)
                          .build(new CacheLoader<ObjectId, Tree>() {
                              public Tree load(ObjectId id) throws IOException {
                                  return new Tree(GitRepository.instance(), id);
                              }
                          });
    private static LoadingCache<Integer, XPath> pathCache
            = CacheBuilder.newBuilder()
                          .maximumSize(1000)
                          .build(
                                  new CacheLoader<Integer, XPath>() {
                                      public XPath load(Integer key) {
                                          return new XPath(key);
//                                          return String.format("%02x/%02x/%02x/%08x",
//                                                        (byte) ((key & 0xFF000000) >> 24),
//                                                        (byte) ((key & 0xFF000000) >> 16),
//                                                        (byte) ((key & 0xFF000000) >> 8),
//                                                        key);
                                      }
                                  });
}
