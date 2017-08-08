package edu.utexas.arlut.ciads.revdb;

import static com.google.common.base.Preconditions.checkNotNull;

import edu.utexas.arlut.ciads.revdb.impl.DataStoreBuilderImpl;
import edu.utexas.arlut.ciads.revdb.impl.GitRepository;
import edu.utexas.arlut.ciads.revdb.util.ExceptionHelper;

import java.io.File;
import java.io.IOException;

public class DataStoreBuilder {

    public static GitRepository at(File f) throws IOException {
        return DataStoreBuilderImpl.at(f);
    }
    public static GitRepository at(String location) throws IOException {
        return at(new File(location));
    }
    // =================================
    // create from baseline revdb, or return existing
    public static DataView of(final DataView startingPoint, final String name) throws ExceptionHelper.DataStoreCreateAccessException {
        checkNotNull(startingPoint);
        checkNotNull(name);
        return DataStoreBuilderImpl.of(startingPoint, name);
    }
    // =================================
    // create detached, or return existing
    public static DataView detached(final String name) throws ExceptionHelper.DataStoreCreateAccessException {
        checkNotNull(name);
        return DataStoreBuilderImpl.detached(name);
    }
    // =================================
    // get the empty root
    public static DataView root() throws ExceptionHelper.DataStoreCreateAccessException {
        return DataStoreBuilderImpl.root();
    }
    // =================================
    // return existing
    public static DataView existing(final String name) throws ExceptionHelper.DataStoreCreateAccessException {
        checkNotNull(name);
        return DataStoreBuilderImpl.existing(name);
    }
    // =================================
    public static void rename(String oldName, String newName) {
        checkNotNull(oldName);
        checkNotNull(newName);
        DataStoreBuilderImpl.rename(oldName, newName);
    }
    // =================================
    public static Iterable<String> getBranches() {
        return DataStoreBuilderImpl.getBranches();
    }
}
