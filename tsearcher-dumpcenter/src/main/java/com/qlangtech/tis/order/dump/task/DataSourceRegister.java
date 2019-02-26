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
package com.qlangtech.tis.order.dump.task;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.qlangtech.tis.order.dump.task.AbstractTableDumpTask.DBLinkMetaData;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DataSourceRegister implements ApplicationContextAware, InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(DataSourceRegister.class);

	public static void main(String[] args) throws Exception {
		InetAddress address = InetAddress.getByName("10.1.7.26");
		System.out.println(address.getHostAddress());
	}

	// daily test
	// private static final String[] dbinfos = new String[] { "10.1.6.101_1_32",
	// "10.1.6.102_33_64", "10.1.6.103_65_96", "10.1.6.104_97_128" };
	private String processDbs;

	public String getProcessDbs() {
		return processDbs;
	}

	public void setProcessDbs(String processDbs) {
		this.processDbs = processDbs;
	}

	public abstract static class DBRegister {

		private final List<DBLinkMetaData> dbMetaList;

		public DBRegister(List<DBLinkMetaData> dbMetaList) {
			this.dbMetaList = dbMetaList;
		}

		private void createDBLinkDefinition(String ip, DBLinkMetaData dbsMeta, int i) {
			final String dbName = dbsMeta.getDbName() + ((i > -1) ? i : StringUtils.EMPTY);
			String jdbcUrl = "jdbc:mysql://" + ip + ":" + dbsMeta.getPort() + "/" + dbName
					+ "?useUnicode=yes&amp;characterEncoding=utf8";
			final String dbDefinitionId = dbsMeta.getDbDefineId() + ((i > -1) ? i : StringUtils.EMPTY);
			log.info("create dbbean:" + dbDefinitionId + ",jdbc url:" + jdbcUrl);
			createDefinition(dbDefinitionId, "com.mysql.jdbc.Driver", jdbcUrl, dbsMeta.getUserName(),
					dbsMeta.getPassword());
		}

		protected abstract void createDefinition(String dbDefinitionId, String driverClassName, String jdbcUrl,
				String userName, String password);

		public void setApplicationContext() throws BeansException {
			String[] splitInfo = null;
			String ip = null;
			int startDb, endDb;
			for (DBLinkMetaData dbsMeta : dbMetaList) {
				for (String dbs : StringUtils.split(dbsMeta.getHostEmnu(), ",")) {
					splitInfo = StringUtils.split(dbs, "_");
					if (splitInfo.length == 1) {
						ip = dbs;
						createDBLinkDefinition(getHostIpAddress(ip), dbsMeta, -1);
					} else if (splitInfo.length == 2) {
						ip = getHostIpAddress(splitInfo[0]);
						startDb = Integer.parseInt(splitInfo[1]);
						createDBLinkDefinition(ip, dbsMeta, startDb);
					} else if (splitInfo.length == 3) {
						ip = getHostIpAddress(splitInfo[0]);
						startDb = Integer.parseInt(splitInfo[1]);
						endDb = Integer.parseInt(splitInfo[2]);
						for (int i = startDb; i <= endDb; i++) {
							createDBLinkDefinition(ip, dbsMeta, i);
						}
					}
				}
			}
		}

		/**
		 * 将host转成IP地址
		 *
		 * @param ip
		 * @return
		 */
		protected String getHostIpAddress(String ip) {
			try {
				InetAddress address = InetAddress.getByName(ip);
				return address.getHostAddress();
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
		}
	}

	// private static final String userName = "order";
	// private static final String password = "order@552208";
	// static void setApplicationContext(DefaultListableBeanFactory factory,
	// List<DBLinkMetaData> dbMetaList) throws BeansException {
	// DefaultListableBeanFactory beanFactory = factory;
	//
	// String[] splitInfo = null;
	// String ip = null;
	// int startDb, endDb;
	//
	// for (DBLinkMetaData dbsMeta : dbMetaList) {
	//
	// for (String dbs : StringUtils.split(dbsMeta.getHostEmnu(), ",")) {
	// splitInfo = StringUtils.split(dbs, "_");
	// if (splitInfo.length == 1) {
	// ip = dbs;
	// createDBLinkDefinition(beanFactory, ip, dbsMeta, -1);
	// } else if (splitInfo.length == 3) {
	// ip = splitInfo[0];
	// startDb = Integer.parseInt(splitInfo[1]);
	// endDb = Integer.parseInt(splitInfo[2]);
	// for (int i = startDb; i <= endDb; i++) {
	// createDBLinkDefinition(beanFactory, ip, dbsMeta, i);
	// }
	// }
	// }
	// }
	// }
	// /**
	// * @param beanFactory
	// * @param ip
	// * @param dbsMeta
	// * @param i
	// */
	// private static void createDBLinkDefinition(
	// DefaultListableBeanFactory beanFactory, String ip,
	// DBLinkMetaData dbsMeta, int i) {
	//
	// }
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 校验bean 是否正确
		String[] dbs = StringUtils.split(processDbs, ";");
		DataSource ds = null;
		StringBuffer buffer = new StringBuffer();
		for (String db : dbs) {
			ds = (DataSource) applicationContext.getBean(db);
			if (ds == null) {
				throw new IllegalStateException("db:" + db + " is not defined");
			}
			if (!isOK(ds, buffer)) {
				buffer.append(db).append(",");
			}
		}
		if (buffer.length() > 0) {
			log.error("db config has some error," + buffer);
			throw new IllegalStateException("db has some error:" + buffer.toString());
		}
	}

	private boolean hasRrcordDbError = false;

	/**
	 * @param ds
	 * @throws SQLException
	 */
	private boolean isOK(DataSource ds, StringBuffer errorRecord) throws SQLException {
		try {
			Connection conn;
			Statement statement;
			ResultSet result;
			conn = ds.getConnection();
			statement = conn.createStatement();
			result = statement.executeQuery("select 1");
			if (result.next()) {
				if (result.getInt(1) != 1) {
					// "result shall be 1 but not");
					return false;
				}
			} else {
				// throw new IllegalStateException("can not get result");
				return false;
			}
			result.close();
			statement.close();
			conn.close();
			return true;
		} catch (Exception e) {
			if (!hasRrcordDbError) {
				errorRecord.append(ExceptionUtils.getStackTrace(e));
				hasRrcordDbError = true;
			}
			return false;
		}
	}
}
