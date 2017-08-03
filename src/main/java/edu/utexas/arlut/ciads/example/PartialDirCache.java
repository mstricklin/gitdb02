package edu.utexas.arlut.ciads.example;

import edu.utexas.arlut.ciads.repo.GitRepository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;

@Slf4j
public class PartialDirCache {

    public static void main(String[] args) throws GitAPIException, IOException {
        GitRepository gr = new GitRepository("t.git");
        Repository repo = gr.repo();

        ObjectId commitId = ObjectId.fromString("0ba10a56034aba6082264241bbbfbb96c8078d89");

        RevWalk rw = new RevWalk(repo);
        RevCommit rc = rw.parseCommit(commitId);
        rw.dispose();
        ObjectId commitTree = rc.getTree();

//        TreeWalk tw = new TreeWalk(repo);
//        tw.addTree(commitTree);
//        tw.setRecursive(true);
//        while (tw.next()) {
//            log.info("{} {}", tw.getPathString(), tw.getObjectId(0));
//        }
//        tw.release();

        ObjectId dir2aOId = repo.resolve(commitTree.name()+":0/1/2a");
        log.info("0/1/2a {}", dir2aOId.name());
        ObjectId dir2cOId = repo.resolve(commitTree.name()+":0/1/2c");
        log.info("0/1/2c {}", dir2cOId.name());


        final DirCache inCoreIndex = DirCache.newInCore();
        final DirCacheBuilder dcBuilder = inCoreIndex.builder();
        final ObjectReader or = gr.newObjectReader();
        dcBuilder.addTree(null, 0, or, dir2aOId);
        dcBuilder.finish();
        for (int i = 0; i < inCoreIndex.getEntryCount(); i++) {
            log.info("Entry {}", inCoreIndex.getEntry(i));
        }
    }
}