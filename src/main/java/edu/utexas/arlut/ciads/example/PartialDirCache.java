package edu.utexas.arlut.ciads.example;

import edu.utexas.arlut.ciads.repo.GitRepository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;

@Slf4j
public class PartialDirCache {

    public static void main(String[] args) throws GitAPIException, IOException {
        GitRepository gr = GitRepository.init("t.git");
        Repository repo = gr.repo();

        ObjectId commitId = ObjectId.fromString("9ddf19eff147a109ec74b28269623015e75022bd");

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

        ObjectId dir2aOID = repo.resolve(commitTree.name() + ":0/1/2a");
        log.info("0/1/2a {}", dir2aOID.name());
        ObjectId dir2cOID = repo.resolve(commitTree.name() + ":0/1/2c");
        log.info("0/1/2c {}", dir2cOID.name());
        ObjectId txt2aOID = repo.resolve(commitTree.name() + ":0/1/2a/2a.txt");
        log.info("0/1/2a/2a.txt {}", txt2aOID.name());
        ObjectId txt2cOID = repo.resolve(commitTree.name() + ":0/1/2c/2c.txt");
        log.info("0/1/2c/2c.txt {}", txt2cOID.name());

        ObjectId txt2cOIDb = gr.persistBlob("2cA".getBytes());

        log.info("");
        final DirCache inCoreIndex = DirCache.newInCore();
        final ObjectReader or = gr.newObjectReader();
        final ObjectInserter inserter = repo.newObjectInserter();

        final DirCacheBuilder dcBuilder0 = inCoreIndex.builder();
//        dcBuilder0.add(makeDCE("0/1/2a/2a.txt", txt2aOID));
        dcBuilder0.add(makeDCE("0/1/2c/2c.txt", txt2cOIDb));

        dcBuilder0.finish();

        for (int i = 0; i < inCoreIndex.getEntryCount(); i++) {
            log.info("Entry {}", inCoreIndex.getEntry(i));
        }
        ObjectId oid = inCoreIndex.writeTree(inserter);
        inserter.flush();
        log.info("written changes to tree {}", oid);

    }

    public static DirCacheEntry makeDCE(String path, ObjectId oid) {
        DirCacheEntry dce = new DirCacheEntry(path);
        dce.setObjectId(oid);
        dce.setFileMode(FileMode.REGULAR_FILE);
        return dce;
    }
}