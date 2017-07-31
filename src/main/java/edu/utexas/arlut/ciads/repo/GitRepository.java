package edu.utexas.arlut.ciads.repo;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;
import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

@Slf4j
public class GitRepository {
    public static GitRepository theRepo;

    public static GitRepository init(String location) throws GitAPIException {
        if (theRepo == null)
            theRepo = new GitRepository(location);
        return theRepo;
    }
    public static GitRepository instance() {
        return theRepo;
    }


    public GitRepository(final String repoPath) throws GitAPIException {
        File gitDBDir = new File(repoPath);
        Git git = Git.init().setBare(true).setDirectory(gitDBDir).call();
        repo = git.getRepository();
        ObjectDatabase db = repo.getObjectDatabase();
        rdb = repo.getRefDatabase();
    }

    public Ref getHead() {
        try {
            Ref head = repo.getRef("HEAD");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Ref> allRefs() {
        try {
            return rdb.getRefs(RefDatabase.ALL);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        getRefs(Constants.R_HEADS));
//        return repo.getRefDatabase().getRefs(prefix).values();
        return Collections.emptyMap();
    }

    public Map<String, Ref> allBranches() {
        Map<String, Ref> m = newHashMap();
        Collection<Ref> refs = new ArrayList<Ref>();
//        refs.addAll(getRefs(Constants.R_HEADS));
        try {
            return newHashMap(repo.getRefDatabase().getRefs(Constants.R_HEADS));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    public Ref getBranch(String refName) throws IOException {
        return rdb.getRef(refName);
    }

    public RevCommit getCommit(String name) throws IOException {
        ObjectId oid = getTagOID(name);
        RevWalk revWalk = new RevWalk(repo);
        RevCommit baselineCommit = revWalk.parseCommit(oid);
        revWalk.dispose();
        return baselineCommit;
    }
    public Ref getRef(String refName) throws IOException {
        return rdb.getRef(refName);
    }

    public ObjectId getTagOID(String tagName) throws IOException {
        // TODO: do I need a peeledOID here?
        // 'resolve' is slow...
        return repo.resolve(tagName);
//        Map<String, Ref> m = repo.getTags();
//        return repo.getTags().get(tagName);
    }

    public Ref getTag(String tagName) throws IOException {
//        return repo.resolve(tagName);
//        Map<String, Ref> m = repo.getTags();
        return repo.getTags().get(tagName);
    }

    public Repository repo() {
        return repo;
    }

    public Serializer serializer() {
        return serializer;
    }

    public ObjectId persist(IKeyed k) throws IOException {
        byte[] b = serializer.serialize(k);
        return persistBlob(b);
    }

    private static final PersonIdent SYSTEM_PERSON_IDENT = new PersonIdent("amt.system", "amt.system@arlut.utexas.edu");
    public static PersonIdent systemIdent() {
        return SYSTEM_PERSON_IDENT;
    }

    public CloseableObjectInserter getInserter() {
        return tlCOI.get();
    }
    public ObjectId persistTree(TreeFormatter tf) throws IOException {
        try (CloseableObjectInserter coi = tlCOI.get()) {
            ObjectId oid = coi.insert(tf);
            log.debug("persistObject {}", oid);
            return oid;
        }
    }

    public ObjectId persistBlob(byte[] b) throws IOException {
        return persistObject(OBJ_BLOB, b);
    }

    public ObjectId persistObject(int type, byte[] b) throws IOException {
        try (CloseableObjectInserter coi = tlCOI.get()) {
            ObjectId oid = coi.insert(type, b);
            log.debug("persistObject {}", oid);
            return oid;
        }
    }

    public byte[] readObject(ObjectId oid) throws IOException {
        try (CloseableObjectReader cor = tlCOR.get()) {
            return cor.read(oid);
        }
    }


    // =================================
    private final Repository repo;
    private final Serializer serializer = JSONSerializer.of();
    final RefDatabase rdb;

    private ThreadLocal<CloseableObjectInserter> tlCOI = new ThreadLocal<CloseableObjectInserter>() {
        @Override
        protected CloseableObjectInserter initialValue() {
            return CloseableObjectInserter.of(repo);
        }
    };
    private ThreadLocal<CloseableObjectReader> tlCOR = new ThreadLocal<CloseableObjectReader>() {
        @Override
        protected CloseableObjectReader initialValue() {
            return CloseableObjectReader.of(repo);
        }
    };
}
