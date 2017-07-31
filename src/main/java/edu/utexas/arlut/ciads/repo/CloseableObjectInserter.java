package edu.utexas.arlut.ciads.repo;

import org.eclipse.jgit.lib.*;

import java.io.Closeable;
import java.io.IOException;

public class CloseableObjectInserter implements Closeable {
    public static CloseableObjectInserter of(Repository repo) {
        return new CloseableObjectInserter(repo.newObjectInserter());
    }

    public ObjectInserter inserter() {
        return oi;
    }

    public ObjectId insert(TreeFormatter formatter) throws IOException {
        return oi.insert(formatter);
    }

    public ObjectId insert(CommitBuilder builder) throws IOException {
        return oi.insert(builder);
    }

    public ObjectId insert(int type, byte[] data) throws IOException {
        return oi.insert(type, data);
    }

    // TODO make this a ClosableObjectReader
    public ObjectReader newReader() {
        return oi.newReader();
    }

    @Override
    public void close() throws IOException {
        oi.flush();
        oi.release();
    }

    private CloseableObjectInserter(ObjectInserter oi_) {
        oi = oi_;
    }

    private final ObjectInserter oi;
}
