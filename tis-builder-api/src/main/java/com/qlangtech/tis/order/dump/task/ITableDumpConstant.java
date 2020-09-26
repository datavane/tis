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
package com.qlangtech.tis.order.dump.task;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public interface ITableDumpConstant {

    // 在导但表时会将数据随机分到16个分区中
    int RAND_GROUP_NUMBER = 16;

    // 最多保存两个历史PT
    int MAX_PARTITION_SAVE = 2;

    String DUMP_START_TIME = "dump_starttime";

    // TableDumpFactory
    String DUMP_TABLE_DUMP_FACTORY_NAME = "dump_table_dump_factory_name";

    String JOB_NAME = "dump_job_name";

    String DUMP_TABLE_NAME = "dump_tableName";

    String DUMP_DBNAME = "dump_db_name";

    // String DUMP_TASK_ID = "dump_taskid";
    String DUMP_FORCE = "dump_force";
}
