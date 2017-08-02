package edu.utexas.arlut.ciads.repo;

import static edu.utexas.arlut.ciads.repo.StringUtil.abbreviate;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.revwalk.RevCommit;

@Slf4j
@ToString
public class RuntimeContext {
    // TODO: look this up by user?

    public static RuntimeContext tlInstance() {
        return tlRuntimeContext.get();
    }

    public DataStore getDS() {
        return ds;
    }
    public RuntimeContext setDS(final RevCommit baseline, final String name) {
        try {
            ds = DataStore.of(baseline, name);
        } catch (ExceptionHelper.DataStoreCreateAccessException e) {
            e.printStackTrace();
            log.info("Error creating dataStore {} in context {}", name, abbreviate(baseline));
        }
        return this;
    }
    public RuntimeContext setDS(DataStore ds) {
        this.ds = ds;
        return this;
    }
    public String getUser() {
        return user;
    }
    public RuntimeContext setUser(String username) {
        user = username;
        return this;
    }

    // =================================
    private RuntimeContext() {
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
