// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;


import static org.junit.Assert.assertTrue;

import edu.utexas.arlut.ciads.revdb.*;
import edu.utexas.arlut.ciads.revdb.DataView;
import edu.utexas.arlut.ciads.revdb.impl.GitRepository;
import edu.utexas.arlut.ciads.revdb.main.RuntimeContext;
import edu.utexas.arlut.ciads.revdb.util.ExceptionHelper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import java.io.IOException;

// =============================================================================

@Slf4j
public class AppTest1 {

    @Rule
    public TestName name= new TestName();
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    GitRepository gr;
    DataView baseline;

    @Before
    public void setup() throws IOException, ExceptionHelper.DataStoreCreateAccessException {
        try {
            //        gr = DataStoreBuilder.at(folder);
            gr = DataStoreBuilder.at("sss.git");
            baseline = DataStoreBuilder.root();
        } catch (IOException|ExceptionHelper.DataStoreCreateAccessException e) {
            e.printStackTrace();
            log.error("Error creating revdb", e);
            throw e;
        }
    }
    @After
    public void teardown() {
        // clean up old revdb dir...
    }

    @Test
    public void testCreatedDS() throws GitAPIException {
        log.info("Created revdb {}", gr.toString());
    }
    @Test
    public void testBasicAdd() throws IOException, ExceptionHelper.DataStoreCreateAccessException {
        RuntimeContext.setDS(baseline, "Test").setUser("Wile E. Coyote");
        DataView ds = RuntimeContext.getDS();
        try (DataView.Transaction tx = ds.beginTX()) {
            for (int i = 0; i < 4; i++) {
                RevDBItem k = TestFrameA.builder(i)
                                        .s0("zero" + i)
                                        .s1("one" + i)
                                        .s2("two" + i)
                                        .build();
                ds.persist(k);
            }

            TestFrameA fa4 = ds.get(4, TestFrameA.class);
            log.info("FrameA fa4 {}", fa4);
            TestFrameA fa5 = ds.get(5, TestFrameA.class);
            log.info("FrameA fa1 {}", fa5);
            ds.dump();

            fa4.setS0("zeroB");
            ds.remove(fa5);

            ds.dump();
            tx.commit();
        }
        ds.dump();
    }
}
// =============================================================================


