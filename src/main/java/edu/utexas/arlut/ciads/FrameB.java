package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.repo.*;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


// =================================
@Slf4j
public class FrameB implements Proxy {
    public final Integer key;
    public static final String TYPE_VALUE = "FrameB";

    public static Builder builder(String id) {
        return new Builder(id);
    }
    public static class Builder {
        private String id;
        private String s0 = "";
        private String s1 = "";
        private String s2 = "";
        private Builder(String id) {
            this.id = id;
        }
        public Builder s0(String s0) {
            this.s0 = s0;
            return this;
        }
        public Builder s1(String s1) {
            this.s1 = s1;
            return this;
        }
        public Builder s2(String s2) {
            this.s2 = s2;
            return this;
        }
        public Impl build() {
            return new Impl(id, s0, s1, s2);
        }
    }
    public static FrameB create(Integer key) {
        return new FrameB(key);
    }
    private FrameB(Integer key) {
        this.key = key;
    }

    // =================================
    public String getS0() {
        return impl().s0;
    }
    public FrameB setS0(String s0) {
        mutable().s0 = s0;
        return this;
    }
    public String getS1() {
        return impl().s1;
    }
    public FrameB setS1(String s1) {
        mutable().s1 = s1;
        return this;
    }
    public String getS2() {
        return impl().s2;
    }
    public FrameB setS2(String s2) {
        mutable().s2 = s2;
        return this;
    }

    @Override
    public String getType() {
        return TYPE_VALUE;
    }
    @Override
    public Integer getKey() {
        return key;
    }
    @Override
    public Impl impl() {
        DataStore ds = RuntimeContext.getDS();
        return (Impl)ds.getImpl(key, FrameB.class);
    }
    @Override
    public Impl mutable() {
        DataStore ds = RuntimeContext.getDS();
        return (Impl)ds.getImplForMutation(key, FrameB.class);
    }
    // =================================
    @Override
    public Class<Impl> proxiedClass() {
        return Impl.class;
    }

    @Override
    public String toString() {
        return Integer.toString(key) + "=" + impl().toString();
    }

    // this is a morally immutable class
    @ToString
    public static class Impl extends Proxied<FrameB> {
        private String id;
        private String s0;
        private String s1;
        private String s2;
        private final String __type = FrameB.TYPE_VALUE;

        // for serializer reconstruction
        private
        Impl() {
            super();
        }

        Impl(String id, String s0, String s1, String s2) {
            super(id);
            this.id = id;
            this.s0 = s0;
            this.s1 = s1;
            this.s2 = s2;
        }

        @Override
        public String getType() {
            return __type;
        }
        @Override
        public Impl copy() {
            return new Impl(id, s0, s1, s2);
        }

        @Override
        public FrameB proxyOf(Integer key) {
            return new FrameB(key);
        }

    }
}
