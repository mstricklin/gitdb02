package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.repo.DataStore;
import edu.utexas.arlut.ciads.repo.GitRepository;
import edu.utexas.arlut.ciads.repo.Keyed;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static edu.utexas.arlut.ciads.repo.StringUtil.dumpMap;

@Slf4j
public class App {
/*
1. Set up baseline
2. Try DirCache
3. Non-trivial directory depth
4. get() and put()
 */
    static DataStore ds;
    public static final String BASELINE_TAG = "baseline";

    static Map<String, RevCommit> baselines = newHashMap();
    private static RevCommit context;
    public static RevCommit getContext() {
        return context;
    }

    public static void main(String[] args) throws GitAPIException, IOException {
        GitRepository gr = GitRepository.init("t.git");

        RevCommit baseline = gr.getCommit(BASELINE_TAG+"^{}");
        context = baseline;
        if (null == context) {
            log.info("Null baseline context");
            System.exit(1);
        }
        // TODO: check on baseline existence, and prime if not
        log.info("Baseline {}", baseline.abbreviate(10).name());
        gr.branch(baseline.getId(), "foo");


        ds = DataStore.of(getContext());

        try (DataStore.Transaction tx = ds.beginTX())
        {
            FrameA af = new FrameA(1, "one0", "one1", "one2");
            FrameB bf = new FrameB(2, "two0", "two1", "two2");
            ds.add(af);
            ds.add(bf);
            ds.dump();
//            a();
            FrameA af0 = FrameA.get(ds, 1);
            log.info("got FrameA {}", af0);
            tx.commit();
        }
        ds.dump();


        log.info("{}", paths("01/02/03/K.04"));



//        ObjectId treeId = ObjectId.fromString("434943a8265129a744745e5d12fa2625a784b283");
//        Tree t = new Tree(gr.repo(), treeId, null);

//        log.info("");
//        try (
//                Transaction tx = ds.beginTX())
//
//        {
//            MutableK k1 = new MutableK(5, "five0", "five1", "five2");
//            tx.add(5, k1);
//            MutableK k2 = new MutableK(6, "six0", "six1", "six2");
//            tx.add(6, k2);
//            tx.remove(3);
//            tx.dump();
//            ds.dump();
//            tx.commit();
//        }
//        ds.dump();

//        try (Transaction tx0 = ds.beginTX()) {
//            MutableK k1 = new MutableK(1, "one0", "one1", "one2");
//            tx0.add(1L, k1);
//            tx0.add(2L, new MutableK(2, "two0", "two1", "two2"));
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

        for (int i=p.lastIndexOf('/'); i!= -1; i=p.lastIndexOf('/')) {
            p = p.substring(0, i);
            l.add(p);
            log.info("index {} {}", i, p);
        }
        return l;
    }
    // =================================
    static void a() {
//        try (DataStore.Transaction tx = ds.beginTX()) {
//            ds.add(3, new MutableK(3, "three0", "three1", "three2"));
//            ds.add(4, new MutableK(4, "four0", "four1", "four2"));
//            ds.dump();
//            tx.commit();
//        }
    }

    abstract static class K extends Keyed {
        static String getPath(Integer key) {
            return "K/" + key;
        }

        protected K(Integer key) {
            super(key);
        }

        @Override
        public String getType() {
            return "K";
        }

        @Override
        public boolean equals(Object other) {
            log.info("equals {}.equals({})", this, other);
            if (other == this)
                return true;
            if (null == other)
                return false;
            if (!other.getClass().equals(getClass()))
                return false;
            return key == ((K) other).key;
        }
    }

    @ToString
    public static class ImmutableK extends K {
        ImmutableK(MutableK mk) {
            super(mk.key);
            this.s0 = mk.s0;
            this.s1 = mk.s1;
            this.s2 = mk.s2;
        }

        public Keyed immutable() {
            return this;
        }

        public Keyed mutable() {
            return new MutableK(this);
        }

        final String s0;
        final String s1;
        final String s2;
    }

    @ToString
    public static class MutableK extends K {
        MutableK(int id, String s0, String s1, String s2) {
            super(id);
            this.s0 = s0;
            this.s1 = s1;
            this.s2 = s2;
        }

        MutableK(ImmutableK ik) {
            super(ik.key);
            this.s0 = ik.s0;
            this.s1 = ik.s1;
            this.s2 = ik.s2;
        }

        @Override
        public Keyed immutable() {
            return new ImmutableK(this);
        }

        @Override
        public Keyed mutable() {
            return this;
        }

        String s0;
        String s1;
        String s2;
    }

    private static final long serialVersionUID = 1L;
}
