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

import java.io.IOException;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.parse.ParseException;
import com.qlangtech.tis.dump.hive.HiveColumn;
import com.qlangtech.tis.dump.hive.HiveDBUtils;
import com.qlangtech.tis.dump.hive.HiveRemoveHistoryDataTask;
import com.qlangtech.tis.dump.hive.HiveTableBuilder;
import com.qlangtech.tis.dump.hive.HiveTableBuilder.SQLCommandTailAppend;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.taskflow.TemplateContext;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JoinHiveTask extends HiveTask {

	// private static final Logger log =
	// LoggerFactory.getLogger(JoinHiveTask.class);
	@Override
	protected void executeSql(String taskName, String sql) {
		processJoinTask(sql);
		super.executeSql(taskName, sql);
	}

	/**
	 * 处理join表，是否需要自动创建表或者删除重新创建表
	 *
	 * @param sql
	 */
	protected void processJoinTask(String sql) {
		try {
			final HiveInsertFromSelectParser insertParser = getSQLParserResult(sql);
			final String targetTableName = insertParser.getTargetTableName();
			final List<HiveColumn> cols = insertParser.getCols();
			final List<HiveColumn> excludePartitionCols = insertParser.getColsExcludePartitionCols();
			processJoinTask(targetTableName, targetTableName, cols, excludePartitionCols);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// final Connection conn =
		// HiveTaskFactory.getConnection(this.getContext());
		// // hive database中已经存在的表
		// List<String> existTabs = HiveTableBuilder.getExistTables(conn,
		// this.getHiveDBHelper());
		//
		// if (existTabs.contains(targetTableName)) {
		//
		// if (HiveTableBuilder.isTableSame(conn, cols, targetTableName)) {
		// // 表结构没有变化，需要清理表中的历史数据
		// RemoveJoinHistoryDataTask historyJoinTableClear = new
		// RemoveJoinHistoryDataTask();
		// // 清理历史hdfs数据
		// historyJoinTableClear.deleteHistoryJoinTable(targetTableName,
		// this.getContext().getParams());
		// // 清理hive数据
		// // (String tableName, String userName,
		// // FileSystem fileSystem, Connection hiveConnection
		// IExecChainContext param = this.getContext().getParams();
		// HiveRemoveHistoryDataTask hiveHistoryClear = new
		// HiveRemoveHistoryDataTask(targetTableName,
		// param.getContextUserName(), param.getDistributeFileSystem());
		// hiveHistoryClear.dropHistoryHiveTable(conn);
		// } else {
		// HiveDBUtils.getInstance().execute(conn, "drop table " +
		// targetTableName);
		// createHiveTable(targetTableName, excludePartitionCols, conn);
		// }
		// } else {
		// // insertParser.getPs();
		// // 直接创建
		// createHiveTable(targetTableName, excludePartitionCols, conn);
		// }
		//
		// } catch (Exception e) {
		// throw new RuntimeException(e);
		// }
	}

	/**
	 * 处理join表，是否需要自动创建表或者删除重新创建表
	 *
	 * @param originTableName
	 *            历史表名称
	 *
	 * @param targetTableName
	 *            目标表名称
	 * @param cols
	 *            目标表的列，包括pt列
	 * @param excludePartitionCols
	 *            目标表的列，排除pt列
	 */
	public void processJoinTask(String originTableName, String targetTableName, List<HiveColumn> cols,
			List<HiveColumn> excludePartitionCols) {
		try {
			final Connection conn = HiveTaskFactory.getConnection(this.getContext());
			// hive database中已经存在的表
			List<String> existTabs = HiveTableBuilder.getExistTables(conn, this.getHiveDBHelper());
			if (existTabs.contains(targetTableName)) {
				if (HiveTableBuilder.isTableSame(conn, cols, targetTableName)) {
					// 表结构没有变化，需要清理表中的历史数据
					RemoveJoinHistoryDataTask historyJoinTableClear = new RemoveJoinHistoryDataTask();
					// 清理历史hdfs数据
					historyJoinTableClear.deleteHistoryJoinTable(targetTableName, this.getContext().getParams());
					// 清理hive数据
					// (String tableName, String userName,
					// FileSystem fileSystem, Connection hiveConnection
					IExecChainContext param = this.getContext().getParams();
					HiveRemoveHistoryDataTask hiveHistoryClear = new HiveRemoveHistoryDataTask(targetTableName,
							param.getContextUserName(), param.getDistributeFileSystem());
					hiveHistoryClear.dropHistoryHiveTable(conn);
				} else {
					HiveDBUtils.getInstance().execute(conn, "drop table " + targetTableName);
					createHiveTable(targetTableName, excludePartitionCols, conn);
				}
			} else {
				// insertParser.getPs();
				// 直接创建
				createHiveTable(targetTableName, excludePartitionCols, conn);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public HiveInsertFromSelectParser getSQLParserResult(TemplateContext tplContext) throws Exception {
		this.setContext(tplContext);
		return this.getSQLParserResult(mergeVelocityTemplate(Collections.emptyMap()));
	}

	/**
	 * @param sql
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private HiveInsertFromSelectParser getSQLParserResult(String sql) throws IOException, ParseException {
		final HiveInsertFromSelectParser insertParser = new HiveInsertFromSelectParser();
		insertParser.start(sql);
		return insertParser;
	}

	/**
	 * 创建hive表
	 *
	 * @param insertParser
	 * @param conn
	 * @throws Exception
	 */
	private void createHiveTable(String targetTableName, List<HiveColumn> cols, Connection conn) throws Exception {
		final String user = this.getContext().getParams().getContextUserName();
		final String newTabColSplitChar = StringUtils.defaultIfBlank(
				this.getContext().getParams().getString(TemplateContext.COL_SPLIT_KEY), TemplateContext.COL_SPLIT_TAB);
		HiveTableBuilder.createHiveTable(conn, targetTableName, cols, // insertParser.getColsExcludePartitionCols()
				new SQLCommandTailAppend() {

					@Override
					public void append(StringBuffer hiveSQl) {
						TSearcherConfigFetcher config = TSearcherConfigFetcher.get();
						hiveSQl.append("\n LOCATION '").append(config.getHdfsAddress())
								.append(HiveRemoveHistoryDataTask.getJoinTableStorePath(user, targetTableName));
						hiveSQl.append("'");
					}
				}, newTabColSplitChar);
	}

}
