// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads.repo;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static edu.utexas.arlut.ciads.repo.util.Strings.abbreviate;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.ObjectWritingException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.*;

@Slf4j
public class Tree implements Iterable<Tree.GitTreeEntry> {
    public Tree(final GitRepository gr) {
        this.gr = gr;
        this.id = ObjectId.zeroId();
        entries = newHashMap();
    }
    public Tree(final Tree src) {
        this.gr = src.gr;
        this.id = src.id;
        entries = newHashMap(src.entries);
    }
    public Tree(final GitRepository gr, final ObjectId myId) throws IOException {
        this.gr = gr;
        id = myId;
        entries = newHashMap();
        readTree();
    }
    public final ObjectId getId() {
        return id;
    }
    public void remove(@NonNull String name) {
        entries.remove(name);
        id = ObjectId.zeroId();
    }
    public GitTreeEntry add(@NonNull String name, @NonNull ObjectId id, @NonNull FileMode fm) {
        GitTreeEntry e = new GitTreeEntry(id, name, fm);
        entries.put(name, e);
        id = ObjectId.zeroId();
        return e;
    }
    public GitTreeEntry addTree(@NonNull String name, @NonNull ObjectId id) {
        GitTreeEntry e = new GitTreeEntry(id, name, FileMode.TREE);
        entries.put(name, e);
        id = ObjectId.zeroId();
        return e;
    }
    public GitTreeEntry addBlob(@NonNull String name, @NonNull ObjectId id) {
        GitTreeEntry e = new GitTreeEntry(id, name, FileMode.REGULAR_FILE);
        entries.put(name, e);
        id = ObjectId.zeroId();
        return e;
    }
    private static final TreeFormatter EMPTY_FORMATTER = new TreeFormatter(0);
    public static TreeFormatter emptyTree() throws IOException {
        return EMPTY_FORMATTER;
    }
    public ObjectId persist() throws IOException {
        TreeFormatter fmt = new TreeFormatter();
        final List<GitTreeEntry> sortedEntries = newArrayList(entries.values());
        Collections.sort(sortedEntries);

        for (GitTreeEntry e : sortedEntries) {
            ObjectId id = e.id;
            if (id == null)
                throw new ObjectWritingException(MessageFormat.format(JGitText.get().objectAtPathDoesNotHaveId, e.name));
            fmt.append(e.name, e.fm, id);
        }
        return gr.persist(fmt);
    }
    @Override
    public String toString() {
        return "Tree "+abbreviate(id)+" "+entries.size()+ " addItems";
    }
    // =================================
    @Override
    public Iterator<GitTreeEntry> iterator() {
        final List<GitTreeEntry> treeEntries = ImmutableList.copyOf(entries.values());
        return treeEntries.iterator();
    }
    // =================================

    private void readTree() throws IOException {
        ObjectLoader ldr = gr.repo().open(getId(), Constants.OBJ_TREE);
        readTree(ldr.getCachedBytes());
    }
    // stolen from deprecated jgit code
    private void readTree(final byte[] raw) throws CorruptObjectException {
        final int rawSize = raw.length;
        int rawPtr = 0;

        while (rawPtr < rawSize) {
            int c = raw[rawPtr++];
            if (c < '0' || c > '7')
                throw new CorruptObjectException(getId(), JGitText.get().corruptObjectInvalidEntryMode);
            int mode = c - '0';
            for (; ; ) {
                c = raw[rawPtr++];
                if (' ' == c)
                    break;
                else if (c < '0' || c > '7')
                    throw new CorruptObjectException(getId(), JGitText.get().corruptObjectInvalidMode);
                mode <<= 3;
                mode += c - '0';
            }

            int nameLen = 0;
            while (raw[rawPtr + nameLen] != 0)
                nameLen++;
            final byte[] name = new byte[nameLen];
            System.arraycopy(raw, rawPtr, name, 0, nameLen);
            rawPtr += nameLen + 1;

            final ObjectId id = ObjectId.fromRaw(raw, rawPtr);
            rawPtr += Constants.OBJECT_ID_LENGTH;

            final GitTreeEntry ent;
            String nameStr = decode(name);
            if (FileMode.REGULAR_FILE.equals(mode)) {
                ent = new GitTreeEntry(id, decode(name), FileMode.REGULAR_FILE);

            } else if (FileMode.EXECUTABLE_FILE.equals(mode)) {
                ent = new GitTreeEntry(id, decode(name), FileMode.EXECUTABLE_FILE);

            } else if (FileMode.TREE.equals(mode)) {
                ent = new GitTreeEntry(id, decode(name), FileMode.TREE);

            } else if (FileMode.SYMLINK.equals(mode)) {
                log.info("Haven't tested handling of SYMLINK entries");
                ent = new GitTreeEntry(id, decode(name), FileMode.SYMLINK);

            } else if (FileMode.GITLINK.equals(mode)) {
                log.info("Haven't tested handling of GITLINK entries");
                ent = new GitTreeEntry(id, decode(name), FileMode.GITLINK);

            } else {
                throw new CorruptObjectException(getId(), MessageFormat.format(
                        JGitText.get().corruptObjectInvalidMode2, Integer.toOctalString(mode)));
            }
            entries.put(nameStr, ent);
        }
    }

    // =================================
    private static ThreadLocal<CharsetDecoder> tlDecoder = new ThreadLocal<CharsetDecoder>() {
        @Override
        protected CharsetDecoder initialValue() {
            return Charsets.UTF_8.newDecoder()
                                 .onMalformedInput(CodingErrorAction.REPORT)
                                 .onUnmappableCharacter(CodingErrorAction.REPORT);
        }
    };
    private static String decode(byte[] b) {
        CharsetDecoder dec = tlDecoder.get();
        try {
            final ByteBuffer bb = ByteBuffer.wrap(b);
            return dec.decode(bb).toString();
        } catch (CharacterCodingException e) {
            return new String(b, Charsets.UTF_8);
        } finally {
            dec.reset();
        }
    }

    @RequiredArgsConstructor
    @ToString
    public static class GitTreeEntry implements Comparable<GitTreeEntry> {
        final ObjectId id;
        final String name;
        final FileMode fm;

        @Override
        public int compareTo(@NonNull GitTreeEntry peer) {
            int length1 = this.name.length();
            int length2 = peer.name.length();
            final int length = Math.min(length1, length2) + 1;
            for (int i = 0; i < length; i++) {
                final char c1;
                if (i < length1) {
                    c1 = this.name.charAt(i);
                } else if ((i == length1) && (this.fm == FileMode.TREE)) {
                    c1 = '/';
                } else {
                    c1 = 0;
                }
                final char c2;
                if (i < length2) {
                    c2 = peer.name.charAt(i);
                } else if ((i == length2) && (peer.fm == FileMode.TREE)) {
                    c2 = '/';
                } else {
                    c2 = 0;
                }
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
            return length1 - length2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GitTreeEntry rhs = (GitTreeEntry)o;

            return id.equals(rhs.id)
                    && fm.equals(rhs.fm)
                    && name.equals(rhs.name);
        }
    }

    private final Map<String, GitTreeEntry> entries;
    private final GitRepository gr;
    private ObjectId id;
}
