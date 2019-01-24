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
package com.qlangtech.tis.indexbuilder.source.impl;

import com.qlangtech.tis.indexbuilder.map.IndexConf;
import java.util.List;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DefaultFileSplitor extends FileSplitor {

    public static final Logger logger = LoggerFactory.getLogger(DefaultFileSplitor.class);

    public DefaultFileSplitor(IndexConf indexConf, FileSystem fileSystem) {
        super(indexConf, fileSystem);
    }

    public void getFiles(Path path, List<FileStatus> dataFiles) throws Exception {
        FileStatus[] stats = this.fileSystem.listStatus(path);
        if (stats == null) {
            throw new Exception("源文件路径不存在！");
        }
        for (FileStatus stat : stats) {
            logger.warn("【注意】需要Dump索引化的文件路径为：" + stat.getPath());
            if (stat.isDir()) {
                getFiles(stat.getPath(), dataFiles);
            } else {
                String name = stat.getPath().getName();
                if ((!name.endsWith(".suc")) && (!name.endsWith(".ok"))) {
                    dataFiles.add(stat);
                }
            }
        }
    }
}
