// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

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
}
