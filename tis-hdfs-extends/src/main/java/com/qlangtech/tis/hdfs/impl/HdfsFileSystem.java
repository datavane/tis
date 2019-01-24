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
package com.qlangtech.tis.hdfs.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CreateFlag;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsServerDefaults;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import com.qlangtech.tis.fs.IContentSummary;
import com.qlangtech.tis.fs.IFSDataInputStream;
import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.IPathInfo;
import com.qlangtech.tis.fs.ITISFileSystem;

/*
 * 相关的类是:TisAbstractDirectory
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsFileSystem implements ITISFileSystem {

    private final FileSystem fs;

    private static final String HDFS = "hdfs";

    public static final String HDFS_SYNC_BLOCK = "solr.hdfs.sync.block";

    public static final int BUFFER_SIZE = 16384;

    public HdfsFileSystem(FileSystem fs) {
        super();
        this.fs = fs;
    }

    @Override
    public String getName() {
        return HDFS;
    }

    @Override
    public IPath getPath(String path) {
        return new HdfsPath(path);
    }

    @Override
    public IPath getPath(IPath parent, String name) {
        return new HdfsPath(parent, name);
    }

    @Override
    public OutputStream getOutputStream(IPath p) {
        try {
            Path path = p.unwrap(Path.class);
            Configuration conf = fs.getConf();
            FsServerDefaults fsDefaults = fs.getServerDefaults(path);
            EnumSet<CreateFlag> flags = EnumSet.of(CreateFlag.CREATE, CreateFlag.OVERWRITE);
            if (Boolean.getBoolean(HDFS_SYNC_BLOCK)) {
                flags.add(CreateFlag.SYNC_BLOCK);
            }
            return fs.create(path, FsPermission.getDefault().applyUMask(FsPermission.getUMask(conf)), flags, fsDefaults.getFileBufferSize(), fsDefaults.getReplication(), fsDefaults.getBlockSize(), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IFSDataInputStream open(IPath path, int bufferSize) {
        return null;
    }

    @Override
    public IContentSummary getContentSummary(IPath path) {
        return null;
    }

    @Override
    public List<IPathInfo> listChildren(IPath path) {
        return null;
    }

    @Override
    public IPathInfo getFileInfo(IPath path) {
        return null;
    }

    @Override
    public boolean delete(IPath f, boolean recursive) throws IOException {
        return false;
    }

    @Override
    public void close() {
    }
}
