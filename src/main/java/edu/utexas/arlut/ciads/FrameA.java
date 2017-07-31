// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.repo.DataStore;
import edu.utexas.arlut.ciads.repo.IKeyed;
import edu.utexas.arlut.ciads.repo.Proxied;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

// =================================
@ToString
@Slf4j
public class FrameA implements Proxied {
    public FrameA(Impl i) {
        this.id = i.id;
        this.i = i;
    }
    public FrameA(int id, String s0, String s1, String s2) {
//        super(id);
        this.id = id;
        i = new Impl(id, s0, s1, s2);
    }
    public static FrameA get(DataStore ds, int id) {
        Impl i = ds.get(id, Impl.class);
        return new FrameA(i);
    }

    // =================================
    public String getS0() {
        // return getImmutableImpl(id).s0;
        return i.s0;
    }
    public FrameA setS0(String s0) {
        // return getImpl(id).s0;
        i.s0 = s0;
        return this;
    }
    public String getS1() {
        // return getImmutableImpl(id).s1;
        return i.s1;
    }
    public FrameA setS1(String s1) {
        // return getImpl(id).s1;
        i.s1 = s1;
        return this;
    }
    public String getS2() {
        // return getImmutableImpl(id).s1;
        return i.s2;
    }
    public FrameA setS2(String s2) {
        // return getImpl(id).s1;
        i.s2 = s2;
        return this;
    }

    private String getType() {
        return type;
    }

    public final int id;
    public static final String type = FrameA.class.getSimpleName();

    Impl i;
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
//        public Keyed immutable() { return new ImmutableImpl(this); }

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
//    public static class ImmutableImpl extends Keyed {
//        final int id;
//        final String s0;
//        final String s1;
//        final String s2;
//        String __type = FrameA.type;
//
//        ImmutableImpl(int id, String s0, String s1, String s2) {
//            super(id);
//            this.id = id;
//            this.s0 = s0;
//            this.s1 = s1;
//            this.s2 = s2;
//        }
//        ImmutableImpl(Impl i) {
//            this(i.id, i.s0, i.s1, i.s2);
//        }
//        public Keyed mutable() { return new Impl(this); }
//        public Keyed immutable() { return this; }
//        @Override
//        public String getType() {
//            return FrameA.type;
//        }
//    }
    // =================================
}
