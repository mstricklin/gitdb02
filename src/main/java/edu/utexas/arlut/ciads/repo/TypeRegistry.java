package edu.utexas.arlut.ciads.repo;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import edu.utexas.arlut.ciads.Proxied;

public class TypeRegistry {
    public static void register(String type, Class<? extends Proxied> clazz) {
        registry.put(type, clazz);

    }
    public static Class<? extends IKeyed> get(String type) {
        return registry.get(type);
    }

    static Map<String, Class<? extends IKeyed>> registry = newHashMap();
}
