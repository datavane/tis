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
package com.qlangtech.tis.fullbuild.taskflow.hive;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import com.qlangtech.tis.dump.hive.HiveRemoveHistoryDataTask;
import com.qlangtech.tis.dump.hive.HiveRemoveHistoryDataTask.PathInfo;
import com.qlangtech.tis.exec.IExecChainContext;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RemoveJoinHistoryDataTask {

    /**
     * 删除宽表历史数据
     *
     * @param execConetxt
     * @throws Exception
     */
    public void deleteHistoryJoinTable(String tableName, IExecChainContext execConetxt) throws Exception {
        if (execConetxt == null) {
            throw new IllegalArgumentException("param: execContext can not be null");
        }
        final String hdfsPath = HiveRemoveHistoryDataTask.getJoinTableStorePath(execConetxt.getContextUserName(), tableName);
        FileSystem fileSys = execConetxt.getDistributeFileSystem();
        Path parent = new Path(hdfsPath);
        if (!fileSys.exists(parent)) {
            return;
        }
        FileStatus[] child = fileSys.listStatus(parent);
        PathInfo pathinfo = null;
        List<PathInfo> timestampList = new ArrayList<PathInfo>();
        Matcher matcher = null;
        for (FileStatus c : child) {
            matcher = HiveRemoveHistoryDataTask.DATE_PATTERN.matcher(c.getPath().getName());
            if (matcher.find()) {
                pathinfo = new PathInfo();
                pathinfo.setPathName(c.getPath().getName());
                pathinfo.setTimeStamp(Long.parseLong(matcher.group()));
                timestampList.add(pathinfo);
            }
        }
        HiveRemoveHistoryDataTask.deleteOldHdfsfile(fileSys, parent, timestampList);
    }
}
