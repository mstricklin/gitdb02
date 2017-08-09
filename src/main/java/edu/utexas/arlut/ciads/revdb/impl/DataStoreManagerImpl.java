package edu.utexas.arlut.ciads.revdb.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.utexas.arlut.ciads.revdb.DataStoreManager;
import edu.utexas.arlut.ciads.revdb.DataView;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class DataStoreManagerImpl implements DataStoreManager {
    DataStoreManagerImpl(File repoLocation) throws IOException {
        gr = GitRepository.init(repoLocation);
    }

    // get existing, or create from root
    // get existing, or create from a starting point
    // get root
    public DataView getOrCreate(final DataView startingView, final String name) throws IOException {
        checkNotNull(name);
        DataView dv = stores.getIfPresent(name);
        if (null != dv)
            return dv;
        return new GitDataView((GitDataView)startingView, name);
    }
    public DataView getDV(final String name) {
        checkNotNull(name);
        return stores.getIfPresent(name);
    }
    // =================================
    private final GitRepository gr;
    private static Cache<String, GitDataView> stores = CacheBuilder.newBuilder()
                                                                   .maximumSize(25)
                                                                   .build();
}
