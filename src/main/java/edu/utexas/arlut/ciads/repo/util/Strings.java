// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;

import static com.google.common.collect.Lists.newArrayList;

@Slf4j
public class Strings {
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

}
