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
package com.qlangtech.tis.hdfs.util;

/*
 * @description
 * @since 2011-8-7 05:58:04
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
