package edu.utexas.arlut.ciads.revdb.impl;

import com.google.common.base.Function;
import edu.utexas.arlut.ciads.revdb.*;
import edu.utexas.arlut.ciads.revdb.util.TreeWalkIterable;
import edu.utexas.arlut.ciads.revdb.util.XPath;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMapWithExpectedSize;
import static edu.utexas.arlut.ciads.revdb.util.Entries.TO_ENTRIES;
import static edu.utexas.arlut.ciads.revdb.util.ExceptionHelper.*;
import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

@Slf4j
public class GitRepository {
    public static final String BASELINE_TAG = "baseline";
    public static final String ROOT_TAG = "ROOT";
    private static final AtomicBoolean hasInited = new AtomicBoolean(false);

    public synchronized static GitRepository init(File f) throws IOException {
        if (!f.exists()) {
            f.mkdirs();
        } else if (!f.isDirectory())
            throw new IOException(f.getAbsolutePath() + " is not a dir");
        try {
            GitRepository theRepo = new GitRepository(f.getAbsolutePath());
            if (null == theRepo.getRoot()) {
                theRepo.addRootCommit();
            }
            return theRepo;
        } catch (GitAPIException e) {
            log.error("Error creating repo at {}", f, e);
            throw new IOException("Error creating repo at "+f.getAbsolutePath(), e);
        }
    }
    public synchronized static GitRepository init(String location) throws IOException {
        return init(new File(location));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " at " + repo.getDirectory();
    }

    private GitRepository(final File gitDBDir) throws GitAPIException {
        Git git = Git.init().setBare(true).setDirectory(gitDBDir).call();
        repo = git.getRepository();
        ObjectDatabase odb = repo.getObjectDatabase().newCachedDatabase();
        rdb = repo.getRefDatabase();
    }
    private GitRepository(final String repoPath) throws GitAPIException {
        this(new File(repoPath));
    }
    // =================================
    public RevCommit getRoot() throws IOException {
        return getCommitByTag(ROOT_TAG);
    }
    private RevCommit addRootCommit() throws IOException {
        TreeFormatter tf = Tree.emptyTree();
        ObjectId treeId = persist(tf);

        try (CloseableObjectInserter coi = getCloseableInserter()) {
            CommitBuilder commit = new CommitBuilder();
            commit.setCommitter(SYSTEM_PERSON_IDENT);
            commit.setAuthor(SYSTEM_PERSON_IDENT);
            commit.setTreeId(treeId);
            ObjectId commitId = coi.insert(commit);
            log.info("empty commit id {}", commitId.name());
            RevCommit rc = getCommit(commitId);
            updateRef(ROOT_TAG, commitId);
//            addTag(ROOT_TAG, rc);
            return rc;
        }
    }
    RevCommit getCommitByTag(String tag) throws IOException {
        return getCommit(tag + "^{}");
    }
    void addTag(String tagName, RevCommit id) {
        TagBuilder newTag = new TagBuilder();
        newTag.setTag(tagName);
        newTag.setObjectId(id);
        newTag.build();
    }


    Map<String, Ref> allRefs() throws IOException {
        return rdb.getRefs(RefDatabase.ALL);
    }

    Ref branch(RevCommit from, String toName) throws IOException {
        if (rdb.isNameConflicting(toName))
            throw createEntryExistsException(toName);
        updateRef(toName, from);
        return rdb.getRef(toName);
    }

    public Map<String, Ref> allBranches() throws IOException {
        return newHashMap(rdb.getRefs(Constants.R_HEADS));
    }

    public Ref getBranch(String refName) throws IOException {
        return rdb.getRef(refName);
    }

    RevCommit getCommit(String name) throws IOException {
        ObjectId oid = getTagOID(name);
        if (null == oid)
            return null;
        return getCommit(oid);
    }
    RevCommit getCommit(Ref r) throws IOException {
        return getCommit(r.getObjectId());
    }
    RevCommit getCommit(ObjectId id) throws IOException {
        RevWalk revWalk = new RevWalk(repo);
        RevCommit baselineCommit = revWalk.parseCommit(id);
        revWalk.dispose();
        return baselineCommit;
    }

    Ref getRef(String refName) throws IOException {
        return rdb.getRef(refName);
    }

    ObjectId lookupPath(RevTree tree, String path) throws IOException {
        return repo.resolve(tree.getId().name() + ':' + path);
    }

    ObjectId lookupPath(RevTree tree, XPath path) throws IOException {
        return repo.resolve(tree.getId().name() + ':' + path);
    }

    ObjectId getTagOID(String tagName) throws IOException {
        // TODO: do I need a peeledOID here?
        // TODO: 'resolve' is slow...
        return repo.resolve(tagName);
//        Map<String, Ref> m = revdb.getTags();
//        return revdb.getTags().get(tagName);
    }

    Ref getTagRef(String tagName) throws IOException {
//        return revdb.resolve(tagName);
//        Map<String, Ref> m = revdb.getTags();
        return repo.getTags().get(tagName);
    }

    <T> Iterable<T> forAllFiles(RevCommit fromCommit, Function<TreeWalk, T> transformer) throws IOException {
        TreeWalk tw = new TreeWalk(repo);
        tw.addTree(fromCommit.getTree());
        tw.setRecursive(true);
        return new TreeWalkIterable<>(tw, transformer);
    }

    Iterable<DirCacheEntry> forAllFiles(RevCommit fromCommit) throws IOException {
        TreeWalk tw = new TreeWalk(repo);
        tw.addTree(fromCommit.getTree());
        tw.setRecursive(true);
        return new TreeWalkIterable<>(tw, TO_ENTRIES);
    }

    private ObjectId commit(ObjectId parentId, ObjectId treeId) throws IOException {
        try (CloseableObjectInserter coi = getCloseableInserter()) {
            CommitBuilder commit = new CommitBuilder();
            commit.setCommitter(SYSTEM_PERSON_IDENT);
            commit.setAuthor(SYSTEM_PERSON_IDENT);
            commit.setParentIds(parentId);
            commit.setTreeId(treeId);
            return coi.insert(commit);
        }
    }

    RevCommit commitAndUpdate(String tagName, RevCommit parent, ObjectId newTree) throws IOException {
        ObjectId commitId = commit(parent.getId(), newTree);
        updateRef(tagName, commitId);
        return getCommit(tagName);
    }

    void updateRef(String name, ObjectId newId) throws IOException {
        RefUpdate updateRef = repo.updateRef(Constants.R_HEADS + name);
        // TODO: check on ref existence
        updateRef.setNewObjectId(newId);
        updateRef.forceUpdate();
    }

    Repository repo() {
        return repo;
    }

    Serializer serializer() {
        return serializer;
    }

    // TODO: parameterize?
    static final PersonIdent SYSTEM_PERSON_IDENT = new PersonIdent("revdb.system", "revdb.system@arlut.utexas.edu");

    // =================================
    ObjectId persist(RevDBItem k) throws IOException {
        byte[] b = serializer.serialize(k);
        return persistBlob(b);
    }

    ObjectId persist(TreeFormatter tf) throws IOException {
        try (CloseableObjectInserter coi = tlCOI.get()) {
            return coi.insert(tf);
        }
    }

    ObjectId persistBlob(byte[] b) throws IOException {
        return persist(OBJ_BLOB, b);
    }

    ObjectId persist(int type, byte[] b) throws IOException {
        try (CloseableObjectInserter coi = tlCOI.get()) {
            return coi.insert(type, b);
        }
    }
    ObjectId persist(DirCache dc) throws IOException {
        ObjectInserter oi = repo.newObjectInserter();
        ObjectId oid = dc.writeTree(oi);
        oi.flush();
        oi.release();
        return oid;
    }

    byte[] readObject(ObjectId oid) throws IOException {
        try (CloseableObjectReader cor = tlCOR.get()) {
            return cor.read(oid);
        }
    }
    <T> T readObject(ObjectId oid, Class<?> clazz) throws IOException {
        byte[] b = readObject(oid);
        return (T)serializer.deserialize(b, clazz);
    }
    // =================================
    CloseableObjectInserter getCloseableInserter() {
        return tlCOI.get();
    }
    ObjectReader newObjectReader() {
        return repo.newObjectReader();
    }
    ObjectInserter newObjectInserter() {
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
