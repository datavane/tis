/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.indexbuilder.merger;

import java.io.IOException;
import java.util.Collection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.NoLockFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FileSystemDirectory extends Directory {

    private final FileSystem fs;

    private final Path directory;

    private final int ioFileBufferSize;

    public FileSystemDirectory(FileSystem fs, Path directory, boolean create, Configuration conf) throws IOException {
        this.fs = fs;
        this.directory = directory;
        this.ioFileBufferSize = conf.getInt("io.file.buffer.size", 4096);
        if (create) {
            create();
        }
        // boolean isDir = false;
        FileStatus status = fs.getFileStatus(directory);
        if (status != null && !status.isDir()) {
            throw new IOException(directory + " is not a directory");
        }
    }

    @Override
    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndexOutput createOutput(String name, IOContext context) throws IOException {
        Path file = new Path(this.directory, name);
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
        try {
            FileStatus status = this.fs.getFileStatus(this.directory);
            if (status != null)
                isDir = status.isDir();
        } catch (IOException e) {
        }
        if (!isDir) {
            throw new IOException(this.directory + " is not a directory");
        }
        FileStatus[] fileStatus = this.fs.listStatus(this.directory, LuceneIndexFileNameFilter.getFilter());
        for (int i = 0; i < fileStatus.length; i++) {
            if (!this.fs.delete(fileStatus[i].getPath())) {
                throw new IOException("Cannot delete index file " + fileStatus[i].getPath());
            }
        }
    }

    public String[] listAll() throws IOException {
        FileStatus[] fileStatus = this.fs.listStatus(this.directory, LuceneIndexFileNameFilter.getFilter());
        String[] result = new String[fileStatus.length];
        for (int i = 0; i < fileStatus.length; i++) {
            result[i] = fileStatus[i].getPath().getName();
        }
        return result;
    }

    public boolean fileExists(String name) throws IOException {
        return this.fs.exists(new Path(this.directory, name));
    }

    public long fileModified(String name) {
        throw new UnsupportedOperationException();
    }

    public void touchFile(String name) {
        throw new UnsupportedOperationException();
    }

    public long fileLength(String name) throws IOException {
        return this.fs.getFileStatus(new Path(this.directory, name)).getLen();
    }

    public void deleteFile(String name) throws IOException {
    // if (!this.fs.delete(new Path(this.directory, name)))
    // throw new IOException("Cannot delete index file " + name);
    }

    public void renameFile(String from, String to) throws IOException {
        this.fs.rename(new Path(this.directory, from), new Path(this.directory, to));
    }

    // public IndexOutput createOutput(String name) throws IOException {
    // 
    // }
    public IndexInput openInput(String name) throws IOException {
        return openInput(name, this.ioFileBufferSize);
    }

    public IndexInput openInput(String name, int bufferSize) throws IOException {
        return new FileSystemIndexInput(new Path(this.directory, name), bufferSize);
    }

    /*
	 * public Lock makeLock(String name) { return new Lock(name) { public
	 * boolean obtain() { return true; }
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

        private final Path filePath;

        private final FSDataOutputStream out;

        private boolean isOpen;

        public FileSystemIndexOutput(Path path, int ioFileBufferSize) throws IOException {
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

        private final Path filePath;

        private final Descriptor descriptor;

        private final long length;

        private boolean isOpen;

        private boolean isClone;

        public FileSystemIndexInput(Path path, int ioFileBufferSize) throws IOException {
            super(path.getName());
            this.filePath = path;
            this.descriptor = new Descriptor(path, ioFileBufferSize);
            this.length = FileSystemDirectory.this.fs.getFileStatus(path).getLen();
            this.isOpen = true;
        }

        protected void readInternal(byte[] b, int offset, int len) throws IOException {
            synchronized (this.descriptor) {
                long position = getFilePointer();
                if (position != this.descriptor.position) {
                    this.descriptor.in.seek(position);
                    this.descriptor.position = position;
                }
                int total = 0;
                do {
                    int i = this.descriptor.in.read(b, offset + total, len - total);
                    if (i == -1) {
                        throw new IOException("Read past EOF");
                    }
                    this.descriptor.position += i;
                    total += i;
                } while (total < len);
            }
        }

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

            public Descriptor(Path file, int ioFileBufferSize) throws IOException {
                this.in = FileSystemDirectory.this.fs.open(file, ioFileBufferSize);
            }
        }
    }

	@Override
	public void syncMetaData() throws IOException {
	}

	@Override
	public void rename(String source, String dest) throws IOException {
	}
}
