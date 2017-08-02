// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;

@Slf4j
public class StringUtil {
    public static void dumpMap(String fmt, final Map<?,?> m) {
        for (Map.Entry<?,?> e: m.entrySet()) {
            log.info(fmt, e.getKey(), e.getValue());
        }
    }
    public static void dumpMap(final Map<?,?> m) {
        dumpMap("{} => {}", m);
    }

    public static String abbreviate(ObjectId oid) {
        return oid.abbreviate(10).name();
    }

    public static <T> String path(IKeyed<T> keyed, String key) {
        return keyed.getType() + "/" + key;
    }
    public static <T> String path(String type, String key) {
        return type + "/" + key;
    }
    public static String path(Class<? extends Proxy> clazz, String key) {
        // TODO: what do we do if this annotation is missing?
        String type = clazz.getAnnotation(TypeValue.class).value();
        return path(type, key);
    }
}
