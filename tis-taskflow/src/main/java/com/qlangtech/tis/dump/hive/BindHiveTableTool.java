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
package com.qlangtech.tis.dump.hive;

import java.util.Set;
import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/*
 * 将hdfs上的数据和hive database中的表绑定
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class BindHiveTableTool {

    private static final Logger logger = LoggerFactory.getLogger(BindHiveTableTool.class);

    public static void bindHiveTables(FileSystem fileSystem, Set<String> hiveTables, final String userName, String timestamp) throws Exception {
        Assert.notNull(fileSystem);
        Assert.notNull(userName);
        Assert.notNull(timestamp);
        StringBuffer tabs = new StringBuffer();
        for (String tab : hiveTables) {
            tabs.append(tab).append(",");
        }
        // System.getProperty("user.name");
        final String user = userName;
        // ▼▼▼▼ bind hive table task
        logger.info("start hive table bind,tabs:" + tabs.toString());
        // Dump 任务结束,开始绑定hive partition
        HiveTableBuilder hiveTableBuilder = new HiveTableBuilder(timestamp, user);
        hiveTableBuilder.setHiveDbHeler(HiveDBUtils.getInstance());
        hiveTableBuilder.setFileSystem(fileSystem);
        hiveTableBuilder.bindHiveTables(fileSystem, hiveTables, userName);
    }
}
