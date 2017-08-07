package edu.utexas.arlut.ciads.repo;

import com.google.common.base.Function;
import edu.utexas.arlut.ciads.repo.util.TreeWalkIterable;
import edu.utexas.arlut.ciads.repo.util.XPath;
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

import static com.google.common.collect.Maps.newHashMap;
import static edu.utexas.arlut.ciads.repo.EntryUtil.TO_ENTRIES;
import static edu.utexas.arlut.ciads.repo.StringUtil.abbreviate;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;
import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

@Slf4j
public class GitRepository {
    public static GitRepository theRepo;
    public static final String BASELINE_TAG = "baseline";
    public static final String ROOT_TAG = "root";

    public static GitRepository init(File f) throws GitAPIException {
        if ( ! f.isDirectory())
            throw new InvalidConfigurationException(f.getAbsolutePath() + " is not a dir");
        if (theRepo == null)
            theRepo = new GitRepository(f.getAbsolutePath());
        return theRepo;
    }
    public static GitRepository init(String location) throws GitAPIException {
        if (theRepo == null)
            theRepo = new GitRepository(location);
        return theRepo;
    }

    public static GitRepository instance() {
        return theRepo;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " at " + repo.getDirectory();
    }

    private GitRepository(final File gitDBDir) throws GitAPIException {
        Git git = Git.init().setBare(true).setDirectory(gitDBDir).call();
        repo = git.getRepository();
        ObjectDatabase db = repo.getObjectDatabase().newCachedDatabase();
        rdb = repo.getRefDatabase();
    }
    private GitRepository(final String repoPath) throws GitAPIException {
        this(new File(repoPath));
    }

    public RevCommit addEmptyCommit() throws IOException {
        TreeFormatter tf = Tree.emptyTree();
        ObjectId treeId = persist(tf);

        try (CloseableObjectInserter coi = getCloseableInserter()) {
            CommitBuilder commit = new CommitBuilder();
            commit.setCommitter(SYSTEM_PERSON_IDENT);
            commit.setAuthor(SYSTEM_PERSON_IDENT);
            commit.setTreeId(treeId);
            ObjectId commitId = coi.insert(commit);
            log.info("empty commit id {}", commitId.name());
            return getCommit(commitId);
        }
    }
    public RevCommit getCommitByTag(String tag) throws IOException {
        return getCommit(tag + "^{}");
    }
    public void addTag(String tagName, RevCommit id) {
        TagBuilder newTag = new TagBuilder();
        newTag.setTag(tagName);
        newTag.setObjectId(id);
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

    public ObjectId lookupPath(RevTree tree, String path) throws IOException {
        return repo.resolve(tree.getId().name() + ':' + path);
    }

    public ObjectId lookupPath(RevTree tree, XPath path) throws IOException {
        return repo.resolve(tree.getId().name() + ':' + path);
    }

    public ObjectId getTagOID(String tagName) throws IOException {
        // TODO: do I need a peeledOID here?
        // TODO: 'resolve' is slow...
        return repo.resolve(tagName);
//        Map<String, Ref> m = repo.getTags();
//        return repo.getTags().get(tagName);
    }

    public Ref getTag(String tagName) throws IOException {
//        return repo.resolve(tagName);
//        Map<String, Ref> m = repo.getTags();
        return repo.getTags().get(tagName);
    }

    public <T> Iterable<T> forAllFiles(RevCommit fromCommit, Function<TreeWalk, T> transformer) throws IOException {
        TreeWalk tw = new TreeWalk(repo);
        tw.addTree(fromCommit.getTree());
        tw.setRecursive(true);
        return new TreeWalkIterable<>(tw, transformer);
    }

    public Iterable<DirCacheEntry> forAllFiles(RevCommit fromCommit) throws IOException {
        TreeWalk tw = new TreeWalk(repo);
        tw.addTree(fromCommit.getTree());
        tw.setRecursive(true);
        return new TreeWalkIterable<>(tw, TO_ENTRIES);
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

    public RevCommit commitAndUpdate(String tagName, RevCommit parent, ObjectId newTree) throws IOException {
        ObjectId commitId = commit(parent.getId(), newTree);
        updateRef(tagName, commitId);
        return getCommit(tagName);
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
    public static final PersonIdent SYSTEM_PERSON_IDENT = new PersonIdent("amt.system", "amt.system@arlut.utexas.edu");

    // =================================
    public ObjectId persist(IKeyed k) throws IOException {
        byte[] b = serializer.serialize(k);
        return persistBlob(b);
    }

    public ObjectId persist(TreeFormatter tf) throws IOException {
        try (CloseableObjectInserter coi = tlCOI.get()) {
            return coi.insert(tf);
        }
    }

    public ObjectId persistBlob(byte[] b) throws IOException {
        return persist(OBJ_BLOB, b);
    }

    public ObjectId persist(int type, byte[] b) throws IOException {
        try (CloseableObjectInserter coi = tlCOI.get()) {
            return coi.insert(type, b);
        }
    }
    public ObjectId persist(DirCache dc) throws IOException {
        ObjectInserter oi = repo.newObjectInserter();
        ObjectId oid = dc.writeTree(oi);
        oi.flush();
        oi.release();
        return oid;
    }

    public byte[] readObject(ObjectId oid) throws IOException {
        try (CloseableObjectReader cor = tlCOR.get()) {
            return cor.read(oid);
        }
    }
    public <T> T readObject(ObjectId oid, Class<?> clazz) throws IOException {
        try (CloseableObjectReader cor = tlCOR.get()) {
//            log.info("read object {}:{} from gr", abbreviate(oid), clazz.getSimpleName());
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
