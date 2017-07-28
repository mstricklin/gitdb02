package edu.utexas.arlut.ciads;

import com.google.common.base.Splitter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Slf4j
public class App {

    static DataStore ds = new DataStore();
    public static void main(String[] args) {

        try (DataStore.Transaction tx0 = ds.beginTX()) {
            tx0.add(2L, new MutableK(2L, "two0", "two1", "two2"));
            tx0.commit();
//            tx0.dump();
        }
//        MutableK k1 = new MutableK(1L, "one0", "one1", "one2");
//        tx0.add(1L, k1);
//        tx0.add(2L, new MutableK(2L, "two0", "two1", "two2"));
//        log.info("DS 2L {}", ds.get(2L));
//        log.info("TX0 2L {}", tx0.get(2L));
//
//        tx0.dump();
//        log.info("Mutate...");
//        k1.s0 = "one0A";
//        tx0.dump();
////        tx.commit();
//        ds.commit();
//        log.info("DS 2L {}", ds.get(2L));
//        ds.dump();
//        a();
//
//        ds.dump();
//
//        try (DataStore.Transaction tx1 = ds.beginTX()) {
//            tx1.remove(1L);
//            tx1.dump();
//            tx1.commit();
//        }
        ds.dump();
        log.info("");
//        K k2 = ds.get(2L);
//        log.info("2L {}", k2);
        try (DataStore.Transaction tx2 = ds.beginTX()) {
            log.info("2L {}", ds.getMutable(2L));
        }

        String s0 = "K/22";
        log.info("path: {}", s0);
        int i = s0.lastIndexOf('/');
        log.info("last index {}", i);
        String s1 = s0.substring(0, i);
        log.info("subStr {}", s1);


        List<String> ls = newArrayList(Splitter.on('/').split(s0));
        log.info("Split: {}", ls);


        ds.shutdown();
    }
    // =================================
    static void a() {
        try (DataStore.Transaction tx = ds.beginTX()) {
            tx.add(3L, new MutableK(3L, "three0", "three1", "three2"));
            tx.add(4L, new MutableK(4L, "four0", "four1", "four2"));
            tx.dump();
            tx.commit();
        }
    }
    // =================================
    abstract static class K extends Keyed {
        static String getPath(Long id) {
            return "/K/" + id;
        }
        protected K(Long id) {
            this.id = id;
        }
        @Override
        public Long id() {
            return 2L;
        }
        @Override
        String getType() {
            return "K";
        }
        protected final long id;
    }

    @ToString
    private static class ImmutableK extends K {
        ImmutableK(MutableK mk) {
            super(mk.id);
            this.s0 = mk.s0;
            this.s1 = mk.s1;
            this.s2 = mk.s2;
        }
        @Override
        public Keyed immutable() {
            return this;
        }
        @Override
        public Keyed mutable() {
            return new MutableK(this);
        }
        final String s0;
        final String s1;
        final String s2;
    }

    @ToString
    private static class MutableK extends K {
        MutableK(long id, String s0, String s1, String s2) {
            super(id);
            this.s0 = s0;
            this.s1 = s1;
            this.s2 = s2;
        }
        MutableK(ImmutableK ik) {
            super(ik.id);
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
