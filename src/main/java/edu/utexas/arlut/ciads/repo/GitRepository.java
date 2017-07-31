package edu.utexas.arlut.ciads.repo;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.ObjectDirectory;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevTree;

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

    public Ref getRef(String refName) throws IOException {
        return rdb.getRef(refName);
    }

    public Ref getTag(String refName) throws IOException {
        Ref r = repo.getTags().get(refName);
        return repo.getTags().get(refName);
    }

    public Repository repo() {
        return repo;
    }

    public Serializer serializer() {
        return serializer;
    }

    public ObjectId insert(Keyed k) throws IOException {
        byte[] b = serializer.serialize(k);
        return insertBlob(b);
    }

    public ObjectId insertBlob(byte[] b) throws IOException {
        return insertObject(OBJ_BLOB, b);
    }

    public ObjectId insertObject(int type, byte[] b) throws IOException {
        try (CloseableObjectInserter coi = tlCOI.get()) {
            ObjectId oid = coi.insert(type, b);
            log.debug("insertObject {}", oid);
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
