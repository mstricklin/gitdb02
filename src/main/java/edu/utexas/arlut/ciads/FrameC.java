// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import edu.utexas.arlut.ciads.repo.IKeyed;
import lombok.ToString;

public interface FrameC {
    @ToString
    class Impl extends Keyed implements FrameC {
        int id;
        String s0;
        String s1;
        String s2;
        String __type = FrameA.type;

        protected Impl(int key) {
            super(key);
        }
        @Override
        public IKeyed<Integer> copy() {
            return null;
        }
        @Override
        public String getType() {
            return null;
        }
    }
}
