// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.revdb.impl;

import static edu.utexas.arlut.ciads.revdb.util.Strings.abbreviate;

import java.io.IOException;

import edu.utexas.arlut.ciads.revdb.RevDBItem;
import edu.utexas.arlut.ciads.revdb.RevDBProxyItem;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.revwalk.RevCommit;

@Slf4j
public class LazyGitDataView extends GitDataView {
    LazyGitDataView(RevCommit revision, String name) throws IOException {
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
    public RevDBItem<?> getImpl(Integer id, Class<? extends RevDBProxyItem> clazz) {
        prime();
        return super.getImpl(id, clazz);
    }
    @Override
    public RevDBItem<?> getImplForMutation(Integer id, Class<? extends RevDBProxyItem> clazz) {
        prime();
        return super.getImplForMutation(id, clazz);
    }
    @Override
    public <T> T get(Integer id, Class<? extends RevDBProxyItem> clazz) {
        prime();
        return super.get(id, clazz);
    }
    @Override
    public <T> T getForMutation(Integer id, Class<? extends RevDBProxyItem> clazz) {
        prime();
        return super.getForMutation(id, clazz);
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
        return String.format("LazyGitDataView %s from %s", name, abbreviate(revision));
    }
    // =================================

}
