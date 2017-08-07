// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.revdb.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.utexas.arlut.ciads.revdb.util.ExceptionHelper.*;
import static edu.utexas.arlut.ciads.revdb.util.ExceptionHelper.createDataStoreCreateAccessException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.utexas.arlut.ciads.revdb.DataStore;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;

@Slf4j
public class DataStoreBuilderImpl {

    public static GitRepository at(File f) throws IOException {
        try {
            return GitRepository.init(f);
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
            throw new IOException("Cannot create GitRepository at" + f, e);
        }
    }
    // =================================
    // create from startingPoint DS, or return existing
    public static DataStore of(final DataStore startDS, final String name) throws DataStoreCreateAccessException {
        try {
            return stores.get(name, new Callable<GitDataStore>() {
                @Override
                public GitDataStore call() throws IOException {
                    return new GitDataStore((GitDataStore)startDS, name);
                }
            });
        } catch (ExecutionException e) {
            throw createDataStoreCreateAccessException(name, e.getCause());
        }
    }
    // =================================
    // create detached, or return existing
    public static DataStore detached(final String name) throws DataStoreCreateAccessException {
        checkNotNull(name);
        DataStore ds = stores.getIfPresent(name);
        if (null == ds) {
            final GitRepository gr = GitRepository.instance();
            return of(root(), name);
        }
        return ds;
    }
    // =================================
    public static GitDataStore root() throws DataStoreCreateAccessException {
        GitDataStore ds = existing(GitRepository.ROOT_TAG);
        if (null != ds)
            return ds;
        try {
            GitRepository gr = GitRepository.instance();
            GitDataStore root = new LazyGitDataStore(gr.getRoot(), GitRepository.ROOT_TAG);
            stores.put(GitRepository.ROOT_TAG, root);
            return root;
        } catch (IOException e) {
            log.error("Error creating empty repo", e);
            throw createDataStoreCreateAccessException("foo", e);
        }
    }
    // =================================
    // return existing
    public static GitDataStore existing(final String name) throws DataStoreCreateAccessException {
        checkNotNull(name);
        return stores.getIfPresent(name);
    }
    // =================================
    public static void rename(String oldName, String newName) {
        GitDataStore ds = stores.getIfPresent(oldName);
        if (null == ds)
            return; // TODO: throw
        stores.invalidate(newName);
        stores.put(newName, ds);
        ds.rename(newName);
    }

    private static Cache<String, GitDataStore> stores = CacheBuilder.newBuilder()
                                                                    .maximumSize(25)
                                                                    .build();
}
