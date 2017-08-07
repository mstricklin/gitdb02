package edu.utexas.arlut.ciads;

import static edu.utexas.arlut.ciads.repo.util.Strings.abbreviate;

import edu.utexas.arlut.ciads.repo.DataStore;
import edu.utexas.arlut.ciads.repo.DataStoreBuilder;
import edu.utexas.arlut.ciads.repo.ExceptionHelper;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.revwalk.RevCommit;

@Slf4j
@ToString
public class RuntimeContext {
    // TODO: look this up by user?

    public static DataStore getDS() {
        return get().ds;
    }

    public static RuntimeContext setDS(final RevCommit baseline, final String name) {
        try {
            get().ds = DataStoreBuilder.of(baseline, name);
        } catch (ExceptionHelper.DataStoreCreateAccessException e) {
            e.printStackTrace();
            log.info("Error creating dataStore {} in context {}", name, abbreviate(baseline));
        }
        return get();
    }

    public static RuntimeContext setDS(DataStore ds) {
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

    private DataStore ds;
    private String user;

    private static ThreadLocal<RuntimeContext> tlRuntimeContext = new ThreadLocal<RuntimeContext>() {
        @Override
        protected RuntimeContext initialValue() {
            return new RuntimeContext();
        }
    };

}
