// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;


import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;

import java.util.List;

import com.google.common.collect.Iterables;
import edu.utexas.arlut.ciads.repo.DataStore;
import edu.utexas.arlut.ciads.repo.Keyed;
import edu.utexas.arlut.ciads.repo.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;

// =============================================================================

@Slf4j
public class AppTest {
    @Test
    public void testCreatedDS() {
        DataStore ds = new DataStore<Integer>(null);
        App.MutableK k1 = new App.MutableK(1, "one0", "one1", "one2");
        App.MutableK k2 = new App.MutableK(1, "two0", "two1", "two2");

        try(Transaction tx = ds.beginTX()) {
            tx.add(1, k1);
            tx.add(2, k2);
            tx.dump();
            ds.dump();
            tx.commit();
        }
        log.info("");
        Iterable<Keyed> list = ds.list();
        for (Keyed k: list) {
            log.info("{}", k);
        }

        List<Keyed> l = newArrayList(list);

//        assertThat(l, contains(k1));
//        assertThat(l. hasI);
        assertThat(l, not(IsEmptyCollection.empty()));

        App.ImmutableK ik1 = new App.ImmutableK(k1);
        assertTrue(Iterables.contains(l, ik1));
    }
    // add, delete, re-add
    //
    @Test
    public void testBasicAddVertex() {
        assertTrue(true);

    }
}
// =============================================================================


