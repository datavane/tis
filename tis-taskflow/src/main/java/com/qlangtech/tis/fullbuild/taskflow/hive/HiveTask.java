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

import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.dump.hive.HiveDBUtils;
import com.qlangtech.tis.fullbuild.taskflow.AdapterTask;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HiveTask extends AdapterTask {

    private HiveDBUtils hiveDBHelper;

    private static final Logger logger = LoggerFactory.getLogger(HiveTask.class);

    @Override
    protected void executeSql(String taskname, String sql) {
        try {
            logger.info("execute hive task:" + taskname);
            logger.info(sql);
            Connection conn = HiveTaskFactory.getConnection(this.getContext());
            getHiveDBHelper().execute(conn, sql);
        } catch (Exception e) {
            // TODO 一旦有异常要将整个链路执行都停下来
            throw new RuntimeException("taskname:" + taskname, e);
        }
    }

    public HiveDBUtils getHiveDBHelper() {
        return hiveDBHelper;
    }

    public void setHiveDBHelper(HiveDBUtils hiveDBHelper) {
        this.hiveDBHelper = hiveDBHelper;
    }
}
