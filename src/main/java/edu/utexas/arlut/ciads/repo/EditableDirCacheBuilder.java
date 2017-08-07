package edu.utexas.arlut.ciads.repo;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;

public class EditableDirCacheBuilder  {
    EditableDirCacheBuilder(final DirCache dc, final int ecnt) {
        cache = dc;
        entries = new DirCacheEntry[ecnt];
    }
    public DirCache getDirCache() {
        return cache;
    }

    protected DirCache cache;

    // should this be a container?
    protected DirCacheEntry[] entries;
    protected int entryCnt;
}
