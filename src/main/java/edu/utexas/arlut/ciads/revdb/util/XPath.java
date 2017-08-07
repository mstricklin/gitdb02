package edu.utexas.arlut.ciads.revdb.util;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

// This is a thin wrapper around a String, but it conveys the semantics that it
@Slf4j
public class XPath {
    public static final String PATH_SEPARATOR = "/";
    public static final Joiner pathJoiner = Joiner.on(PATH_SEPARATOR);

    public static Integer extractKeyFromPath(String path) {
        int li = 1 + path.lastIndexOf('/');
        String subP = path.substring(li);
        return Integer.parseInt(subP, 16);
    }

    public static XPath of(int key) {
        return pathCache.getUnchecked(key);
    }
    public XPath(int key) {
        path = String.format("%02x/%02x/%02x/%08x",
                             (byte) ((key & 0xFF000000) >> 24),
                             (byte) ((key & 0xFF000000) >> 16),
                             (byte) ((key & 0xFF000000) >> 8),
                             key);
    }

    public XPath(String first, String... more) {
        this.path = pathJoiner.join(first, more);
    }

    public XPath(String path) {
        this.path = path;
    }

    public XPath getParent() {
        int i = path.lastIndexOf(PATH_SEPARATOR);
        String p = path.substring(0, i);
        return new XPath(p);
    }

    public Iterable<XPath> getParents() {
        List<XPath> paths = newArrayList();
        String p = path;

        for (int i = p.lastIndexOf('/'); i != -1; i = p.lastIndexOf('/')) {
            p = p.substring(0, i);
            paths.add(new XPath(p));
        }
        return paths;
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(final Object rhs) {
        if (this == rhs) return true;
        if (null == rhs) return false;
        if (getClass() != rhs.getClass()) return false;
        XPath xp = (XPath) rhs;
        return path.equals(xp.path);
    }

    public static Function<XPath, Iterable<XPath>> MAKE_PATHS
            = new Function<XPath, Iterable<XPath>>() {
        @Override
        public Iterable<XPath> apply(XPath p) {
            return p.getParents();
        }
    };

    public final String path;
    // =================================
    private static LoadingCache<Integer, XPath> pathCache
            = CacheBuilder.newBuilder()
                          .maximumSize(1000)
                          .build(
                                  new CacheLoader<Integer, XPath>() {
                                      public XPath load(Integer key) {
                                          return new XPath(key);
                                      }
                                  });
}
