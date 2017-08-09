package edu.utexas.arlut.ciads.revdb.main;

import edu.utexas.arlut.ciads.revdb.*;
import edu.utexas.arlut.ciads.revdb.DataView;
import edu.utexas.arlut.ciads.revdb.impl.GitRepository;
import edu.utexas.arlut.ciads.revdb.util.ExceptionHelper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static edu.utexas.arlut.ciads.revdb.util.Strings.dumpMap;

@Slf4j
public class App {
    public static void main(String[] args) throws GitAPIException, IOException, ExceptionHelper.DataStoreCreateAccessException {
        GitRepository gr = DataStoreBuilder.at("t.git");

        RevCommit root = gr.getRoot();
        DataView baseline = DataStoreBuilder.root();

        RuntimeContext.setDS(baseline, "shazam").setUser("Wile E. Coyote");
        log.info("RuntimeContext {}", RuntimeContext.str());
        DataView ds = RuntimeContext.getDS();

        // TODO: check on baseline existence, and prime if not

        try (DataView.Transaction tx = ds.beginTX()) {
            for (int i = 0; i < 4; i++) {
                RevDBItem k = FrameA.builder(i)
                                    .s0("zero" + i)
                                    .s1("one" + i)
                                    .s2("two" + i)
                                    .build();
                ds.persist(k);
            }


            FrameA fa4 = ds.get(4, FrameA.class);
            log.info("FrameA fa4 {}", fa4);
            FrameA fa5 = ds.get(5, FrameA.class);
            log.info("FrameA fa1 {}", fa5);
            ds.dump();

            fa4.setS0("zeroB");
            ds.remove(fa5);

            ds.dump();
            tx.commit();
        }
        ds.dump();
        FrameA fa2b = ds.get(2, FrameA.class);
        log.info("s0 {}", fa2b.getS0());
        log.info("\n");

        RuntimeContext.setDS(ds, "shazam2").setUser("Wile E. Coyote");
        log.info("RuntimeContext {}", RuntimeContext.str());
        ds = RuntimeContext.getDS();

        try (DataView.Transaction tx = ds.beginTX()) {
            for (int i = 0; i < 3; i++) {
                RevDBItem k = FrameA.builder(i)
                                    .s0("zero" + i)
                                    .s1("one" + i)
                                    .s2("two" + i)
                                    .build();
                ds.persist(k);
            }
            ds.dump();
            tx.commit();
        }
        ds.dump();
//        ds.get()


//        log.info("{}", paths("01/02/03/K.04"));


//        ObjectId treeId = ObjectId.fromString("434943a8265129a744745e5d12fa2625a784b283");
//        Tree t = new Tree(gr.revdb(), treeId, null);

//        log.info("");
//        try (
//                Transaction tx = ds.beginTX())
//
//        {
//            MutableK k1 = new MutableK(5, "five0", "five1", "five2");
//            tx.persist(5, k1);
//            MutableK k2 = new MutableK(6, "six0", "six1", "six2");
//            tx.persist(6, k2);
//            tx.remove(3);
//            tx.dump();
//            ds.dump();
//            tx.commit();
//        }
//        ds.dump();

//        try (Transaction tx0 = ds.beginTX()) {
//            MutableK k1 = new MutableK(1, "one0", "one1", "one2");
//            tx0.persist(1L, k1);
//            tx0.persist(2L, new MutableK(2, "two0", "two1", "two2"));
//            log.info("DS 2L {}", ds.get(2L));
//            log.info("TX0 2L {}", tx0.get(2L));
//
//            tx0.dump();
//            log.info("Mutate...");
//            k1.s0 = "one0A";
//            tx0.dump();
////        tx.commit();
//            ds.commit();
//            log.info("DS 2L {}", ds.get(2L));
//            ds.dump();
//            a();
//        }
//
//        ds.dump();
//
//        try (Transaction tx1 = ds.beginTX()) {
//            tx1.remove(1L);
//            tx1.dump();
//            tx1.commit();
//        }
//        ds.dump();
//        log.info("");
////        K k2 = ds.get(2L);
////        log.info("2L {}", k2);
//        try (Transaction tx2 = ds.beginTX()) {
//            log.info("2L {}", ds.getForMutation(2));
//        }

//        String s0 = "K/22";
//        log.info("path: {}", s0);
//        int i = s0.lastIndexOf('/');
//        log.info("last index {}", i);
//        String s1 = s0.substring(0, i);
//        log.info("subStr {}", s1);
//
//
//        List<String> ls = newArrayList(Splitter.on('/').split(s0));
//        log.info("Split: {}", ls);


        ds.shutdown();
    }

    // =================================
    static List<String> paths(final String path) {
        String p = path;
        List<String> l = newArrayList();

        for (int i = p.lastIndexOf('/'); i != -1; i = p.lastIndexOf('/')) {
            p = p.substring(0, i);
            l.add(p);
            log.info("index {} {}", i, p);
        }
        return l;
    }

    // =================================

}
