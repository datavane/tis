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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.dump.hive.HiveDBUtils.ResultProcess;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HiveRemoveHistoryDataTask {

	private static final Logger log = LoggerFactory.getLogger(HiveRemoveHistoryDataTask.class);

	private final String tableName;

	private static final int MAX_PARTITION_SAVE = 1;

	// daily ps name
	private final String pt = "pt";

	private final String userName;

	private final FileSystem fileSystem;

	// private final Connection hiveConnection;
	public static void main(String[] arg) {
		// Matcher matcher = INDEX_NAME_PATTERN.matcher("4xxxxx");
		// if (!matcher.matches()) {
		// throw new IllegalStateException(
		// "table name is not illegal,tableName:");
		// }
		//
		// System.out.println(matcher.group(2));
		List<PathInfo> timestampList = new ArrayList<PathInfo>();
		PathInfo path = null;
		for (int i = 0; i < 100; i++) {
			path = new PathInfo();
			path.setTimeStamp(i);
			timestampList.add(path);
		}
		sortTimestamp(timestampList);
		for (PathInfo info : timestampList) {
			System.out.println(info.timeStamp);
		}
	}

	public HiveRemoveHistoryDataTask(// ,
			String tableName, // ,
			String userName, // ,
			FileSystem fileSystem) // Connection
	// hiveConnection
	{
		super();
		if (StringUtils.isBlank(tableName)) {
			throw new IllegalArgumentException("tableName can not be null");
		}
		this.tableName = tableName;
		this.userName = userName;
		this.fileSystem = fileSystem;
		// this.hiveConnection = hiveConnection;
	}

	public String getTableName() {
		return tableName;
	}

	// 20160106131304
	public static final Pattern DATE_PATTERN = Pattern.compile("20\\d{12}");

	/**
	 * @param execConetxt
	 * @param t
	 * @throws Exception
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void deleteHdfsHistoryFile(Connection hiveConnection) throws Exception {
		this.deleteMetadata();
		this.deleteHdfsFile(false);
		// 索引数据: /search4totalpay/all/0/output/20160104003306
		this.deleteHdfsFile(true);
		this.dropHistoryHiveTable(hiveConnection);
	}

	/**
	 * 删除dump的metadata<br>
	 * example:/search4customerregistercard/all/20160106131304
	 *
	 * @param execConetxt
	 */
	private void deleteMetadata() throws Exception {
		String hdfsPath = getJoinTableStorePath(this.userName, this.getTableName()) + "/all";
		FileSystem fileSys = this.fileSystem;
		Path parent = new Path(hdfsPath);
		if (!fileSys.exists(parent)) {
			return;
		}
		FileStatus[] child = fileSys.listStatus(parent);
		List<PathInfo> timestampList = new ArrayList<PathInfo>();
		PathInfo pathinfo = null;
		Matcher matcher = null;
		for (FileStatus c : child) {
			matcher = DATE_PATTERN.matcher(c.getPath().getName());
			if (matcher.matches()) {
				pathinfo = new PathInfo();
				pathinfo.pathName = c.getPath().getName();
				pathinfo.timeStamp = Long.parseLong(matcher.group());
				timestampList.add(pathinfo);
			}
		}
		deleteOldHdfsfile(fileSys, parent, timestampList);
	}

	/**
	 * @param fileSys
	 * @param parent
	 * @param timestampList
	 * @throws IOException
	 */
	public static void deleteOldHdfsfile(FileSystem fileSys, Path parent, List<PathInfo> timestampList)
			throws IOException {
		sortTimestamp(timestampList);
		Path toDelete = null;
		for (int index = (MAX_PARTITION_SAVE); index < timestampList.size(); index++) {
			toDelete = new Path(parent, timestampList.get(index).pathName);
			// 删除历史数据
			log.info("history old hdfs file path:" + toDelete.toString() + " delete,success:"
					+ fileSys.delete(toDelete, true) + ",getMaxPartitionSave:" + MAX_PARTITION_SAVE);
		}
	}

	public static class PathInfo {

		private long timeStamp;

		private String pathName;

		public long getTimeStamp() {
			return timeStamp;
		}

		public void setTimeStamp(long timeStamp) {
			this.timeStamp = timeStamp;
		}

		public String getPathName() {
			return pathName;
		}

		public void setPathName(String pathName) {
			this.pathName = pathName;
		}
	}

	/**
	 * 删除历史索引build文件
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void removeHistoryBuildFile() throws IOException, FileNotFoundException {
		this.deleteHdfsFile(true);
	}

	/**
	 * 删除hdfs中的文件
	 *
	 * @param execConetxt
	 * @param fileSys
	 * @param t
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void deleteHdfsFile(boolean isBuildFile) throws IOException, FileNotFoundException {
		String hdfsPath = null;
		// dump数据:/search4totalpay/all/0/20160105003307
		hdfsPath = getJoinTableStorePath(this.userName, this.getTableName()) + "/all";
		FileSystem fileSys = this.fileSystem;
		int group = 0;
		List<PathInfo> dumptimestamps = null;
		PathInfo pathinfo = null;
		FileStatus[] child = null;
		Path parent = null;
		while (true) {
			parent = new Path(hdfsPath + "/" + (group++));
			if (isBuildFile) {
				parent = new Path(parent.toString(), "output");
			}
			if (!fileSys.exists(parent)) {
				break;
			}
			child = fileSys.listStatus(parent);
			dumptimestamps = new ArrayList<PathInfo>();
			pathinfo = new PathInfo();
			for (FileStatus f : child) {
				try {
					pathinfo = new PathInfo();
					pathinfo.pathName = f.getPath().getName();
					pathinfo.timeStamp = Long.parseLong(f.getPath().getName());
					dumptimestamps.add(pathinfo);
				} catch (Throwable e) {
				}
			}
			deleteOldHdfsfile(fileSys, parent, dumptimestamps);
		}
	}

	/**
	 * @param dumptimestamps
	 */
	private static void sortTimestamp(List<PathInfo> timestampList) {
		// 最大的应该的index为0的位置上
		Collections.sort(timestampList, new Comparator<PathInfo>() {

			@Override
			public int compare(PathInfo o1, PathInfo o2) {
				return (int) (o2.timeStamp - o1.timeStamp);
			}
		});
	}

	private static final Pattern INDEX_NAME_PATTERN = Pattern.compile("(search4)?(.+)");

	/**
	 * 删除hive中的历史表
	 */
	public void dropHistoryHiveTable(Connection hiveConn) {
		Matcher matcher = INDEX_NAME_PATTERN.matcher(this.tableName);
		if (!matcher.matches()) {
			throw new IllegalStateException("table name is not illegal,tableName:" + this.tableName);
		}
		final String hiveTableName = matcher.group(2);
		if (StringUtils.isBlank(pt)) {
			throw new IllegalStateException("pt name shall be set");
		}
		// HiveTaskFactory.getConnection(this.getContext());
		Connection conn = hiveConn;
		final Set<String> ptSet = new HashSet<String>();
		try {
			final AtomicBoolean isExist = new AtomicBoolean(false);
			// 判断表是否存在
			String showTables = "show tables like '" + hiveTableName + "'";
			HiveDBUtils.getInstance().query(conn, showTables, new ResultProcess() {

				@Override
				public void callback(ResultSet result) throws Exception {
					isExist.set(true);
				}
			});
			if (!isExist.get()) {
				// 表不存在
				log.info(hiveTableName + " is not exist");
				return;
			}
			final String showPartition = "show partitions " + hiveTableName;
			final Pattern ptPattern = Pattern.compile(pt + "=(\\d+)");
			HiveDBUtils.getInstance().query(conn, showPartition, new ResultProcess() {

				@Override
				public void callback(ResultSet result) throws Exception {
					Matcher matcher = ptPattern.matcher(result.getString(1));
					if (matcher.find()) {
						ptSet.add(matcher.group(1));
					} else {
						log.warn(hiveTableName + ",partition" + result.getString(1) + ",is not match pattern:"
								+ ptPattern);
					}
					//
				}
			});
			List<String> ptList = new LinkedList<>(ptSet);
			Collections.sort(ptList);
			int count = 0;
			log.info("maxPartitionSave:" + MAX_PARTITION_SAVE);
			for (int i = ptList.size() - 1; i >= 0; i--) {
				if ((++count) > MAX_PARTITION_SAVE) {
					String alterSql = "alter table " + hiveTableName + " drop partition (  " + pt + " = '"
							+ ptList.get(i) + "' )";
					try {
						HiveDBUtils.getInstance().execute(conn, alterSql);
					} catch (Throwable e) {
						log.error("alterSql:" + alterSql, e);
					}
					log.info("history table:" + hiveTableName + ", partition:" + this.pt + "='" + ptList.get(i)
							+ "', have been removed");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getJoinTableStorePath(String user, String targetTableName) {
		return TSearcherConfigFetcher.get().getHDFSRootDir() + "/" + targetTableName;
	}

	public String getPt() {
		return pt;
	}
	// /**
	// * 这个标记位表示在绑定表的时候 是否需要绑定hive表，因为宽表是不需要绑定的
	// *
	// * @return
	// */
	// public boolean isBindtable() {
	// return bindtable;
	// }
}
