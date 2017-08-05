// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;

import static com.google.common.collect.Lists.newArrayList;

@Slf4j
public class StringUtil {
    public static void dumpMap(String fmt, final Map<?, ?> m) {
        for (Map.Entry<?, ?> e : m.entrySet()) {
            log.info(fmt, e.getKey(), e.getValue());
        }
    }

    public static void dumpMap(final Map<?, ?> m) {
        dumpMap("{} => {}", m);
    }

    // =================================
    public static String abbreviate(ObjectId oid) {
        return oid.abbreviate(10).name();
    }

    // =================================
    public static String path(int key) {
        String s = String.format("%02x/%02x/%02x/%08x",
                                 (byte) ((key & 0xFF000000) >> 24),
                                 (byte) ((key & 0xFF000000) >> 16),
                                 (byte) ((key & 0xFF000000) >> 8),
                                 key
        );
        return Paths.get(s).toString();
    }

    // =================================
    public static Function<String, Iterable<String>> MAKE_PATHS
            = new Function<String, Iterable<String>>() {
        @Override
        public Iterable<String> apply(String s) {
            return paths(s);
        }
    };

    public static Iterable<String> paths(final String path) {
        String p = path;
        List<String> l = newArrayList();

        for (int i = p.lastIndexOf('/'); i != -1; i = p.lastIndexOf('/')) {
            p = p.substring(0, i);
            l.add(p);
        }
        return l;
    }

    // =================================
    public static Path path2(int key) {
        return Paths.get(String.format("%02x/%02x/%02x/%08x",
                                       (byte) ((key & 0xFF000000) >> 24),
                                       (byte) ((key & 0xFF000000) >> 16),
                                       (byte) ((key & 0xFF000000) >> 8),
                                       key
        ));
    }

    public static Function<Path, Iterable<Path>> MAKE_PATHS2
            = new Function<Path, Iterable<Path>>() {
        @Override
        public Iterable<Path> apply(Path p) {
            return parentPaths(p);
        }
    };

    static Iterable<Path> parentPaths(Path path) {
        List<Path> paths = newArrayList();
        Path p = path;
        while (null != (p = p.getParent())) {
            paths.add(p);
        }
        return paths;
    }
    // =================================
}
