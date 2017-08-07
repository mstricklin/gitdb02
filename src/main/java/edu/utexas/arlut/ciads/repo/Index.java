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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static edu.utexas.arlut.ciads.repo.util.Entries.toEntry;
import static edu.utexas.arlut.ciads.repo.util.Strings.*;

@Slf4j
public class Index {

    static Index of(RevTree baseline) {
        return new Index(baseline);
    }

    static void dumpAll() {
        log.info("==Index dump ==");
        for (Map.Entry<Integer, Index> e : roots.entrySet()) {
            log.info("\tIndex {} => {}", e.getKey(), e.getValue());
            e.getValue().dump();
        }
    }

    Index(RevTree baseline) {
        this.baselineRevTree = baseline;
        this.gr = GitRepository.instance();
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

    // one way to do this would be a single persistent DirCache per Index/GitDataStore,
    // then munge it per commit?
    // the best way would be to keep&write only the mutated trees
    ObjectId commit() throws IOException {
        log.info("Index commit persist {}", addItems.keySet());
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
        log.trace("lookup {}", id);
        return gr.lookupPath(baselineRevTree, XPath.of(id));
    }

    Index add(ObjectId id, Integer key) {
        XPath p = XPath.of(key);
        log.trace("persist to  {} {} {} => {}", this, key, p, id);
        addItems.put(p, id);
        return this;
    }

    Index remove(final Integer key) {
        XPath p = XPath.of(key);
        log.trace("rm from {} {} {}", this, key, p);
        rmItems.add(p);
        return this;
    }

    @Override
    public String toString() {
        return "Index " + id;
    }

    void dump() {
        log.info("\t{} dump", this);
        dumpMap("\t+ {} => {}", addItems);
        log.info("\t- {}", rmItems);
    }

    // =================================
    private static final Map<Integer, Index> roots = newHashMap();
    private static final AtomicInteger cnt = new AtomicInteger(0);
    private final int id = cnt.getAndIncrement();

    // TODO: persist a watcher for evictions, to check if we're too small
    Cache<Integer, ObjectId> index = CacheBuilder.newBuilder()
                                                 .maximumSize(10000)
                                                 .build();
    //    private final GitDataStore datastore;
    private final RevTree baselineRevTree;
    final GitRepository gr;

    final Map<XPath, ObjectId> addItems = newHashMap();
    final Set<XPath> rmItems = newHashSet();
}
