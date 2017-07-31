package edu.utexas.arlut.ciads.repo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.primitives.Ints;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Tree;
import org.eclipse.jgit.lib.TreeEntry;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Maps.newHashMap;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;

@Slf4j
public class Index<T> {

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
        this.baselineTree = baseline;
//        index = newHashMap( baseline.index );
//        rootTree = new Tree(); // TODO...

    }

    void commit() {
        // TODO: make immutable?
        roots.put(this.id, this);
    }

    ObjectId get(final Keyed.Path p) throws IOException {
        final GitRepository gr = GitRepository.instance();
        try {
            return index.get(p, new Callable<ObjectId>() {
                @Override
                public ObjectId call() throws IOException, NoSuchElementException {
                    TreeWalk tw = TreeWalk.forPath(gr.repo(), p.path, baselineTree);
                    if (!tw.next()) {
                        throw new NoSuchElementException();
                    }
                    return tw.getObjectId(0);
                }
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    void put(final Keyed.Path p, ObjectId oid) {
        index.put(p, oid);
    }

    Index addAll(Map<T, Keyed> m) {
        final GitRepository gr = GitRepository.instance();

        try {
            byte[] b = gr.readObject(baselineTree);
            Tree t = new Tree(gr.repo(), baselineTree, b);
            log.info("{} isLoaded {}", t, t.isLoaded());
            for (TreeEntry te: t.members()) {
                log.info("TE: {} {} {}", te.getName(), te.getMode(), te.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("");
        try {
            Tree t = new Tree(gr.repo());
            t.setId(baselineTree);
            log.info("{} isLoaded {}", t, t.isLoaded());
            for (TreeEntry te: t.members()) {
                log.info("TE: {} {} {}", te.getName(), te.getMode(), te.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // flatten tree?
        log.info("addAll");
        try {
            TreeFormatter tf = new TreeFormatter();
            TreeWalk tw = new TreeWalk(gr.repo());
            tw.addTree(baselineTree);
            tw.setRecursive(false);
            while (tw.next()) {
                tf.append(tw.getNameString(), tw.getFileMode(0), tw.getObjectId(0));
                log.info("{} {} {}", tw.getPathString(), tw.getFileMode(0), tw.getObjectId(0));
            }
            log.info("TreeFormatter {}", tf);
        } catch (IOException e) {

        }

//        for (Keyed k : m.values()) {
//            log.info("keyed[{}] path {}", k, k.path.path);
//            Keyed.Path p = k.path;

//            ObjectId oid0 = rootTree.get( p);
//            Tree t0 = trees.get(oid0);
//            if (t0 == null) {
//                t0 = new Tree();
//            }

            // persist k
            // add k[oid] to cache
            // add k.path(), k[oid] to t0

//        }
        // persist all mutated trees


//        index.putAll(m);
        return this;
    }

    Index remove(Iterable<Keyed.Path> remove) {
//        for (T id: remove.a) {
//            index.remove(id);
        index.invalidateAll(remove);
//        }
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

    // immutable need to keep path vs. oid
    // mutable need to keep path vs. Keyable

    private static final Map<Integer, Index> roots = newHashMap();
    private static final AtomicInteger cnt = new AtomicInteger(0);

    //    private final Map<T, Keyed> index;
    private final Map<ObjectId, Tree> trees = newHashMap();

//    private final Map<Keyed.Path, ObjectId> index;

    //    private final Map<ObjectId, >
    private final int id = cnt.getAndIncrement();

//    private final Tree rootTree;

    // TODO: add a watcher for evictions, to check if we're too small
    Cache<Keyed.Path, ObjectId> index = CacheBuilder.newBuilder()
                                                    .maximumSize(10000)
                                                    .build();
    //    private final DataStore datastore;
    private final RevTree baselineTree;
}
