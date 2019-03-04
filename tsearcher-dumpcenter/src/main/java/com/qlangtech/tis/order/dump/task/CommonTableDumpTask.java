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

import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import com.qlangtech.tis.common.utils.MockHDFSProvider;
import com.qlangtech.tis.common.utils.RealtimeTerminatorBeanFactory;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.hdfs.client.bean.HdfsRealTimeTerminatorBean;
import com.qlangtech.tis.hdfs.client.data.IDataSourceGetter;
import com.qlangtech.tis.hdfs.client.data.MultiThreadHDFSDataProvider;
import com.qlangtech.tis.hdfs.client.data.SourceDataProviderFactory;
import com.qlangtech.tis.order.dump.task.DataSourceRegister.DBRegister;

/*
 * 单表导入,只导入单个表
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CommonTableDumpTask extends AbstractTableDumpTask {

	private static final String SQL_SELECT = "SELECT `id` ,`consume_date`, `action`, `card_id` , `customer_id`,  `num` ,  `status`,  `relation_id`,  `quantity` ,  `gift_name`,  `dispose_name`,  `operator_id` ,  `entity_id`,`is_valid`,DATE_FORMAT(from_unixtime(create_time/1000),'%Y%m%d%H%i%s') as create_time ,"
			+ "DATE_FORMAT(from_unixtime(op_time/1000),'%Y%m%d%H%i%s') as op_time ," + "`last_ver`," + "`active_id`,"
			+ "`degree`," + "`pay_id` ," + "	CASE "
			+ "  WHEN  shop_entity_id IS NULL OR  shop_entity_id = '' THEN entity_id " + "  ELSE  shop_entity_id "
			+ "  END AS `shop_entity_id` ," + "`extend_fields`  from `degree_flow`";

	private static final String DS_CONFIG = "db.member.enum=10.1.6.101\n" + "db.member.dbname=member\n"
			+ "db.member.username=order\n" + "db.member.password=order@552208";

	public static void main(String[] args) {
		CommonTableDumpTask tableDumpTask = new CommonTableDumpTask();
		TaskContext taskContext = new TaskContext();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		taskContext.setUserParam("dumpstarttime", format.format(new Date()));
		taskContext.setUserParam("job.name", "search4degreeflow4de-dump_degree_flow");
		tableDumpTask.map(taskContext);
	}

	@Override
	protected Reader getDataSourceConfigStream() {
		StringReader reader = new StringReader(DS_CONFIG);
		return reader;
	}

	public void initialSpringContext(TaskContext context) {
	}

	@Override
	protected void registerExtraBeanDefinition(DefaultListableBeanFactory factory) {
	}

	@Override
	protected Collection<HdfsRealTimeTerminatorBean> getDumpBeans(TaskContext context) throws Exception {
		// 目标表
		String targetTable = StringUtils.substringBeforeLast(context.getUserParam("job.name"), "-");
		RealtimeTerminatorBeanFactory beanFactory = new RealtimeTerminatorBeanFactory();
		beanFactory.setServiceName(targetTable);
		beanFactory.setJustDump(false);
		MultiThreadHDFSDataProvider hdfsDataProvider = new MultiThreadHDFSDataProvider(
				MultiThreadHDFSDataProvider.DEFUALT_WAIT_QUEUE_SIZE,
				MultiThreadHDFSDataProvider.DEFUALT_WAIT_QUEUE_SIZE);
		SourceDataProviderFactory dataProviderFactory = new SourceDataProviderFactory();
		final Map<String, String> /* dump sql */
		subTablesDesc = new HashMap<String, String>();
		final List<DBLinkMetaData> dbMetaList = parseDbLinkMetaData(context);
		final Map<String, DataSource> dsMap = new HashMap<String, DataSource>();
		final StringBuffer dbNames = new StringBuffer();
		final DBRegister dbRegister = new DBRegister(dbMetaList) {

			@Override
			protected void createDefinition(String dbDefinitionId, String driverClassName, String jdbcUrl,
					String userName, String password) {
				// System.out.println("jdbcUrl:" + jdbcUrl);
				BasicDataSource ds = new BasicDataSource();
				ds.setDriverClassName(driverClassName);
				ds.setUrl(jdbcUrl);
				ds.setUsername(userName);
				ds.setPassword(password);
				ds.setValidationQuery("select 1");
				dsMap.put(dbDefinitionId, ds);
				dbNames.append(dbDefinitionId).append(";");
			}
		};
		dbRegister.setApplicationContext();
		subTablesDesc.put(dbNames.toString(), SQL_SELECT);
		dataProviderFactory.setSubTablesDesc(subTablesDesc);
		dataProviderFactory.setDataSourceGetter(new IDataSourceGetter() {

			@Override
			public DataSource getDataSource(String dbKeyName) {
				return dsMap.get(dbKeyName);
			}
		});
		// dataProviderFactory.init();
		hdfsDataProvider.setSourceData(dataProviderFactory);
		beanFactory.setIncrDumpProvider(new MockHDFSProvider());
		beanFactory.setFullDumpProvider(hdfsDataProvider);
		beanFactory.setGrouprouter(null);
		beanFactory.afterPropertiesSet();
		HdfsRealTimeTerminatorBean dumpBean = (HdfsRealTimeTerminatorBean) beanFactory.getObject();
		return Collections.singletonList(dumpBean);
	}
}
