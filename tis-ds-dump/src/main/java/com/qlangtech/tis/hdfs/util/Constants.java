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
package com.qlangtech.tis.hdfs.util;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Constants {

    public static final String DEFAULT_PROVIDER_NAME = "DEFAULT_PROVIDER_NAME";

    public static final String DEFAULT_SERVICE_NAME = "DEFAULT_SERVICE_NAME";

    public static final String IMPORT_HDFS_ERROR = "IMPORT_ERROR";

    public static final String EONSUMER_HDFS_ERROR = "EONSUMER_ERROR";

    public static final String IMPORT_HDFS_ROW_COUNT = "IMPORT_HDFS_ROW_COUNT";

    public static final String EONSUMER_HDFS_ROW_COUNT = "EONSUMER_HDFS_ROW_COUNT";

    public static final String IS_INCR = "IS_INCR";

    public static final String DEFAULT_APP_ID = "terminator-hdfs";

    public static final String ALL_DUMP = "alldump";

    public static final String INCR_DUMP = "incrdump";

    // 塞在result中标志是否要执行jobhook
    public static final String SHALL_DO_JOB_HOOK = "shalldojobhook";

    public static final String SHARD_COUNT = "shardcount";

    public static final String IMPORT_COUNT = "importcount";

    public static final String LOCK = "LOCK";

    public static final String WAIT_TASK_TIME = "WAIT_TASK_TIME";

    public static final String DEFAULT_TASK_NAME = "hdfs-import-task";

    public static final String DEFAULT_HDFS_ALL_CAPABILITY = DEFAULT_APP_ID + "/" + DEFAULT_TASK_NAME + "/" + ALL_DUMP;

    public static final String DEFAULT_HDFS_INCR_CAPABILITY = DEFAULT_APP_ID + "/" + DEFAULT_TASK_NAME + "/" + INCR_DUMP;

    public static final String TAB = "\t";

    public static final String NEWLINE = "\r\n";

    public static final String DATA_OPT = "opt";

    public static final String DEL_OPT = "d";

    public static final String ADD_OPT = "i";

    public static final String UPDATE_OPT = "u";

    public static final String OPT_COLUMN = "opt";

    public static final String DEL_ID = "delId";

    public static final String BOOST_COLUMN = "boot";

    public static final String SUFFIX_HDFS_FILE = ".txt";

    public static final String TIME_POINT = "TIME_POINT";

    public static final String EXECUTED_TASK = "EXECUTED_TASK";

    public static final String INDEX_TYPE_MR = "MR";

    public static final String INDEX_TYPE_NOR = "NOR";

    public static final String PATHFILTER = "pathFilter";

    public static final String SPILTSIZE = "spiltsize";

    public static final char EOL = '\n';

    public static final String NEW_CORE = "newCore";

    public static final String SOLR_CORE = "solrCore";

    public static final String USER = "user";

    public static final String TIME = "time";

    public static final String TIMEFILENAME = "import_time.inc";
}
