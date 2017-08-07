package edu.utexas.arlut.ciads.revdb.impl;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;

import java.io.Closeable;
import java.io.IOException;

public class CloseableObjectReader implements Closeable {
    public static CloseableObjectReader of(Repository repo) {
        return new CloseableObjectReader(repo.newObjectReader());
    }

    public ObjectReader reader() { return or; }

    public byte[] read(ObjectId oid) throws IOException {
        ObjectLoader ol = or.open(oid);
        return ol.getBytes();
    }
    @Override
    public void close() throws IOException {
        or.release();
    }
    private CloseableObjectReader(ObjectReader or_) { or = or_; }

    private final ObjectReader or;
}
