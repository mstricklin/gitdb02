// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.repo.IKeyed;
import lombok.Getter;

// =================================
public class AFrame implements IKeyed<Integer> {
    private AFrame(int id, String s0, String s1) {
        this.id = id;
        this.s0 = s0;
        this.s1 = s1;
    }
    public static AFrameBuilder builder() {
        return new AFrameBuilder();
    }
    // =================================
    private final int id;
    @Getter
    private final String s0;
    @Getter
    private final String s1;
    @Override
    public Integer id() {
        return id;
    }
    @Override
    public String getType() {
        return "AFrame";
    }

    // =================================
    public static class AFrameBuilder {
        private int id;
        private String s0;
        private String s1;
        AFrameBuilder() {
        }
        AFrameBuilder(final AFrame af) {
            this.id = af.id;
            this.s0 = af.s0;
            this.s1 = af.s1;
        }
        public AFrame.AFrameBuilder id(int id) {
            this.id = id;
            return this;
        }
        public AFrame.AFrameBuilder s0(String s0) {
            this.s0 = s0;
            return this;
        }
        public AFrame.AFrameBuilder s1(String s1) {
            this.s1 = s1;
            return this;
        }
        public AFrame build() {
            return new AFrame(id, s0, s1);
        }
    }
}
