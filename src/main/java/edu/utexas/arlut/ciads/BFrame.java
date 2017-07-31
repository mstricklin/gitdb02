package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.repo.Keyed;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


// =================================
@ToString
@Slf4j
public class BFrame {
    public BFrame(int id, String s0, String s1, String s2) {
        this.id = id;
        i = new Impl(id, s0, s1, s2);
    }
    Impl i;
    // =================================
    public String getS0() {
        // return getImmutableImpl(id).s0;
        return i.s0;
    }
    public BFrame setS0(String s0) {
        // return getImpl(id).s0;
        i.s0 = s0;
        return this;
    }
    public String getS1() {
        // return getImmutableImpl(id).s1;
        return i.s1;
    }
    public BFrame setS1(String s1) {
        // return getImpl(id).s1;
        i.s1 = s1;
        return this;
    }
    private final int id;
    public static final String type = AFrame.class.getSimpleName();


    @ToString
    public static class Impl extends Keyed {
        int id;
        String s0;
        String s1;
        String s2;
        String __type = AFrame.type;

        Impl(int id, String s0, String s1, String s2) {
            super(id);
            this.id = id;
            this.s0 = s0;
            this.s1 = s1;
            this.s2 = s2;
        }
        Impl(ImmutableImpl ii) {
            this(ii.id, ii.s0, ii.s1, ii.s2);
        }
        public Keyed mutable() { return this; }
        public Keyed immutable() { return new ImmutableImpl(this); }

        @Override
        public String getType() {
            return AFrame.type;
        }

    }
    @ToString
    public static class ImmutableImpl extends Keyed {
        final int id;
        final String s0;
        final String s1;
        final String s2;
        String __type = AFrame.type;

        ImmutableImpl(int id, String s0, String s1, String s2) {
            super(id);
            this.id = id;
            this.s0 = s0;
            this.s1 = s1;
            this.s2 = s2;
        }
        ImmutableImpl(Impl i) {
            this(i.id, i.s0, i.s1, i.s2);
        }
        public Keyed mutable() { return new Impl(this); }
        public Keyed immutable() { return this; }
        @Override
        public String getType() {
            return AFrame.type;
        }
    }
    // =================================
}
