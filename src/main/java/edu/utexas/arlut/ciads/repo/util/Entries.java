package edu.utexas.arlut.ciads.repo.util;

import com.google.common.base.Function;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;

public class Entries {
    public static DirCacheEntry toEntry(final TreeWalk tw) {
        final DirCacheEntry e = new DirCacheEntry(tw.getRawPath());
        final AbstractTreeIterator i;

        i = tw.getTree(0, AbstractTreeIterator.class);
        e.setFileMode(tw.getFileMode(0));
        e.setObjectIdFromRaw(i.idBuffer(), i.idOffset());
        return e;
    }

    public static DirCacheEntry toEntry(final String path, final ObjectId id) {
        final DirCacheEntry e = new DirCacheEntry(path);
        e.setFileMode(FileMode.REGULAR_FILE);
        e.setObjectId(id);
        e.setLastModified(System.currentTimeMillis());
        return e;
    }

    public static Function<TreeWalk, DirCacheEntry> TO_ENTRIES = new Function<TreeWalk, DirCacheEntry>() {
        @Override
        public DirCacheEntry apply(TreeWalk tw) {
            return toEntry(tw);
        }
    };
}
