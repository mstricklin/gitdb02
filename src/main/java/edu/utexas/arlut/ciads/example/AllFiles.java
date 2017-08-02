// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.example;

import java.io.IOException;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import edu.utexas.arlut.ciads.FrameA;
import edu.utexas.arlut.ciads.repo.CloseableObjectReader;
import edu.utexas.arlut.ciads.repo.GitRepository;
import edu.utexas.arlut.ciads.repo.IKeyed;
import edu.utexas.arlut.ciads.repo.TypeRegistry;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

@Slf4j
public class AllFiles {
    public static void main(String[] args) throws GitAPIException, IOException {

        GitRepository gr = GitRepository.init("t.git");
        Repository repo = gr.repo();
        RevCommit baseline = gr.getBaseline();
        ObjectDatabase odb = gr.repo().getObjectDatabase();

        TypeRegistry.register(FrameA.TYPE_VALUE, FrameA.Impl.class);

        log.info("baseline {}", baseline.getId());
        log.info("baseline tree {}", baseline.getTree().name());


        Splitter PATH_SPLITTER = Splitter.on('/');

        TreeWalk tw = new TreeWalk(gr.repo());
        tw.addTree(baseline.getTree());
        tw.setRecursive(true);
        while(tw.next()) {
            log.info("{} {}", tw.getPathString(), tw.getObjectId(0));
            ObjectId oid = tw.getObjectId(0);
            String path = tw.getPathString();
            String type = Iterables.getFirst(PATH_SPLITTER.split(path), null);
            Class<? extends IKeyed> typeClazz = TypeRegistry.get(type);

            if (null == typeClazz) {
                log.info("No known type found in {}", path);
            } else {
                log.info("type found {}", typeClazz);
                IKeyed k = gr.readObject(oid, typeClazz);
                log.info("type loaded {}", k);
            }
        }
    }
}
