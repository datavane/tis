/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.indexbuilder.merger;

import com.qlangtech.tis.fs.*;
import org.apache.lucene.store.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年9月11日 下午5:07:24
 */
public class FileSystemDirectory extends Directory {

    private final ITISFileSystem fs;

    private final IPath directory;

    private final int ioFileBufferSize;

    public FileSystemDirectory(ITISFileSystem fs, IPath directory, boolean create) throws IOException {
        this.fs = fs;
        this.directory = directory;
        // conf.getInt("io.file.buffer.size", 4096);
        this.ioFileBufferSize = 4096;
        if (create) {
            create();
        }
        // boolean isDir = false;
        IPathInfo status = fs.getFileInfo(directory);
        if (status != null && !status.isDir()) {
            throw new IOException(directory + " is not a directory");
        }
    }

    @Override
    public Set<String> getPendingDeletions() throws IOException {
        return Collections.emptySet();
    }

    @Override
    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndexOutput createOutput(String name, IOContext context) throws IOException {
        IPath file = this.fs.getPath(this.directory, name);
        if ((this.fs.exists(file)) && (!this.fs.delete(file))) {
            throw new IOException("Cannot overwrite index file " + file);
        }
        return new FileSystemIndexOutput(file, this.ioFileBufferSize);
    // return null;
    }

    @Override
    public void sync(Collection<String> names) throws IOException {
    }

    @Override
    public void syncMetaData() throws IOException {
    }

    @Override
    public void rename(String source, String dest) throws IOException {
    }

    @Override
    public IndexInput openInput(String name, IOContext context) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Lock obtainLock(String name) throws IOException {
        return NoLockFactory.INSTANCE.obtainLock(this, name);
    }

    private void create() throws IOException {
        if (!this.fs.exists(this.directory)) {
            this.fs.mkdirs(this.directory);
        }
        boolean isDir = false;
        // try {
        IPathInfo status = this.fs.getFileInfo(this.directory);
        if (status != null) {
            isDir = status.isDir();
        }
        // }
        if (!isDir) {
            throw new IOException(this.directory + " is not a directory");
        }
        List<IPathInfo> fileStatus = this.fs.listChildren(this.directory, LuceneIndexFileNameFilter.getFilter());
        for (int i = 0; i < fileStatus.size(); i++) {
            if (!this.fs.delete(fileStatus.get(i).getPath())) {
                throw new IOException("Cannot delete index file " + fileStatus.get(i).getPath());
            }
        }
    }

    public String[] listAll() throws IOException {
        List<IPathInfo> fileStatus = this.fs.listChildren(this.directory, LuceneIndexFileNameFilter.getFilter());
        String[] result = new String[fileStatus.size()];
        for (int i = 0; i < fileStatus.size(); i++) {
            result[i] = fileStatus.get(i).getPath().getName();
        }
        return result;
    }

    public boolean fileExists(String name) throws IOException {
        return this.fs.exists(this.fs.getPath(this.directory, name));
    }

    public long fileModified(String name) {
        throw new UnsupportedOperationException();
    }

    public void touchFile(String name) {
        throw new UnsupportedOperationException();
    }

    public long fileLength(String name) throws IOException {
        return this.fs.getFileInfo(fs.getPath(this.directory, name)).getLength();
    // return this.fs.getFileStatus(new Path(this.directory, name)).getLen();
    }

    public void deleteFile(String name) throws IOException {
    // if (!this.fs.delete(new Path(this.directory, name)))
    // throw new IOException("Cannot delete index file " + name);
    }

    public void renameFile(String from, String to) throws IOException {
        this.fs.rename(this.fs.getPath(this.directory, from), this.fs.getPath(this.directory, to));
    }

    // public IndexOutput createOutput(String name) throws IOException {
    //
    // }
    public IndexInput openInput(String name) throws IOException {
        return openInput(name, this.ioFileBufferSize);
    }

    public IndexInput openInput(String name, int bufferSize) throws IOException {
        return new FileSystemIndexInput(this.fs.getPath(this.directory, name), bufferSize);
    }

    /*
     * public Lock makeLock(String name) { return new Lock(name) { public boolean
     * obtain() { return true; }
     *
     * public void release() { }
     *
     * public boolean isLocked() { throw new UnsupportedOperationException(); }
     *
     * public String toString() { return "Lock@" + new
     * Path(FileSystemDirectory.this.directory, this.val$name); } }; }
     */
    public void close() throws IOException {
    }

    public String toString() {
        return getClass().getName() + "@" + this.directory;
    }

    private class FileSystemIndexOutput extends IndexOutput {

        private final IPath filePath;

        private final TISFSDataOutputStream out;

        private boolean isOpen;

        public FileSystemIndexOutput(IPath path, int ioFileBufferSize) throws IOException {
            super(path.getName(), path.getName());
            this.filePath = path;
            this.out = FileSystemDirectory.this.fs.create(path, true, ioFileBufferSize);
            this.isOpen = true;
        }

        @Override
        public void close() throws IOException {
            if (this.isOpen) {
                this.out.close();
                this.isOpen = false;
            } else {
                throw new IOException("Index file " + this.filePath + " already closed");
            }
        }

        // public void seek(long pos) throws IOException {
        // throw new UnsupportedOperationException();
        // }
        //
        // public long length() throws IOException {
        // return this.out.getPos();
        // }
        @Override
        public long getFilePointer() {
            try {
                return this.out.getPos();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public long getChecksum() throws IOException {
            return this.out.getPos();
        }

        // public void flushBuffer(byte[] b, int offset, int size)
        // throws IOException {
        // this.out.write(b, offset, size);
        // }
        @Override
        public void writeByte(byte b) throws IOException {
            this.out.write(b);
        }

        @Override
        public void writeBytes(byte[] b, int offset, int length) throws IOException {
            this.out.write(b, offset, length);
        }

        protected void finalize() throws IOException {
            if (this.isOpen) {
                close();
            }
        }
    }

    private class FileSystemIndexInput extends BufferedIndexInput {

        private final IPath filePath;

        private final Descriptor descriptor;

        private final long length;

        private boolean isOpen;

        private boolean isClone;

        @Override
        protected void readInternal(ByteBuffer b) throws IOException {
        }

        public FileSystemIndexInput(IPath path, int ioFileBufferSize) throws IOException {
            super(path.getName());
            this.filePath = path;
            this.descriptor = new Descriptor(path, ioFileBufferSize);
            this.length = FileSystemDirectory.this.fs.getFileInfo(path).getLength();
            this.isOpen = true;
        }

//        @Override
//        protected void readInternal(byte[] b, int offset, int len) throws IOException {
//            synchronized (this.descriptor) {
//                long position = getFilePointer();
//                if (position != this.descriptor.position) {
//                    this.descriptor.in.seek(position);
//                    this.descriptor.position = position;
//                }
//                int total = 0;
//                do {
//                    int i = this.descriptor.in.read(b, offset + total, len - total);
//                    if (i == -1) {
//                        throw new IOException("Read past EOF");
//                    }
//                    this.descriptor.position += i;
//                    total += i;
//                } while (total < len);
//            }
//        }

        public void close() throws IOException {
            if (!this.isClone)
                if (this.isOpen) {
                    this.descriptor.in.close();
                    this.isOpen = false;
                } else {
                    throw new IOException("Index file " + this.filePath + " already closed");
                }
        }

        protected void seekInternal(long position) {
        }

        public long length() {
            return this.length;
        }

        protected void finalize() throws IOException {
            if ((!this.isClone) && (this.isOpen)) {
                close();
            }
        }

        public BufferedIndexInput clone() {
            FileSystemIndexInput clone = (FileSystemIndexInput) super.clone();
            clone.isClone = true;
            return clone;
        }

        private class Descriptor {

            public final FSDataInputStream in;

            public long position;

            public Descriptor(IPath file, int ioFileBufferSize) throws IOException {
                this.in = FileSystemDirectory.this.fs.open(file, ioFileBufferSize);
            }
        }
    }
}
