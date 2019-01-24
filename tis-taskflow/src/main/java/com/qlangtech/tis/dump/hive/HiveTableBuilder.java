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

import static java.sql.Types.BIGINT;
import static java.sql.Types.BIT;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.REAL;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TINYINT;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;

/*
 * 在hive中生成新表，和在新表上创建创建Partition
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HiveTableBuilder {

    private static final Logger log = LoggerFactory.getLogger(HiveTableBuilder.class);

    public static String HIVE_TYPE_STRING = "STRING";

    private final String timestamp;

    private final String user;

    private HiveDBUtils hiveDbHeler;

    private FileSystem fileSystem;

    public HiveTableBuilder(String timestamp, String user) {
        super();
        this.timestamp = timestamp;
        this.user = user;
    }

    /**
     * @param hiveDbHeler
     * @return
     * @throws Exception
     */
    public static List<String> getExistTables(Connection conn, final HiveDBUtils hiveDbHeler) throws Exception {
        final List<String> tables = new ArrayList<>();
        hiveDbHeler.query(conn, "show tables", new HiveDBUtils.ResultProcess() {

            @Override
            public void callback(ResultSet result) throws Exception {
                tables.add(result.getString(1));
            }
        });
        return tables;
    }

    public static void createHiveTable(Connection conn, String tableName, List<HiveColumn> cols, SQLCommandTailAppend sqlCommandTailAppend) throws Exception {
        createHiveTable(conn, tableName, cols, sqlCommandTailAppend, "\\t");
    }

    /**
     * @param conn
     * @param tableName
     * @param cols
     * @param sqlCommandTailAppend
     * @throws Exception
     */
    public static void createHiveTable(Connection conn, String tableName, List<HiveColumn> cols, SQLCommandTailAppend sqlCommandTailAppend, String colSplit) throws Exception {
        if (StringUtils.isBlank(colSplit)) {
            throw new IllegalArgumentException("param colSplit can not be null");
        }
        int maxColLength = 0;
        int tmpLength = 0;
        for (HiveColumn c : cols) {
            // .length();
            tmpLength = StringUtils.length(c.getName());
            if (tmpLength < 1) {
                throw new IllegalStateException("col name length can not small than 1,cols size:" + cols.size());
            }
            if (tmpLength > maxColLength) {
                maxColLength = tmpLength;
            }
        }
        HiveColumn o = null;
        String colformat = "%-" + (++maxColLength) + "s";
        StringBuffer hiveSQl = new StringBuffer();
        hiveSQl.append("CREATE EXTERNAL TABLE IF NOT EXISTS `" + tableName + "` (\n");
        final int colsSize = cols.size();
        for (int i = 0; i < colsSize; i++) {
            o = cols.get(i);
            if (i != o.getIndex()) {
                throw new IllegalStateException("i:" + i + " shall equal with index:" + o.getIndex());
            }
            hiveSQl.append("  ").append("`").append(String.format(colformat, o.getName() + '`')).append(" ").append(o.getType());
            if ((i + 1) < colsSize) {
                hiveSQl.append(",");
            }
            hiveSQl.append("\n");
        }
        hiveSQl.append(") COMMENT 'tis_hive_tmp_" + tableName + "' PARTITIONED BY(pt string,pmod string) ROW FORMAT DELIMITED FIELDS TERMINATED BY '" + colSplit + "' LINES TERMINATED BY '\\n' NULL DEFINED AS '' STORED AS TEXTFILE");
        sqlCommandTailAppend.append(hiveSQl);
        log.info(hiveSQl.toString());
        HiveDBUtils.getInstance().execute(conn, hiveSQl.toString());
    }

    private static String getHiveType(int type) {
        switch(type) {
            case BIT:
            case TINYINT:
            case SMALLINT:
            case INTEGER:
                return "INT";
            case BIGINT:
                return "BIGINT";
            case FLOAT:
            case REAL:
            case DOUBLE:
            case NUMERIC:
            case DECIMAL:
                return "DOUBLE";
            default:
                return HIVE_TYPE_STRING;
        }
    }

    /**
     * 构建hive中的表
     *
     * @param indexName
     * @throws Exception
     */
    public void createHiveTable(Connection conn, String indexName) throws Exception {
        buildTableDDL(conn, indexName, timestamp, StringUtils.substringAfter(indexName, "search4"));
    }

    /**
     * 和hdfs上已经导入的数据进行绑定
     *
     * @param hiveTables
     * @throws Exception
     */
    public void bindHiveTables(FileSystem fileSystem, Set<String> hiveTables, final String userName) throws Exception {
        Connection conn = null;
        try {
            conn = hiveDbHeler.createConnection();
            final List<String> tables = getExistTables(conn, hiveDbHeler);
            for (String hiveTable : hiveTables) {
                String tableName = StringUtils.substringAfter(hiveTable, "search4");
                List<HiveColumn> columns = getColumns(hiveTable, timestamp);
                if (tables.contains(tableName)) {
                    if (isTableSame(conn, columns, tableName)) {
                        // 需要清空原来表数据
                        HiveRemoveHistoryDataTask hiveHistoryClear = new HiveRemoveHistoryDataTask(tableName, userName, fileSystem);
                        hiveHistoryClear.dropHistoryHiveTable(conn);
                    } else {
                        // 原表有改动，需要把表drop掉
                        HiveDBUtils.getInstance().execute(conn, "drop table " + tableName);
                        this.createHiveTable(conn, columns, tableName);
                    }
                } else {
                    this.createHiveTable(conn, columns, tableName);
                }
                // 生成 hive partitiion
                this.createTablePartition(conn, hiveTable);
            }
        } finally {
            try {
                conn.close();
            } catch (Throwable e) {
            }
        }
    }

    public static boolean isTableSame(Connection conn, List<HiveColumn> columns, String tableName) {
        boolean isTableSame;
        try {
            final StringBuffer errMsg = new StringBuffer();
            final StringBuffer equalsCols = new StringBuffer("compar equals:");
            final AtomicBoolean compareOver = new AtomicBoolean(false);
            HiveDBUtils.getInstance().query(conn, "desc " + tableName, new HiveDBUtils.ResultProcess() {

                int index = 0;

                @Override
                public void callback(ResultSet result) throws Exception {
                    if (errMsg.length() > 0) {
                        return;
                    }
                    final String keyName = result.getString(1);
                    if (compareOver.get() || (StringUtils.isBlank(keyName) && compareOver.compareAndSet(false, true))) {
                        // 所有列都解析完成
                        return;
                    }
                    if (index > (columns.size() - 1)) {
                        errMsg.append("create table " + tableName + " col:" + keyName + " is not exist");
                        return;
                    }
                    HiveColumn column = columns.get(index++);
                    if (column.getIndex() != (index - 1)) {
                        throw new IllegalStateException("col:" + column.getName() + " index shall be " + (index - 1) + " but is " + column.getIndex());
                    }
                    if (!StringUtils.equals(keyName, column.getName())) {
                        errMsg.append("target tanle keyName:" + keyName + " is not equal with source table col:" + column.getName());
                    } else {
                        equalsCols.append(keyName).append(",");
                    }
                }
            });
            if (errMsg.length() > 0) {
                isTableSame = false;
                log.warn("create table has been modify,error:" + errMsg);
                log.warn(equalsCols.toString());
            } else {
                // 没有改动，不过需要把元表清空
                isTableSame = true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return isTableSame;
    }

    /**
     * 创建表分区
     *
     * @param indexName
     * @throws Exception
     */
    public void createTablePartition(Connection conn, String indexName) throws Exception {
        String tableName = null;
        tableName = StringUtils.substringAfter(indexName, "search4");
        int index = 0;
        String sql = null;
        Path path = null;
        while (true) {
            path = new Path("/user/" + user + "/" + indexName + "/all/" + index + "/" + (timestamp));
            if (!fileSystem.exists(path)) {
                break;
            }
            sql = "alter table " + tableName + " add if not exists partition(pt='" + timestamp + "',pmod='" + index + "') location '" + TSearcherConfigFetcher.get().getHdfsAddress() + "/user/" + user + "/" + indexName + "/all/" + index + "/" + timestamp + "'";
            log.info(sql);
            hiveDbHeler.execute(conn, sql);
            index++;
        }
    }

    private List<HiveColumn> getColumns(String collection, String timestamp) throws IOException {
        InputStream input = null;
        List<HiveColumn> cols = new ArrayList<>();
        try {
            Path path = new Path("/user/" + user + "/" + collection + "/all/" + timestamp + "/cols-metadata");
            input = fileSystem.open(path);
            String content = IOUtils.toString(input);
            JSONArray array = (JSONArray) JSON.parse(content);
            for (Object anArray : array) {
                JSONObject o = (JSONObject) anArray;
                HiveColumn col = new HiveColumn();
                col.setName(o.getString("key"));
                col.setIndex(o.getIntValue("index"));
                col.setType(getHiveType(o.getIntValue("type")));
                cols.add(col);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
        return cols;
    }

    /**
     * @param conn
     * @param collection
     * @param timestamp
     * @param tableName
     * @throws IOException
     */
    private void buildTableDDL(Connection conn, String collection, String timestamp, String tableName) throws Exception {
        createHiveTable(conn, tableName, getColumns(collection, timestamp), new SQLCommandTailAppend() {

            @Override
            public void append(StringBuffer hiveSQl) {
            }
        });
    }

    private void createHiveTable(Connection conn, List<HiveColumn> columns, String tableName) throws Exception {
        createHiveTable(conn, tableName, columns, new SQLCommandTailAppend() {

            @Override
            public void append(StringBuffer hiveSQl) {
            }
        });
    }

    public HiveDBUtils getHiveDbHeler() {
        return hiveDbHeler;
    }

    public void setHiveDbHeler(HiveDBUtils hiveDbHeler) {
        this.hiveDbHeler = hiveDbHeler;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public abstract static class SQLCommandTailAppend {

        public abstract void append(StringBuffer hiveSQl);
    }
}
