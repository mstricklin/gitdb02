// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.example;

import java.io.IOException;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import edu.utexas.arlut.ciads.FrameA;
import edu.utexas.arlut.ciads.repo.GitRepository;
import edu.utexas.arlut.ciads.repo.Keyed;
import edu.utexas.arlut.ciads.repo.util.TreeWalkIterable;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

@Slf4j
public class AllFiles {
    public static void main(String[] args) throws GitAPIException, IOException {

        GitRepository gr = GitRepository.init("t.git");
        RevCommit baseline = gr.getBaseline();
        ObjectDatabase odb = gr.repo().getObjectDatabase();

        log.info("baseline {}", baseline.getId());
        log.info("baseline tree {}", baseline.getTree().name());


        Splitter PATH_SPLITTER = Splitter.on('/');

        Stopwatch stopwatch = Stopwatch.createStarted();
        for (String s: gr.forAllFiles(baseline, f)) {
            log.info("{}", s);
        }
        stopwatch.stop();
        log.info("elapsed {}", stopwatch);

        stopwatch.reset().start();

        TreeWalk tw0 = new TreeWalk(gr.repo());
        tw0.addTree(baseline.getTree());
        tw0.setRecursive(true);
        while (tw0.next()) {
            log.info("{} {}", tw0.getPathString(), tw0.getObjectId(0));
            ObjectId oid = tw0.getObjectId(0);
            String path = tw0.getPathString();
            String type = Iterables.getFirst(PATH_SPLITTER.split(path), null);
            Class<? extends Keyed> typeClazz = null;

            if (null == typeClazz) {
                log.info("No known type found in {}", path);
            } else {
                log.info("type found {}", typeClazz);
                Keyed k = gr.readObject(oid, typeClazz);
                log.info("type loaded {}", k);
            }
        }
        stopwatch.stop();
        log.info("elapsed {}", stopwatch);

        stopwatch.reset().start();
        TreeWalk tw2 = new TreeWalk(gr.repo());
        tw2.addTree(baseline.getTree());
        tw2.setRecursive(true);
        TreeWalkIterable<String> twi2 = new TreeWalkIterable<>(tw2, f);
        for (String s: twi2) {
            log.info("{}", s);
        }
        stopwatch.stop();
        log.info("elapsed {}", stopwatch);

    }

    static Function<TreeWalk, String> f = new Function<TreeWalk, String>() {
        @Override
        public String apply(TreeWalk tw) {
            return tw.getPathString();
        }
    };

}
