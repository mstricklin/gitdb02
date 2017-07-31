// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;


import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.Matchers.contains;

import edu.utexas.arlut.ciads.repo.DataStore;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;

// =============================================================================

@Slf4j
public class AppTest {
    @Test
    public void testCreatedDS() throws GitAPIException {
        DataStore ds = DataStore.of(null);
//        App.MutableK k1 = new App.MutableK(1, "one0", "one1", "one2");
//        App.MutableK k2 = new App.MutableK(1, "two0", "two1", "two2");
//
//        try(DataStore.Transaction tx = ds.beginTX()) {
//            ds.add(1, k1);
//            ds.add(2, k2);
//            ds.dump();
//            tx.commit();
//        }
        log.info("");
//        Iterable<Integer> list = ds.list();
//        for (Keyed k: list) {
//            log.info("{}", k);
//        }
//
//        List<Keyed> l = newArrayList(list);

//        assertThat(l, contains(k1));
//        assertThat(l. hasI);
//        assertThat(l, not(IsEmptyCollection.empty()));
//
//        App.ImmutableK ik1 = new App.ImmutableK(k1);
//        assertTrue(Iterables.contains(l, ik1));
    }
    // add, delete, re-add
    //
    @Test
    public void testBasicAddVertex() {
        assertTrue(true);

    }
}
// =============================================================================


