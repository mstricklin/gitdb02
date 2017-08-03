package edu.utexas.arlut.ciads.example;

import edu.utexas.arlut.ciads.repo.GitRepository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.RenameBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuildIterator;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.NoSuchElementException;

import static com.google.common.collect.Lists.newArrayList;

@Slf4j
public class TryDirCache {
    static GitRepository gr;

    public static void main(String[] args) throws GitAPIException, IOException {
        gr = new GitRepository("t.git");
        Repository repo = gr.repo();

        ObjectId pathId = repo.resolve("abc754f719124b8e286e104c1068ba72539e5002:0/1/2");
        log.info("pathId: {}", pathId.name());

        ObjectId commitTree = ObjectId.fromString("abc754f719124b8e286e104c1068ba72539e5002");
        log.info("commitTree: {}", commitTree.name());
        ObjectId commitId = ObjectId.fromString("f182210f25df259a901422a8279f00fc9566d518");

        RevWalk rw = new RevWalk(repo);
        RevCommit rc = rw.parseCommit(commitId);
        rw.dispose();
        log.info("RevCommit {}", rc);
        log.info("RevCommit {}", rc.getTree());
        log.info("RevCommit {}", rc.getAuthorIdent());

        ObjectReader or = repo.newObjectReader();
        TreeWalk tw = TreeWalk.forPath(repo, "0", commitTree);
        if (null != tw) {
            pathId = tw.getObjectId(0);
            log.info("pathId: {}", pathId.name());
        } else
            log.info("No such");

        TreeWalk tw0 = new TreeWalk(repo);
        tw0.addTree(commitTree);
        tw0.setRecursive(true);
        while (tw0.next()) {
            log.info("tw0 {}", tw0.getObjectId(0));
        }
        tw0.release();

//        final DirCache inCoreIndex = DirCache.newInCore();
//        final DirCacheBuilder dcBuilder = inCoreIndex.builder();
//        final ObjectInserter inserter = repo.newObjectInserter();
//
//        DirCacheEntry dce = addFile("0", "sam");
//        dcBuilder.add(dce);
//        dce = addFile("1", "sam/i/am");
//        dcBuilder.add(dce);
//        dce = addFile("sam i am 2", "green/eggs/ham");
//        dcBuilder.add(dce);
//        dce = addFile("sam i am 3", "green/eggs/ham3");
//        dcBuilder.add(dce);
////        log.info("dcBuilder {}", dcBuilder);
//        dcBuilder.finish();
//
//        for (int i = 0; i < inCoreIndex.getEntryCount(); i++) {
//            log.info("Entry {}", inCoreIndex.getEntry(i));
//        }
//        log.info("");
//        // this allocates a new array
//        for (DirCacheEntry e: inCoreIndex.getEntriesWithin("")) {
//            log.info("Entry {}", e);
//        }
//
//        ObjectId oid = inCoreIndex.writeTree(inserter);
//        inserter.flush();
//        log.info("written tree {}", oid.name());
//

        // =================================
//        log.info("");
//        RevCommit baseline = gr.getCommit("baseline^{}");
//        final DirCache inCoreIndex2 = DirCache.newInCore();
//        final DirCacheBuilder dcb = inCoreIndex2.builder();
//        final ObjectInserter inserter = repo.newObjectInserter();
//        final ObjectReader or = repo.newObjectReader();
//
//        final TreeWalk walk = new TreeWalk(repo);
//        walk.reset();
//        walk.setRecursive(true);
//        ObjectId treeId = ObjectId.fromString("073df825a2fdade8679a9fea3669d457c4a5be9b");
////
//        dcb.addTree(null, 0, or, treeId);
//        dcb.finish();
//
//
//
//        walk.setFilter(new MyFilter());
//        walk.addTree(treeId);
////        walk.setFilter(PathFilter.create("name/to/remove"));
//        walk.addTree(new DirCacheBuildIterator(dcb));
//
//        while (walk.next()) {
//            log.info("{}", walk.getPathString());
//            ; // do nothing on a match as we want to remove matches
//        }
//
//        dcb.finish();
//
//        for (int i = 0; i < inCoreIndex2.getEntryCount(); i++) {
//            log.info("Entry {}", inCoreIndex2.getEntry(i));
//        }
//        dcb.commit();
//        edit.commit();


        // =================================
//        log.info("");
//        final DirCache dc = DirCache.newInCore();
//        final DirCacheBuilder dcb = dc.builder();
//        final ObjectInserter inserter = repo.newObjectInserter();
//        final ObjectReader or = repo.newObjectReader();
//
//        ObjectId treeId = ObjectId.fromString("073df825a2fdade8679a9fea3669d457c4a5be9b");
//        dcb.addTree(null, 0, or, treeId);
//        dcb.finish();
//        for (int i = 0; i < dc.getEntryCount(); i++) {
//            log.info("Entry {}", dc.getEntry(i));
//        }
//
//        final DirCacheBuilder dcb2 = dc.builder();
//        final TreeWalk tw = new TreeWalk(repo);
//        tw.reset(); // drop the first empty tree, which we do not need here
//        tw.setRecursive(true);
//        tw.setFilter(PathFilterGroup.createFromStrings( "green/eggs/ham"));
//        tw.addTree(new DirCacheBuildIterator(dcb2));
//        tw.addTree(treeId);
//
//        while (tw.next()) {
//            log.info("{} {}", tw.getPathString(), tw.getObjectId(0).name());
//        }
//        dcb2.finish();
//        log.info("");
//        for (int i = 0; i < dc.getEntryCount(); i++) {
//            log.info("Entry {}", dc.getEntry(i));
//        }

    }

    static class MyFilter extends TreeFilter {
        String n = "green/eggs/ham";

        @Override
        public boolean include(TreeWalk walk) throws MissingObjectException, IncorrectObjectTypeException, IOException {
            log.info("MyFilter {}", walk.getPathString());
            return false;
//            return ( ! n.equals(walk.getPathString()));
//            return true;
        }

        @Override
        public boolean shouldBeRecursive() {
            return false;
        }

        @Override
        public TreeFilter clone() {
            return this;
        }
    }

    public static DirCacheEntry addFile(String text, String fileName) throws IOException {
        ObjectId s0OID = gr.persistBlob(text.getBytes());
        final DirCacheEntry dcEntry = new DirCacheEntry(fileName);
        dcEntry.setObjectId(s0OID);
        dcEntry.setFileMode(FileMode.REGULAR_FILE);
        return dcEntry;
    }
}