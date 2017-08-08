package edu.utexas.arlut.ciads.revdb.main;

import edu.utexas.arlut.ciads.revdb.DataView;
import edu.utexas.arlut.ciads.revdb.DataStoreBuilder;
import edu.utexas.arlut.ciads.revdb.util.ExceptionHelper;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public class RuntimeContext {
    // TODO: look this up by user?

    public static DataView getDS() {
        return get().ds;
    }

    public static RuntimeContext setDS(final DataView baseline, final String name) {
        try {
            get().ds = DataStoreBuilder.of(baseline, name);
        } catch (ExceptionHelper.DataStoreCreateAccessException e) {
            e.printStackTrace();
            log.info("Error creating dataStore {} from {}", name, baseline);
        }
        return get();
    }

    public static RuntimeContext setDS(DataView ds) {
        get().ds = ds;
        return get();
    }

    public static String getUser() {
        return get().user;
    }

    public static RuntimeContext setUser(String username) {
        get().user = username;
        return get();
    }
    public static String str() {
        return get().toString();
    }

    // =================================
    private RuntimeContext() {
    }

    private static RuntimeContext get() {
        return tlRuntimeContext.get();
    }

    private DataView ds;
    private String user;

    private static ThreadLocal<RuntimeContext> tlRuntimeContext = new ThreadLocal<RuntimeContext>() {
        @Override
        protected RuntimeContext initialValue() {
            return new RuntimeContext();
        }
    };

}
