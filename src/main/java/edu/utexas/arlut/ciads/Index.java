package edu.utexas.arlut.ciads;

import org.eclipse.jgit.lib.ObjectId;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class Index {

    Tree root = new Tree(); // varies by context...

    void add(ObjectId oid, String path) {

    }
    Tree getTree(String path) {
        return null;
    }
    public static class Tree {
        Tree() {
            parent = null;
        }
        Tree(Tree parent) {
            this.parent = parent;
        }

        private final Tree parent;
        private final Map<String, ObjectId> index = newHashMap();
    }
}
