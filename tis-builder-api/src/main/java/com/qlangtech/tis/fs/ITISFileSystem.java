/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.fs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2018年11月23日
 */
public interface ITISFileSystem {
    /**
     * 文件系统的根目录
     *
     * @return
     */
    public String getRootDir();

    /**
     * 取得文件系统的名称 hdfs ，OSS，或者其他
     *
     * @return
     */
    public String getName();

    public IPath getPath(String path);

    public IPath getPath(IPath parent, String name);

    public OutputStream getOutputStream(IPath path);

    public FSDataInputStream open(IPath path, int bufferSize);

    public FSDataInputStream open(IPath path);

    public TISFSDataOutputStream create(IPath f, boolean overwrite, int bufferSize) throws IOException;

    public TISFSDataOutputStream create(IPath f, boolean overwrite) throws IOException;

    public boolean exists(IPath path);

    public boolean mkdirs(IPath f) throws IOException;

    public void copyToLocalFile(IPath srcPath, File dstPath);

    public void rename(IPath from, IPath to);

    public boolean copyFromLocalFile(File localIncrPath, IPath remoteIncrPath);

    public IFileSplitor getSplitor(IPath path) throws Exception;

    /**
     * 路径内容汇总信息
     *
     * @param path
     * @return
     */
    public IContentSummary getContentSummary(IPath path);

    /**
     * 取得子目录信息
     *
     * @param path
     * @return
     */
    public List<IPathInfo> listChildren(IPath path);

    public List<IPathInfo> listChildren(IPath path, IPathFilter filter);

    /**
     * 取得文件信息
     *
     * @param path
     * @return
     */
    public IPathInfo getFileInfo(IPath path);

    public boolean delete(IPath f, boolean recursive) throws IOException;

    public boolean delete(IPath f) throws IOException;

    public void close();

    public interface IPathFilter {

        /**
         * Tests whether or not the specified abstract pathname should be included in a
         * pathname list.
         *
         * @param path The abstract pathname to be tested
         * @return <code>true</code> if and only if <code>pathname</code> should be
         * included
         */
        boolean accept(IPath path);
    }
}
