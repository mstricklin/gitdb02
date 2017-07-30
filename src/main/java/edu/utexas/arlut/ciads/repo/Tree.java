package edu.utexas.arlut.ciads.repo;

import org.eclipse.jgit.lib.ObjectId;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class Tree {
    Tree(Tree t) {
        entries = newHashMap(t.entries);
    }


    final Map<String, ObjectId> entries;
}
