package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.repo.IKeyed;
import edu.utexas.arlut.ciads.repo.Proxied;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


// =================================
@ToString
@Slf4j
public class FrameB implements Proxied {
    public FrameB(int id, String s0, String s1, String s2) {
        this.id = id;
        i = new Impl(id, s0, s1, s2);
    }

    // =================================
    public String getS0() {
        // return getImmutableImpl(id).s0;
        return i.s0;
    }
    public FrameB setS0(String s0) {
        // return getImpl(id).s0;
        i.s0 = s0;
        return this;
    }
    public String getS1() {
        // return getImmutableImpl(id).s1;
        return i.s1;
    }
    public FrameB setS1(String s1) {
        // return getImpl(id).s1;
        i.s1 = s1;
        return this;
    }
    public String getS2() {
        // return getImmutableImpl(id).s1;
        return i.s2;
    }
    public FrameB setS2(String s2) {
        // return getImpl(id).s1;
        i.s2 = s2;
        return this;
    }

    private String getType() {
        return type;
    }
    private final int id;
    public static final String type = FrameA.class.getSimpleName();

    FrameB.Impl i;
    @Override
    public IKeyed<Integer> impl() {
//        return i.immutable();
        return i;
    }
    @Override
    public IKeyed<Integer> mutable() {
//        return i.mutable();
        return i;
    }
    // =================================
    @ToString
    public static class Impl extends Keyed {
        int id;
        String s0;
        String s1;
        String s2;
        String __type = FrameA.type;
        Impl(int id, String s0, String s1, String s2) {
            super(id);
            this.id = id;
            this.s0 = s0;
            this.s1 = s1;
            this.s2 = s2;
        }
//        Impl(ImmutableImpl ii) {
//            this(ii.id, ii.s0, ii.s1, ii.s2);
//        }
//        public Keyed mutable() { return this; }
//        public Keyed immutable() {
//            return new ImmutableImpl(id, s0, s1, s2);
//        }
        @Override
        public String getType() {
            return FrameA.type;
        }
        @Override
        public Impl copy() {
            return new Impl(id, s0, s1, s2);
        }
    }
//    @ToString
//    @Value
//    public static class ImmutableImpl extends Keyed {
//        int id;
//        String s0;
//        String s1;
//        String s2;
//        String __type = FrameA.type;
//
////        ImmutableImpl(int id, String s0, String s1, String s2) {
////            super(id);
////            this.id = id;
////            this.s0 = s0;
////            this.s1 = s1;
////            this.s2 = s2;
////        }
////        ImmutableImpl(Impl i) {
////            this(i.id, i.s0, i.s1, i.s2);
////        }
//        public Keyed mutable() { return new Impl(this); }
//        public Keyed immutable() { return this; }
//        @Override
//        public String getType() {
//            return FrameA.type;
//        }
//        @Override
//        public ImmutableImpl copy() {
//            return new ImmutableImpl(id, s0, s1, s2);
//        }
//    }
    // =================================
}
