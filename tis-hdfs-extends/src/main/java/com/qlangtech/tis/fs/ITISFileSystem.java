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
package com.qlangtech.tis.fs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface ITISFileSystem {

    /**
     * 取得文件系统的名称 hdfs ，OSS，或者其他
     *
     * @return
     */
    public String getName();

    public IPath getPath(String path);

    public IPath getPath(IPath parent, String name);

    public OutputStream getOutputStream(IPath path);

    public IFSDataInputStream open(IPath path, int bufferSize);

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

    /**
     * 取得文件信息
     *
     * @param path
     * @return
     */
    public IPathInfo getFileInfo(IPath path);

    public boolean delete(IPath f, boolean recursive) throws IOException;

    public void close();
}
