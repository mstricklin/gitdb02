// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.revdb.DataView;
import edu.utexas.arlut.ciads.revdb.RevDBProxyItem;
import edu.utexas.arlut.ciads.revdb.main.RevDBProxiedItem;
import edu.utexas.arlut.ciads.revdb.main.RuntimeContext;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

// =================================
@Slf4j
public class TestFrameA implements RevDBProxyItem {

    public final Integer id;
    public static final String TYPE_VALUE = "FrameA";

    public static Builder builder(Integer id) {
        return new Builder(id);
    }
    public static class Builder {
        private Integer id;
        private String s0 = "";
        private String s1 = "";
        private String s2 = "";
        private Builder(Integer id) {
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
    public static TestFrameA create(Integer id) {
        return new TestFrameA(id);
    }
    private TestFrameA(Integer id) {
        this.id = id;
    }


    // =================================
    public String getS0() {
        return impl().s0;
    }
    public TestFrameA setS0(String s0) {
        mutable().s0 = s0;
        return this;
    }
    public String getS1() {
        return impl().s1;
    }
    public TestFrameA setS1(String s1) {
        mutable().s1 = s1;
        return this;
    }
    public String getS2() {
        return impl().s2;
    }
    public TestFrameA setS2(String s2) {
        mutable().s2 = s2;
        return this;
    }

    @Override
    public String getType() {
        return TYPE_VALUE;
    }
    @Override
    public Integer getId() {
        return id;
    }


    @Override
    public Impl impl() {
        DataView ds = RuntimeContext.getDS();
        return (Impl)ds.getImpl(id, TestFrameA.class);
    }
    @Override
    public Impl mutable() {
        DataView ds = RuntimeContext.getDS();
        return (Impl)ds.getImplForMutation(id, TestFrameA.class);
    }

    @Override
    public Class<Impl> proxiedClass() {
        return Impl.class;
    }

    @Override
    public String toString() {
        return Integer.toString(id) + "=" + impl().toString();
    }

    // this is a morally immutable class
    @ToString
    public static class Impl extends RevDBProxiedItem<TestFrameA> {
        private String s0;
        private String s1;
        private String s2;
        private final String __type = TestFrameA.TYPE_VALUE;

        // for serializer reconstruction
        private Impl() {
            super();
        }

        Impl(Integer id, String s0, String s1, String s2) {
            super(id);
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
        public TestFrameA proxyOf(Integer id) {
            return new TestFrameA(id);
        }

    }
}
