/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package store.hdfs;

import com.qlangtech.tis.fs.FSDataInputStream;
import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.IPathInfo;
import com.qlangtech.tis.fs.ITISFileSystem;
import org.apache.lucene.store.*;
import org.apache.solr.store.blockcache.CustomBufferedIndexInput;
import org.apache.solr.store.hdfs.HdfsDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisAbstractDirectory extends BaseDirectory {

    public static Logger LOG = LoggerFactory.getLogger(HdfsDirectory.class);

    public static final int BUFFER_SIZE = 8192;

    private static final String LF_EXT = ".lf";

    protected final IPath hdfsDirPath;

    // protected final Configuration configuration;
    private final ITISFileSystem fileSystem;

    // private final AtomicLong allReadBytesCount;
    private final Set<String> readFiles = new HashSet<>();

    public TisAbstractDirectory(IPath hdfsDirPath, ITISFileSystem fileSystem) throws IOException {
        this(hdfsDirPath, NoLockFactory.INSTANCE, fileSystem);
    }

    @Override
    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
        throw new UnsupportedOperationException();
    }

    private TisAbstractDirectory(IPath path, LockFactory lockFactory, ITISFileSystem fileSystem) throws IOException {
        super(lockFactory);
        this.hdfsDirPath = path;
        this.fileSystem = fileSystem;
    }

    @Override
    public void close() throws IOException {
        LOG.info("Closing hdfs directory {}", hdfsDirPath);
        fileSystem.close();
        isOpen = false;
    }

    @Override
    public Set<String> getPendingDeletions() throws IOException {
        return Collections.emptySet();
    }

    /**
     * Check whether this directory is open or closed. This check may return stale
     * results in the form of false negatives.
     *
     * @return true if the directory is definitely closed, false if the directory is
     * open or is pending closure
     */
    public boolean isClosed() {
        return !isOpen;
    }

    @Override
    public IndexOutput createOutput(String name, IOContext context) throws IOException {
        // public class HdfsFileWriter extends OutputStreamIndexOutput {
        // 
        // public static final String HDFS_SYNC_BLOCK = "solr.hdfs.sync.block";
        // public static final int BUFFER_SIZE = 16384;
        // 
        // public HdfsFileWriter(FileSystem fileSystem, Path path, String name) throws
        // IOException {
        // super("fileSystem=" + fileSystem + " path=" + path, name,
        // getOutputStream(fileSystem, path), BUFFER_SIZE);
        // }
        int BUFFER_SIZE = 16384;
        IPath path = fileSystem.getPath(hdfsDirPath, name);
        return new OutputStreamIndexOutput("fileSystem=" + fileSystem + " path=" + path, name, fileSystem.getOutputStream(path), BUFFER_SIZE);
    // return new HdfsFileWriter(getFileSystem(), fileSystem.getPath(hdfsDirPath,
    // name), name);
    }

    private String[] getNormalNames(List<String> files) {
        int size = files.size();
        for (int i = 0; i < size; i++) {
            String str = files.get(i);
            files.set(i, toNormalName(str));
        }
        return files.toArray(new String[] {});
    }

    private String toNormalName(String name) {
        if (name.endsWith(LF_EXT)) {
            return name.substring(0, name.length() - 3);
        }
        return name;
    }

    @Override
    public IndexInput openInput(String name, IOContext context) throws IOException {
        return openInput(name, BUFFER_SIZE);
    }

    private IndexInput openInput(String name, int bufferSize) throws IOException {
        return new HdfsIndexInput(name, getFileSystem(), this.fileSystem.getPath(hdfsDirPath, name), BUFFER_SIZE);
    }

    @Override
    public void deleteFile(String name) throws IOException {
        // Path path = new Path(hdfsDirPath, name);
        IPath path = this.fileSystem.getPath(hdfsDirPath, name);
        LOG.debug("Deleting {}", path);
        getFileSystem().delete(path, false);
    }

    @Override
    public void syncMetaData() throws IOException {
    }

    @Override
    public void rename(String source, String dest) throws IOException {
    // Path sourcePath = new Path(hdfsDirPath, source);
    // Path destPath = new Path(hdfsDirPath, dest);
    // fileContext.rename(sourcePath, destPath);
    }

    // @Override
    // public void renameFile(String source, String dest) throws IOException {
    // 
    // }
    @Override
    public long fileLength(String name) throws IOException {
        IPathInfo fileStatus = getFileSystem().getFileInfo(this.fileSystem.getPath(hdfsDirPath, name));
        return fileStatus.getLength();
    // return HdfsFileReader.getLength(getFileSystem(), new Path(hdfsDirPath,
    // name));
    }

    public long fileModified(String name) throws IOException {
        // .getFileStatus(new
        IPathInfo fileStatus = getFileSystem().getFileInfo(this.fileSystem.getPath(hdfsDirPath, name));
        // name));
        return fileStatus.getModificationTime();
    }

    @Override
    public String[] listAll() throws IOException {
        List<IPathInfo> childinfos = getFileSystem().listChildren(hdfsDirPath);
        // FileStatus[] listStatus = getFileSystem().listStatus(hdfsDirPath);
        List<String> files = new ArrayList<>();
        if (childinfos == null) {
            return new String[] {};
        }
        for (IPathInfo status : childinfos) {
            // files.add(status.getPath().getName());
            files.add(status.getName());
        }
        return getNormalNames(files);
    }

    public IPath getHdfsDirPath() {
        return hdfsDirPath;
    }

    public ITISFileSystem getFileSystem() {
        return fileSystem;
    }

    public static Logger logger = LoggerFactory.getLogger(HdfsIndexInput.class);

    private class HdfsIndexInput extends CustomBufferedIndexInput {

        private final IPath path;

        private final FSDataInputStream inputStream;

        private final long length;

        private boolean clone = false;

        public HdfsIndexInput(String name, ITISFileSystem fileSystem, IPath path, int bufferSize) throws IOException {
            super(name);
            this.path = path;
            logger.debug("Opening normal index input on {}", path);
            IPathInfo fileStatus = fileSystem.getFileInfo(path);
            length = fileStatus.getLength();
            inputStream = fileSystem.open(path, bufferSize);
        }

        @Override
        public void readBytes(byte[] b, int offset, int len) throws IOException {
            super.readBytes(b, offset, len);
        // allReadBytesCount.addAndGet(len);
        }

        @Override
        protected void readInternal(byte[] b, int offset, int readLength) throws IOException {
            inputStream.readFully(getFilePointer(), b, offset, readLength);
        // if (!shallNotCalculateReadBytesCount) {
        // 
        // synchronized (this) {
        // if (!readFiles.contains(path.toString())) {
        // readFiles.add(path.toString());
        // }
        // }
        // 
        // if ((this.fileReaded + readLength) > this.length) {
        // allReadBytesCount.addAndGet(this.length - this.fileReaded);
        // } else {
        // allReadBytesCount.addAndGet(readLength);
        // this.fileReaded += readLength;
        // }
        // }
        }

        @Override
        protected void seekInternal(long pos) throws IOException {
        }

        @Override
        protected void closeInternal() throws IOException {
            logger.debug("Closing normal index input on {}", path);
            if (!clone) {
                inputStream.close();
            }
        }

        @Override
        public long length() {
            return length;
        }

        @Override
        public IndexInput clone() {
            HdfsIndexInput clone = (HdfsIndexInput) super.clone();
            clone.clone = true;
            return clone;
        }
    }

    @Override
    public void sync(Collection<String> names) throws IOException {
        LOG.debug("Sync called on {}", Arrays.toString(names.toArray()));
    }

    @Override
    public int hashCode() {
        return hdfsDirPath.hashCode();
    }
    // @Override
    // public boolean equals(Object obj) {
    // if (obj == this) {
    // return true;
    // }
    // if (obj == null) {
    // return false;
    // }
    // if (!(obj instanceof HdfsDirectory)) {
    // return false;
    // }
    // return this.hdfsDirPath.equals(((HdfsDirectory) obj).getHdfsDirPath());
    // }
}
