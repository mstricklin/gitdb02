package edu.utexas.arlut.ciads.example;

import edu.utexas.arlut.ciads.repo.GitRepository;
import edu.utexas.arlut.ciads.repo.DataStore;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;
import java.util.Map;

import static edu.utexas.arlut.ciads.repo.util.Strings.dumpMap;

@Slf4j
public class TestTags {
    static DataStore ds;

    public static void main(String[] args) throws GitAPIException, IOException {
        GitRepository gr = GitRepository.init("t.git");

        Ref baselineTag = gr.getRef("baseline");
        log.info("baselineTag {}", baselineTag);
        log.info("baselineTag.getObjectId {}", baselineTag.getObjectId());
        log.info("baselineTag.isPeeled {}", baselineTag.isPeeled());
        log.info("baselineTag.getPeeledObjectId {}", baselineTag.getPeeledObjectId());
        log.info("baselineTag.getLeaf {}", baselineTag.getLeaf());
        log.info("");

        Ref masterTag = gr.getRef("master");
        log.info("masterTag {}", masterTag);
        log.info("masterTag.getObjectId {}", masterTag.getObjectId());
        log.info("masterTag.isPeeled {}", masterTag.isPeeled());
        log.info("masterTag.getPeeledObjectId {}", masterTag.getPeeledObjectId());
        log.info("masterTag.getLeaf {}", masterTag.getLeaf());
        log.info("");

        Ref baselineTagOnly = gr.getTag("baselineTagOnly");
        log.info("baselineTagOnly {}", baselineTagOnly);
        log.info("baselineTagOnly.getObjectId {}", baselineTagOnly.getObjectId());
        log.info("baselineTagOnly.isPeeled {}", baselineTagOnly.isPeeled());
        log.info("baselineTagOnly.getPeeledObjectId {}", baselineTagOnly.getPeeledObjectId());
        log.info("baselineTagOnly.getLeaf {}", baselineTagOnly.getLeaf());
        log.info("");

        Map<String, Ref> branches = gr.allBranches();
        dumpMap(" branch: {} => {}", branches);
        for (Map.Entry<String, Ref> e : branches.entrySet()) {

        }

        Ref masterRef = gr.getBranch("master");
        Repository r = gr.repo();

        RevWalk walk = new RevWalk(gr.repo());
        Ref head = r.getRef("HEAD");
        RevCommit commit = walk.parseCommit(masterRef.getObjectId());
        System.out.println("Commit: " + commit);

        // a commit points to a tree
//        RevTree tree = walk.parseTree(commit.getTree().getId());
        RevTree tree = commit.getTree();

        System.out.println("Found Tree: " + tree);

        walk.dispose();

        // treewalk needs a RevTree
        // need a RevTree from a Ref...?
//        Ref head = repository.exactRef("refs/heads/master");
    }
}