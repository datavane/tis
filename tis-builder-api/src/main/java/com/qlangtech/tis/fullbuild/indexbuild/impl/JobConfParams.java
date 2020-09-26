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
package com.qlangtech.tis.fullbuild.indexbuild.impl;

import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.manage.common.IndexBuildParam;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.order.dump.task.ITableDumpConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-23 14:04
 */
public class JobConfParams {

    private static final Logger logger = LoggerFactory.getLogger(JobConfParams.class);

    private Map<String, String> params = new HashMap<>();

    /**
     * @param table
     * @param startTime
     * @param dumpFactoryName 对应ParamsConfig的名称
     * @return
     */
    public static JobConfParams createTabDumpParams(TaskContext taskContext, IDumpTable table, String startTime, String dumpFactoryName) {
        JobConfParams jobConf = new JobConfParams();
        Objects.requireNonNull(dumpFactoryName, "paramConfigName can not be null");
        final String jobName = table.getDbName() + "." + table.getTableName();
        jobConf.set(ITableDumpConstant.DUMP_TABLE_DUMP_FACTORY_NAME, dumpFactoryName);
        jobConf.set(IndexBuildParam.JOB_TYPE, IndexBuildParam.JOB_TYPE_DUMP);
        jobConf.set(ITableDumpConstant.DUMP_START_TIME, startTime);
        jobConf.set(ITableDumpConstant.JOB_NAME, jobName);
        jobConf.set(ITableDumpConstant.DUMP_TABLE_NAME, table.getTableName());
        jobConf.set(ITableDumpConstant.DUMP_DBNAME, table.getDbName());
        jobConf.set(IParamContext.KEY_TASK_ID, String.valueOf(taskContext.getTaskId()));
        // 有已经导入的数据存在是否有必要重新导入
        jobConf.set(ITableDumpConstant.DUMP_FORCE, "true");
        return jobConf;
    }

    public void set(String key, String value) {
        this.params.put(key, value);
    }

    public String[] paramsArray() {
        List<String> params = paramsList();
        return params.toArray(new String[params.size()]);
    }

    private List<String> paramsList() {
        List<String> plist = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (StringUtils.isBlank(entry.getValue())) {
                continue;
            }
            plist.add("-" + entry.getKey());
            plist.add(entry.getValue());
        // buffer.append(" -").append(entry.getKey()).append(" ").append(entry.getValue());
        }
        return plist;
    // logger.info("main(String[] args),param:" + buffer.toString());
    // return plist.toArray(new String[plist.size()]);
    // return buffer;
    }

    public String paramSerialize() {
        return paramsList().stream().collect(Collectors.joining(" "));
    // StringBuffer buffer = new StringBuffer();
    // for (Map.Entry<String, String> entry : params.entrySet()) {
    // if (StringUtils.isBlank(entry.getValue())) {
    // continue;
    // }
    // buffer.append(" -").append(entry.getKey()).append(" ").append(entry.getValue());
    // }
    // logger.info("main(String[] args),param:" + buffer.toString());
    // return buffer;
    }
}
