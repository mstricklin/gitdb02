package edu.utexas.arlut.ciads.revdb.util;

import com.google.common.base.Function;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.util.Iterator;

public class TreeWalkIterable<T> implements Iterable<T> {
    public TreeWalkIterable(final TreeWalk tw, Function<TreeWalk, T> f) {
        this.tw = tw;
        this.transformer = f;
    }
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T> () {

            @Override
            public boolean hasNext() {
                try {
                    return tw.next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public T next() {
                return transformer.apply(tw);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("no changes allowed");
            }
        };
    }

    private final TreeWalk tw;
    private final Function<TreeWalk, T> transformer;
}
