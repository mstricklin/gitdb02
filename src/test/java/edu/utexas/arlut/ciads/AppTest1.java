// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;


import static org.junit.Assert.assertTrue;

import edu.utexas.arlut.ciads.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;

// =============================================================================

@Slf4j
public class AppTest1 {

    @Rule
    public TestName name= new TestName();
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    GitRepository gr;
    RevCommit emptyCommit;

    @Before
    public void setup() throws GitAPIException, IOException {
        File f = folder.newFolder(name.getMethodName() + ".git");
//        gr = GitRepository.init(f);
        gr = GitRepository.init("sss.t");
        emptyCommit = gr.addEmptyCommit();
        gr.addTag("empty", emptyCommit);
    }

    @Test
    public void testCreatedDS() throws GitAPIException {
        log.info("Created ds {}", gr.toString());
    }
    @Test
    public void testBasicAdd() throws IOException, ExceptionHelper.DataStoreCreateAccessException {
        assertTrue(true);
        DataStore ds = DataStoreBuilder.of(emptyCommit, "Test");

    }
}
// =============================================================================


