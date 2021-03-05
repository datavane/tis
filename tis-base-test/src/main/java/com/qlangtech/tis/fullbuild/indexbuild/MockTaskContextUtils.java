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
package com.qlangtech.tis.fullbuild.indexbuild;

import com.google.common.collect.Maps;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.order.dump.task.ITableDumpConstant;
import com.qlangtech.tis.order.dump.task.ITestDumpCommon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-03 16:29
 */
public class MockTaskContextUtils {

    public static int TEST_TASK_ID = 1234567;
    public static final ThreadLocal<SimpleDateFormat> timeFormatYyyyMMddHHmmss = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            return format;
        }
    };

    public static TaskContext create(Date startTime) {

        final String startTimeStamp = timeFormatYyyyMMddHHmmss.get().format(startTime);

        Map<String, String> params = Maps.newHashMap();

        params.put(ITableDumpConstant.DUMP_START_TIME, startTimeStamp);
        params.put(ITableDumpConstant.JOB_NAME, ITestDumpCommon.DB_EMPLOYEES + "." + ITestDumpCommon.TABLE_EMPLOYEES);
        params.put(ITableDumpConstant.DUMP_TABLE_NAME, ITestDumpCommon.TABLE_EMPLOYEES);
        params.put(ITableDumpConstant.DUMP_DBNAME, ITestDumpCommon.DB_EMPLOYEES);
        params.put(IParamContext.KEY_TASK_ID, String.valueOf(TEST_TASK_ID));
        // 有已经导入的数据存在是否有必要重新导入
        params.put(ITableDumpConstant.DUMP_FORCE, "true");
        return TaskContext.create(params);
    }
}
