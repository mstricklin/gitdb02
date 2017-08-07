package edu.utexas.arlut.ciads.repo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.utexas.arlut.ciads.repo.ExceptionHelper.createDataStoreCreateAccessException;
import static edu.utexas.arlut.ciads.repo.ExceptionHelper.createRefNotFoundException;

public class DataStoreBuilder {

    // =================================
    // create from baseline repo, or return existing
    public static DataStore of(final RevCommit baseline, final String name) throws ExceptionHelper.DataStoreCreateAccessException {
        checkNotNull(baseline);
        checkNotNull(name);

        try {
            return stores.get(name, new Callable<DataStore>() {
                @Override
                public DataStore call() throws IOException {
                    final GitRepository gr = GitRepository.instance();
                    Ref branch = gr.getBranch(name);
                    if (null == branch)
                        branch = gr.branch(baseline, name);

                    RevCommit commit = gr.getCommit(branch);
                    return new GitDataStore(commit, name);
                }
            });
        } catch (ExecutionException e) {
            throw createDataStoreCreateAccessException(name, e.getCause());
        }
    }
    // =================================
    // create detached, or return existing
    public static DataStore detached(final String name) throws ExceptionHelper.DataStoreCreateAccessException, IOException {
        checkNotNull(name);
        DataStore ds = stores.getIfPresent(name);
        if (null == ds) {
            final GitRepository gr = GitRepository.instance();
            RevCommit empty = gr.getCommit("root^{}");
            return of(empty, name);
        }
        return ds;
    }
    // =================================
    // return existing
    public static DataStore existing(final String name) throws RefNotFoundException {
        checkNotNull(name);
        DataStore ds = stores.getIfPresent(name);
        if (null == ds)
            throw createRefNotFoundException(name);
        return ds;
    }
    // =================================
    public void rename(String oldName, String newName) {
        DataStore ds = stores.getIfPresent(oldName);
        if (null == ds)
            return; // TODO: throw
        stores.invalidate(newName);
        stores.put(newName, ds);
        ds.rename(newName);
    }

    private static Cache<String, DataStore> stores = CacheBuilder.newBuilder()
                                                                 .maximumSize(25)
                                                                 .build();

}
