package edu.utexas.arlut.ciads.repo;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.ObjectDirectory;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static edu.utexas.arlut.ciads.repo.StringUtil.abbreviate;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;
import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

@Slf4j
public class GitRepository {
    public static GitRepository theRepo;
    public static final String BASELINE_TAG = "baseline";
    public static final String ROOT_TAG = "root";


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
        ObjectDatabase db = repo.getObjectDatabase().newCachedDatabase();
        rdb = repo.getRefDatabase();
    }

    public RevCommit getBaseline() throws IOException {
        return getCommit(BASELINE_TAG + "^{}");
    }
    public RevCommit getEmpty() throws IOException {
        return getCommit(ROOT_TAG + "^{}");
    }

    public Map<String, Ref> allRefs() throws IOException {
        return rdb.getRefs(RefDatabase.ALL);
    }

    public Ref branch(RevCommit from, String toName) throws IOException {
        // TODO: check if branchname already exists
        updateRef(toName, from);
        return rdb.getRef(toName);
    }

    public Map<String, Ref> allBranches() throws IOException {
        return newHashMap(rdb.getRefs(Constants.R_HEADS));
    }

    public Ref getBranch(String refName) throws IOException {
        return rdb.getRef(refName);
    }

    public RevCommit getCommit(String name) throws IOException {
        ObjectId oid = getTagOID(name);
        if (null == oid)
            return null;
        return getCommit(oid);
    }
    public RevCommit getCommit(Ref r) throws IOException {
        return getCommit(r.getObjectId());
    }
    public RevCommit getCommit(ObjectId id) throws IOException {
        RevWalk revWalk = new RevWalk(repo);
        RevCommit baselineCommit = revWalk.parseCommit(id);
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

    public ObjectId commit(ObjectId parentId, ObjectId treeId) throws IOException {
        try (CloseableObjectInserter coi = getCloseableInserter()) {
            CommitBuilder commit = new CommitBuilder();
            commit.setCommitter(SYSTEM_PERSON_IDENT);
            commit.setAuthor(SYSTEM_PERSON_IDENT);
            commit.setParentIds(parentId);
            commit.setTreeId(treeId);
            return coi.insert(commit);
        }
    }

    public void updateRef(String name, ObjectId newId) throws IOException {
        RefUpdate updateRef = repo.updateRef(Constants.R_HEADS + name);
        // TODO: check on ref existence
        updateRef.setNewObjectId(newId);
        updateRef.forceUpdate();
    }

    public Repository repo() {
        return repo;
    }

    public Serializer serializer() {
        return serializer;
    }

    // TODO: parameterize?
    private static final PersonIdent SYSTEM_PERSON_IDENT = new PersonIdent("amt.system", "amt.system@arlut.utexas.edu");

    // =================================
    public ObjectId persist(IKeyed k) throws IOException {
        byte[] b = serializer.serialize(k);
        return persistBlob(b);
    }

    public ObjectId persistTree(TreeFormatter tf) throws IOException {
        try (CloseableObjectInserter coi = tlCOI.get()) {
            return coi.insert(tf);
        }
    }

    public ObjectId persistBlob(byte[] b) throws IOException {
        return persistObject(OBJ_BLOB, b);
    }

    public ObjectId persistObject(int type, byte[] b) throws IOException {
        try (CloseableObjectInserter coi = tlCOI.get()) {
            return coi.insert(type, b);
        }
    }

    public byte[] readObject(ObjectId oid) throws IOException {
        try (CloseableObjectReader cor = tlCOR.get()) {
            return cor.read(oid);
        }
    }
    public <T> T readObject(ObjectId oid, Class<?> clazz) throws IOException {
        try (CloseableObjectReader cor = tlCOR.get()) {
            log.info("read object {}:{} from repo", abbreviate(oid), clazz.getSimpleName());
            byte[] b = cor.read(oid);
            return (T)serializer.deserialize(b, clazz);
        }
    }
    // =================================
    public CloseableObjectInserter getCloseableInserter() {
        return tlCOI.get();
    }
    public ObjectReader newObjectReader() {
        return repo.newObjectReader();
    }
    public ObjectInserter newObjectInserter() {
        return repo.newObjectInserter();
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
