// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.revdb.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.utexas.arlut.ciads.revdb.util.ExceptionHelper.*;
import static edu.utexas.arlut.ciads.revdb.util.ExceptionHelper.createDataStoreCreateAccessException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.utexas.arlut.ciads.revdb.DataView;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;

@Slf4j
public class DataStoreBuilderImpl {

    // create(baseline, name);
    // existing(name) -> null|DataView
    // detached(name) -> DataView
    // root() -> rootView

    public static GitRepository at(File f) throws IOException {
        try {
            return GitRepository.init(f);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Cannot create GitRepository at" + f, e);
        }
    }
    // =================================

    // =================================
    // create from startDV D.V., or return existing
    public static DataView createOrGet(final DataView startDV, final String name) throws DataStoreCreateAccessException {
        // TODO; make a branch on create
        try {
            log.info("Getting or creating '{}' from {}", name, startDV);
            return stores.get(name, new Callable<GitDataView>() {
                @Override
                public GitDataView call() throws IOException {
                    log.info("creating new DataView '{}' from {}", name, startDV);
                    return new GitDataView((GitDataView)startDV, name);
                }
            });
        } catch (ExecutionException e) {
            throw createDataStoreCreateAccessException(name, e.getCause());
        }
    }
    // =================================
    // create detached, or return existing
    public static DataView detached(final String name) throws DataStoreCreateAccessException {
        checkNotNull(name);
        DataView ds = stores.getIfPresent(name);
        if (null == ds) {
            return createOrGet(root(), name);
        }
        return ds;
    }
    // =================================
    public static GitDataView root() throws DataStoreCreateAccessException {
        GitDataView ds = existing(GitRepository.ROOT_TAG);
        if (null != ds)
            return ds;
        try {
            GitRepository gr = GitRepository.instance();
            GitDataView root = new LazyGitDataView(gr.getRoot(), GitRepository.ROOT_TAG);
            stores.put(GitRepository.ROOT_TAG, root);
            return root;
        } catch (IOException e) {
            log.error("Error creating empty repo", e);
            throw createDataStoreCreateAccessException("foo", e);
        }
    }
    // =================================
    // return existing
    public static GitDataView existing(final String name) throws DataStoreCreateAccessException {
        checkNotNull(name);
        return stores.getIfPresent(name);
    }
    // =================================
    public static void rename(String oldName, String newName) {
        GitDataView ds = stores.getIfPresent(oldName);
        if (null == ds)
            return; // TODO: throw
        stores.invalidate(newName);
        stores.put(newName, ds);
        ds.rename(newName);
    }
    // =================================
    public static Iterable<String> getBranches() {
        GitRepository gr = GitRepository.instance();
        try {
            return gr.allBranches().keySet();
        } catch (IOException e) {
            log.error("Error retrieving branches", e);
            return Collections.emptyList();
        }
    }

    private static Cache<String, GitDataView> stores = CacheBuilder.newBuilder()
                                                                   .maximumSize(25)
                                                                   .build();
}
