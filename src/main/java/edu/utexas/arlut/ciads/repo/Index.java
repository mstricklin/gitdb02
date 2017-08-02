package edu.utexas.arlut.ciads.repo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Maps.newHashMap;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;

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

    // TODO: try DirCache!!!
    Index(RevTree baseline) {
        this.baselineTree = baseline;
        gr = GitRepository.instance();
        ObjectId treeID = baselineTree.getId();
        try {
            rootTree = new Tree(gr.repo(), treeID);
        } catch (IOException e) {
            // TODO - exception
            e.printStackTrace();
        }
    }

    ObjectId commit() throws IOException {
        // TODO: this is O(n) on the size of the tree in both space and time
        // a context is about 12201+2756 elements, * 30 bytes = ~500k copied, per commit
        //  doing an actual windowed tree would be more like O(log n) in both space and time

        // one way to do this would be a single persistent DirCache per index, munge it per commit?
        // the best way would be to keep&write only the mutated trees
//        final DirCache inCoreIndex = DirCache.newInCore();
//        final DirCacheBuilder dcBuilder = inCoreIndex.builder();
//        final ObjectReader or = gr.newObjectReader();
//        dcBuilder.addTree(null, 0, or, baselineTree.getId());
//        dcBuilder.finish();
//        for (int i = 0; i < inCoreIndex.getEntryCount(); i++) {
//            log.info("Entry {}", inCoreIndex.getEntry(i));
//        }

        for (Tree.GitTreeEntry e : rootTree) {
            log.info("Tree.GitTreeEntry {}", e);
        }
        try {
            ObjectId treeId = GitRepository.instance().persistTree(rootTree.format());
            log.info("treeId {}", treeId.name());
            return treeId;
        } catch (IOException e) {
            // TODO - exception
            log.error("Error formatting tree {}", rootTree);
            log.error("", e);
            return null;
        }
    }

    private String getPath(Integer p) {
        return Integer.toString(p);
    }

    ObjectId lookup(final Integer id) throws IOException {
        log.info("lookup {}", id);
        return gr.repo().resolve(baselineTree.getId().name() + ':' + getPath(id));

//        TreeWalk tw = TreeWalk.forPath(repo, "0", commitTree);
//        if (null != tw) {
//            pathId = tw.getObjectId(0);
//            log.info("pathId: {}", pathId.name());
//        }
//
//        try {
//            gr.repo().resolve(baselineTree.getId().name() + ':' + getPath(id));
//
//            return index.lookup(id, new Callable<ObjectId>() {
//                @Override
//                public ObjectId call() throws IOException, NoSuchElementException {
//                    TreeWalk tw = TreeWalk.forPath(gr.repo(), getPath(id), baselineTree);
//                    if (!tw.next()) {
//                        throw new NoSuchElementException();
//                    }
//                    return tw.getObjectId(0);
//                }
//            });
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        return null;
    }

    Index add(ObjectId oid, final Integer id) {
        // TODO: parse out this entry's path, and add resulting Tree to cache

        rootTree.addBlob(getPath(id), oid);
        return this;
    }

    Index remove(final Integer id) {
        rootTree.remove(getPath(id));
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
    // immutable need to keep path vs. oid
    // mutable need to keep path vs. Keyable

    Tree rootTree;

    private static final Map<Integer, Index> roots = newHashMap();
    private static final AtomicInteger cnt = new AtomicInteger(0);

    //    private final Map<T, Proxied> index;
    private final Map<ObjectId, Tree> trees = newHashMap();

//    private final Map<Proxied.Path, ObjectId> index;

    //    private final Map<ObjectId, >
    private final int id = cnt.getAndIncrement();

//    private final Tree rootTree;

    // TODO: add a watcher for evictions, to check if we're too small
    Cache<Integer, ObjectId> index = CacheBuilder.newBuilder()
                                                 .maximumSize(10000)
                                                 .build();
    //    private final DataStore datastore;
    private final RevTree baselineTree;
    final GitRepository gr;
}
