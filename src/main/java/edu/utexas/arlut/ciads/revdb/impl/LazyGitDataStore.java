// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.revdb.impl;

import static edu.utexas.arlut.ciads.revdb.util.Strings.abbreviate;

import java.io.IOException;

import edu.utexas.arlut.ciads.revdb.RevDBItem;
import edu.utexas.arlut.ciads.revdb.RevDBProxyItem;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.revwalk.RevCommit;

@Slf4j
public class LazyGitDataStore extends GitDataStore {
    LazyGitDataStore(RevCommit revision, String name) throws IOException {
        super(revision, name);
    }

    private boolean primed = false;
    private void prime() {
        if (primed)
            return;
        try {
            initialLoad();
            primed = true;
        } catch (IOException e) {
            log.error("Error on initial load of", this);
        }
    }
    @Override
    public void dump() {
        prime();
        super.dump();
    }
    @Override public void commit() throws IOException {
        prime();
        super.commit();
    }
    @Override
    public RevDBItem<?> getImpl(Integer key, Class<? extends RevDBProxyItem> clazz) {
        prime();
        return super.getImpl(key, clazz);
    }
    @Override
    public RevDBItem<?> getImplForMutation(Integer key, Class<? extends RevDBProxyItem> clazz) {
        prime();
        return super.getImplForMutation(key, clazz);
    }
    @Override
    public <T> T get(Integer key, Class<? extends RevDBProxyItem> clazz) {
        prime();
        return super.get(key, clazz);
    }
    @Override
    public <T> T getForMutation(Integer key, Class<? extends RevDBProxyItem> clazz) {
        prime();
        return super.getForMutation(key, clazz);
    }
    @Override
    public Iterable<? extends RevDBItem> list() {
        prime();
        return super.list();
    }
    @Override
    public Iterable<? extends RevDBItem> list(Class<? extends RevDBItem> clazz) {
        prime();
        return super.list(clazz);
    }
    // =================================
    @Override
    public String toString() {
        return String.format("LazyGitDataStore %s from %s", name, abbreviate(revision));
    }
    // =================================

}
