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
 * 单表导入,只导入单个表
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年1月29日 下午1:39:30
 */
public class CommonTableDumpTask {

    // extends AbstractTableDumpTask
    public static void main(String[] args) {
    }
    // private static final String SQL_SELECT = "SELECT `id` ,`consume_date`,
    // `action`, `card_id` , `customer_id`, `num` , `status`, `relation_id`,
    // `quantity` , `gift_name`, `dispose_name`, `operator_id` ,
    // `entity_id`,`is_valid`,DATE_FORMAT(from_unixtime(create_time/1000),'%Y%m%d%H%i%s')
    // as create_time ,"
    // + "DATE_FORMAT(from_unixtime(op_time/1000),'%Y%m%d%H%i%s') as op_time ,"
    // + "`last_ver`," + "`active_id`," + "`degree`," + "`pay_id` ,"
    // + " CASE "
    // + " WHEN shop_entity_id IS NULL OR shop_entity_id = '' THEN entity_id "
    // + " ELSE shop_entity_id " + " END AS `shop_entity_id` ,"
    // + "`extend_fields` from `degree_flow`";
    // 
    // private static final String DS_CONFIG = "db.member.enum=10.1.6.101\n"
    // + "db.member.dbname=member\n" + "db.member.username=order\n"
    // + "db.member.password=order@552208";
    // 
    // public static void main(String[] args) {
    // CommonTableDumpTask tableDumpTask = new CommonTableDumpTask();
    // TaskContext taskContext = new TaskContext();
    // SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    // taskContext.setUserParam("dumpstarttime", format.format(new Date()));
    // taskContext.setUserParam("job.name",
    // "search4degreeflow4de-dump_degree_flow");
    // tableDumpTask.map(taskContext);
    // }
    // 
    // @Override
    // protected DBLinkMetaData getDataSourceConfig() {
    // // StringReader reader = new StringReader(DS_CONFIG);
    // // return reader;
    // return null;
    // }
    // 
    // public void initialSpringContext(TaskContext context) {
    // 
    // }
    // 
    // @Override
    // protected void registerExtraBeanDefinition(
    // DefaultListableBeanFactory factory) {
    // }
    // 
    // @Override
    // protected Collection<HdfsRealTimeTerminatorBean> getDumpBeans(
    // TaskContext context) throws Exception {
    // 
    // // 目标表
    // String targetTable = StringUtils
    // .substringBeforeLast(context.getUserParam("job.name"), "-");
    // 
    // RealtimeTerminatorBeanFactory beanFactory = new
    // RealtimeTerminatorBeanFactory();
    // beanFactory.setServiceName(targetTable);
    // beanFactory.setJustDump(false);
    // 
    // MultiThreadHDFSDataProvider hdfsDataProvider = new
    // MultiThreadHDFSDataProvider(
    // MultiThreadHDFSDataProvider.DEFUALT_WAIT_QUEUE_SIZE,
    // MultiThreadHDFSDataProvider.DEFUALT_WAIT_QUEUE_SIZE);
    // 
    // SourceDataProviderFactory dataProviderFactory = new
    // SourceDataProviderFactory();
    // 
    // final Map<String/* ds key */, String/* dump sql */> subTablesDesc = new
    // HashMap<String, String>();
    // 
    // final DBLinkMetaData dbLinkMetaData = parseDbLinkMetaData(context);
    // final Map<String, DataSource> dsMap = new HashMap<String, DataSource>();
    // final StringBuffer dbNames = new StringBuffer();
    // final DBRegister dbRegister = new DBRegister(dbLinkMetaData) {
    // @Override
    // protected void createDefinition(String dbDefinitionId,
    // String driverClassName, String jdbcUrl, String userName,
    // String password) {
    // 
    // //System.out.println("jdbcUrl:" + jdbcUrl);
    // 
    // BasicDataSource ds = new BasicDataSource();
    // ds.setDriverClassName(driverClassName);
    // ds.setUrl(jdbcUrl);
    // ds.setUsername(userName);
    // ds.setPassword(password);
    // ds.setValidationQuery("select 1");
    // dsMap.put(dbDefinitionId, ds);
    // dbNames.append(dbDefinitionId).append(";");
    // }
    // };
    // dbRegister.setApplicationContext();
    // subTablesDesc.put(dbNames.toString(), SQL_SELECT);
    // dataProviderFactory.setSubTablesDesc(subTablesDesc);
    // 
    // dataProviderFactory.setDataSourceGetter(new IDataSourceGetter() {
    // @Override
    // public DataSource getDataSource(String dbKeyName) {
    // return dsMap.get(dbKeyName);
    // }
    // });
    // // dataProviderFactory.init();
    // hdfsDataProvider.setSourceData(dataProviderFactory);
    // 
    // beanFactory.setIncrDumpProvider(new MockHDFSProvider());
    // beanFactory.setFullDumpProvider(hdfsDataProvider);
    // beanFactory.setGrouprouter(null);
    // 
    // beanFactory.afterPropertiesSet();
    // HdfsRealTimeTerminatorBean dumpBean = (HdfsRealTimeTerminatorBean)
    // beanFactory
    // .getObject();
    // 
    // return Collections.singletonList(dumpBean);
    // }
}
