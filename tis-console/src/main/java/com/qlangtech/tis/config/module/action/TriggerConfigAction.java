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
package com.qlangtech.tis.config.module.action;

import com.qlangtech.tis.runtime.module.action.BasicModule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class TriggerConfigAction extends BasicModule {
    // private static final long serialVersionUID = 1L;
    // 
    // private static final Pattern tablePattern = Pattern
    // .compile("(.+?)(\\[(\\d{4})\\-(\\d{4})\\])?");
    // 
    // private final SolrFieldsParser solrFieldsParser = new SolrFieldsParser();
    // 
    // public static void main(String[] arg) {
    // 
    // // Matcher m = tablePattern.matcher();
    // // List<String> tabs = parseTableList("auction_attribute_extra_");
    // // for (String tab : tabs) {
    // // System.out.println(tab);
    // // }
    // 
    // SimpleDateFormat dateFormat = new SimpleDateFormat("20141105");
    // System.out.println(dateFormat.format(new Date()));
    // }
    // 
    // private static List<String> parseTableList(String tabpattern) {
    // List<String> result = new ArrayList<String>();
    // 
    // Matcher m = tablePattern.matcher(tabpattern);
    // String tableLogicName = null;
    // int start = -1;
    // int to = -1;
    // if (m.matches()) {
    // tableLogicName = m.group(1);
    // try {
    // start = Integer.parseInt(m.group(3));
    // } catch (Throwable e) {
    // 
    // }
    // try {
    // to = Integer.parseInt(m.group(4));
    // } catch (Throwable e) {
    // 
    // }
    // } else {
    // return result;
    // }
    // 
    // if (!(start > -1 && to > -1)) {
    // result.add(tableLogicName);
    // return result;
    // }
    // 
    // for (int i = start; i <= to; i++) {
    // result.add(tableLogicName + String.format("%04d", i));
    // }
    // 
    // return result;
    // }
    // 
    // //	/**
    // //	 * 取得当前选中的快照资源集合
    // //	 *
    // //	 * @param appid
    // //	 * @return
    // //	 */
    // //	public SnapshotDomain getSnapshotDomain(Integer appid) {
    // //
    // //		final ServerGroup group = DownloadServlet.getServerGroup(appid,
    // //				(short) AddAppAction.FIRST_GROUP_INDEX, ManageUtils
    // //						.getRuntime().getId(), this.getServerGroupDAO());
    // //
    // //		if (group == null || group.getPublishSnapshotId() == null) {
    // //			throw new IllegalStateException(
    // //					" relevant ServerGroup is null or publishSnapshot is null,appid:"
    // //							+ appid);
    // //		}
    // //
    // //		SnapshotDomain snapshot = this.getSnapshotViewDAO().getView(
    // //				group.getPublishSnapshotId());
    // //
    // //		return snapshot;
    // //	}
    // 
    // private static final int ADVANCE_SOURCE_XML_DEFINE_FLAG = -1;
    // 
    // /**
    // * 取得高级数据源配置
    // *
    // * @param context
    // * @throws Exception
    // */
    // public void doGetAdvanceDatasourceConfigDesc(Context context)
    // throws Exception {
    // 
    // Application app = getApplication();
    // JSONObject json = new JSONObject();
    // boolean hasSetAdvanceDatasourceConfigDesc = hasSetAdvanceDatasourceConfigDesc(app);
    // if (hasSetAdvanceDatasourceConfigDesc) {
    // byte[] content = getAdvanceDatasrouceConfigDesc(app);
    // json.put("configDesc", new String(content, getEncode()));
    // }
    // 
    // json.put("hasSetAdvanceDatasourceConfigDesc",
    // hasSetAdvanceDatasourceConfigDesc);
    // 
    // context.put("query_result", json.toString(1));
    // }
    // 
    // /**
    // * @param context
    // * @throws Exception
    // */
    // 
    // public void doGetConfig(Context context) throws Exception {
    // 
    // Application app = getApplication();
    // //		ApplicationExtend appExtend = this.getApplicationExtendDAO()
    // //				.selectByAppId(app.getAppId());
    // 
    // //		ReleaseIndexInfo rlsIndexInfo = ReleaseScreen
    // //				.getReleaseIndexInfoCurEnv(this, app.getAppId());
    // 
    // //		final Long taskid = TairAction.createTask(app, this.getTaskDAO());
    // 
    // TriggerTaskConfig triggerConfig = null;
    // if ("tddl".equals(appExtend.getSourceType())) {
    // TddlTaskConfig config = null;
    // final Tddl tddl = this.getTddlDAO().loadByAppId(app.getAppId());
    // if (tddl == null) {
    // throw new IllegalStateException("appid:" + app.getAppId()
    // + " relevant tddl object is null");
    // }
    // config = getTddlToplog(tddl.getTddlAppName(), tddl.getTableName());
    // config.setTddlAppName(tddl.getTddlAppName());
    // config.setDataSizeEstimate(tddl.getDataSizeEstimate());
    // config.setLogicTableName(tddl.getTableName());
    // 
    // if (rlsIndexInfo != null) {
    // config.setGroupSize(rlsIndexInfo.getGroupSize());
    // config.setRepSize(rlsIndexInfo.getReplicationSize());
    // 
    // if (ReleaseScreen.RLS_SELECT.equals(rlsIndexInfo
    // .getReleaseType())) {
    // String serversKey = ConstantUtil.RLS_SERVERLIST_PREFIX
    // + ManageUtils.getRunEnvironment().getId() + "_"
    // + app.getAppId();
    // List<String> serverList = this.getCache()
    // .getObj(serversKey);
    // 
    // // throw exception later or now
    // config.setServerList(serverList);
    // }
    // 
    // }
    // 
    // EncryptODPSConfig extODPSConfig = new EncryptODPSConfig(
    // Config.getOdpsConfig());
    // extODPSConfig.setShareModPsKey("share_mod");
    // extODPSConfig.setDailyPsKey("ps");
    // config.setOdpsConfig(extODPSConfig);
    // 
    // // <<======================================== =============
    // // 将schema中的字段内容注入到需要导入column中
    // SnapshotDomain domain = this.getSnapshotDomain(app.getAppId());
    // ByteArrayInputStream byteReader = new ByteArrayInputStream(domain
    // .getSolrSchema().getContent());
    // try {
    // ParseResult parseResult = solrFieldsParser
    // .parseSchema(byteReader);
    // if (!parseResult.isValid() || parseResult.dFields.size() < 1) {
    // throw new IllegalStateException(
    // "when parse the app schema of " + app.getAppId()
    // + "." + app.getProjectName()
    // + " has some error occured");
    // }
    // for (PSchemaField f : parseResult.dFields) {
    // config.addSchemaColumn(f.getName());
    // }
    // config.setShareId(parseResult.getSharedKey());
    // } finally {
    // IOUtils.closeQuietly(byteReader);
    // }
    // // =====================================================>>
    // triggerConfig = config;
    // } else if ("rds".equals(appExtend.getSourceType())
    // || "bcrds".equals(appExtend.getSourceType())) {
    // RDSTaskConfig rdsTriggerConfig = new RDSTaskConfig();
    // rdsTriggerConfig.setTaskId(taskid.intValue());
    // 
    // // ========================================
    // // 设置isv名称
    // IsvCriteria icriteria = new IsvCriteria();
    // icriteria.createCriteria().andDptIdEqualTo(app.getDptId());
    // icriteria.setOrderByClause("id asc");
    // for (Isv isv : this.getIsvDAO().selectByExample(icriteria, 1, 1)) {
    // rdsTriggerConfig.setIsv(isv.getIsvName());
    // break;
    // }
    // // ========================================
    // IndexConfig indexConfig = new IndexConfig();
    // indexConfig.setIndexname(app.getProjectName());
    // SnapshotDomain domain = this.getSnapshotDomain(app.getAppId());
    // indexConfig.setSchema(new String(domain.getSolrSchema()
    // .getContent(), Charset.forName("utf8")));
    // 
    // // RdsTableCriteria criteria = new RdsTableCriteria();
    // // criteria.createCriteria()
    // // .andRIdEqualTo(ADVANCE_SOURCE_XML_DEFINE_FLAG)
    // // .andAIdEqualTo(app.getAppId());
    // // criteria.setOrderByClause("gmt_create desc");
    // // List<RdsTable> tables = this.getRdsTableDAO().selectByExample(
    // // criteria, 1, 1);
    // 
    // if (hasSetAdvanceDatasourceConfigDesc(app)) {
    // byte[] content = getAdvanceDatasrouceConfigDesc(app);
    // indexConfig.setAdvanceDatasourceConfigDesc(new String(content,
    // getEncode()));
    // 
    // } else {
    // List<RdsDbTables> dbs = this.getRdsTableDAO()
    // .getIndexDataSource(app.getAppId());
    // 
    // // 没有选择高级视图功能
    // for (RdsDbTables db : dbs) {
    // RdsSource rds = new RdsSource();
    // rds.setDbname("rds".equals(appExtend.getSourceType()) ? db
    // .getDbName() : StringUtils.substringBefore(
    // db.getDbName(), ","));
    // 
    // rds.setUsername(db.getUserName());
    // rds.setPassword(db.getPassword());
    // rds.setHost(db.getHost());
    // rds.addTable(Arrays.asList(StringUtils.split(
    // db.getTbsName(), ",")));
    // indexConfig.add(rds);
    // }
    // }
    // appExtend.setSourceType("rds");
    // rdsTriggerConfig.addIndexConfig(indexConfig);
    // triggerConfig = rdsTriggerConfig;
    // 
    // } else if ("odps".equalsIgnoreCase(appExtend.getSourceType())) {
    // ODPSTaskConfig odpsTaskConfig = new ODPSTaskConfig();
    // Odps odps = this.getOdpsDAO().loadByAppId(new Long(app.getAppId()));
    // if (odps == null) {
    // throw new IllegalStateException("typs of source type,appid:"
    // + app.getAppId() + " get not get relevant Odps source");
    // }
    // odpsTaskConfig.setTableName(odps.getTabName());
    // ODPSConfig odpsConfig = new ODPSConfig();
    // odpsConfig.setAccessId(odps.getAccessId());
    // odpsConfig.setAccessKey(odps.getAccessKey());
    // 
    // if ("Y".equalsIgnoreCase(odps.getFromInner())) {
    // // 弹内
    // String innerOdpsEndpoint = getOdpsDataTunelEndPoint();
    // odpsConfig.setDatatunelEndPoint(innerOdpsEndpoint);
    // } else {
    // 
    // if (ManageUtils.getRuntime() == RunEnvironment.DAILY) {
    // odpsConfig
    // .setDatatunelEndPoint("http://dt.odps.aliyun.com");
    // } else {
    // odpsConfig.setDatatunelEndPoint("http://"
    // + Config.getOuterDatatunelEndpoint());
    // }
    // 
    // }
    // 
    // odpsConfig.setShallIgnorPartition(!JobConstant.STOPED.equals(odps
    // .getHasPartation()));
    // odpsConfig.setProject(odps.getProject());
    // odpsConfig.setGroupSize(odps.getGroupNum());
    // 
    // if (!odpsConfig.isShallIgnorPartition()) {
    // // << 设置daily partition=========================
    // if (StringUtils.startsWith(odps.getDateFormat(), "{")) {
    // JSONTokener jtoken = new JSONTokener(odps.getDateFormat());
    // JSONObject psformat = new JSONObject(jtoken);
    // if (!psformat.isNull("date_format")) {
    // SimpleDateFormat dateFormat = new SimpleDateFormat(
    // psformat.getString("date_format"));
    // Calendar calender = Calendar.getInstance();
    // calender.setTime(new Date());
    // int offset = 0;
    // try {
    // offset = psformat.getInt("offset");
    // } catch (Throwable e) {
    // 
    // }
    // calender.add(Calendar.DAY_OF_YEAR, offset);
    // odpsConfig.setDailyPartition(new DailyPartition(odps
    // .getDailyPs(), dateFormat.format(calender
    // .getTime())));
    // }
    // }
    // // =============================================>>
    // odpsConfig.setGroupPartition(odps.getModPs());
    // }
    // odpsTaskConfig.setOdpsSource(odpsConfig);
    // triggerConfig = odpsTaskConfig;
    // } else {
    // throw new IllegalStateException("source type:"
    // + appExtend.getSourceType() + " is illegal");
    // }
    // 
    // // 单次最多导入多少条？
    // if (appExtend.getTestMaxDocNum() != null) {
    // triggerConfig
    // .setMaxDumpCount(new Long(appExtend.getTestMaxDocNum()));
    // }
    // 
    // // 设置最大QPS
    // triggerConfig.setMaxQPS(appExtend.getMaxPvCount().intValue());
    // triggerConfig.setTaskId(taskid.intValue());
    // triggerConfig.setType(appExtend.getSourceType());
    // triggerConfig.setAppName(app.getProjectName());
    // JSON configJson = (JSON) JSON.toJSON(triggerConfig);
    // context.put("query_result", configJson.toJSONString());
    // }
    // 
    // protected boolean hasSetAdvanceDatasourceConfigDesc(Application app) {
    // RdsTableCriteria criteria = createRdsTableCriteria(app);
    // return this.getRdsTableDAO().countByExample(criteria) > 0;
    // }
    // 
    // //	/**
    // //	 * @param app
    // //	 * @return
    // //	 */
    // //	protected RdsTableCriteria createRdsTableCriteria(Application app) {
    // //		RdsTableCriteria criteria = new RdsTableCriteria();
    // //		criteria.createCriteria().andRIdEqualTo(ADVANCE_SOURCE_XML_DEFINE_FLAG)
    // //				.andAIdEqualTo(app.getAppId());
    // //		criteria.setOrderByClause("gmt_create desc");
    // //		return criteria;
    // //	}
    // 
    // /**
    // * @param app
    // * @param tables
    // * @return
    // */
    // protected byte[] getAdvanceDatasrouceConfigDesc(Application app) {
    // // 选择了数据源高级视图模式
    // Long uploadResourceId = null;
    // RdsTableCriteria criteria = createRdsTableCriteria(app);
    // List<RdsTable> tables = this.getRdsTableDAO().selectByExample(criteria,
    // 1, 1);
    // for (RdsTable tab : tables) {
    // try {
    // uploadResourceId = Long.parseLong(tab.getTableName());
    // } catch (Exception e) {
    // throw new IllegalStateException("tab.getTableName():"
    // + tab.getTableName() + " is not a numeric");
    // }
    // break;
    // }
    // 
    // if (uploadResourceId == null) {
    // throw new IllegalStateException("app.getAppId():" + app.getAppId()
    // + ",tables size:" + tables.size());
    // }
    // 
    // UploadResource resource = this.getUploadResourceDAO().loadFromWriteDB(
    // uploadResourceId);
    // 
    // byte[] content = resource.getContent();
    // if (content == null) {
    // throw new IllegalStateException("app.getAppId():" + app.getAppId()
    // + " content can not be null");
    // }
    // return content;
    // }
    // /**
    // * @param tddl
    // * @return
    // * @throws MalformedURLException
    // */
    // public static TddlTaskConfig getTddlToplog(String appName,
    // final String logicTabName) {
    // try {
    // TddlTaskConfig config;
    // final URL url = new URL("http://" + Config.getTddlParseHost()
    // + "/mc/authed_api/queryTopology.do?appName=" + appName
    // + "&env=" + ManageUtils.getRuntime().getKeyName());
    // 
    // config = ConfigFileContext.processContent(url,
    // new StreamProcess<TddlTaskConfig>() {
    // @Override
    // public TddlTaskConfig p(int status, InputStream stream,
    // String md5) {
    // TddlTaskConfig taskConfig = new TddlTaskConfig();
    // JSONObject topology = null;
    // JSONObject group = null;
    // JSONArray groupDetail = null;
    // JSONArray array = null;
    // JSONObject logic = null;
    // String pattern = null;
    // String groupName = null;
    // try {
    // JSONTokener tokener = new JSONTokener(IOUtils
    // .toString(stream));
    // 
    // topology = new JSONObject(tokener);
    // topology = topology.getJSONObject("topology");
    // array = topology.getJSONArray("app_topology");
    // 
    // for (int i = 0; i < array.length(); i++) {
    // group = array.getJSONObject(i);
    // if ("DELETED".equalsIgnoreCase(group
    // .getString("status"))) {
    // continue;
    // }
    // if (!StringUtils.equals(logicTabName,
    // group.getString("logic_table"))) {
    // continue;
    // }
    // groupDetail = group
    // .getJSONArray("detail_topology");
    // 
    // for (int j = 0; j < groupDetail.length(); j++) {
    // logic = groupDetail.getJSONObject(j);
    // 
    // groupName = logic.getString("group_ds");
    // // auction_attribute_extra_[0000-0031]
    // pattern = logic.getString("pattern");
    // taskConfig.addTable(groupName,
    // parseTableList(pattern));
    // }
    // 
    // }
    // 
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // 
    // return taskConfig;
    // }
    // });
    // return config;
    // } catch (MalformedURLException e) {
    // throw new IllegalStateException(e);
    // }
    // }
    // /**
    // * @return
    // */
    // private static String getOdpsDataTunelEndPoint() {
    // return "http://"
    // + ((ManageUtils.getRuntime() == RunEnvironment.DAILY) ? "dt-corp.odps.aliyun-inc.com"
    // : "dt.odps.aliyun-inc.com");
    // }
    // public static class EncryptODPSConfig extends ODPSConfig {
    // private final ODPSConfig config;
    // 
    // private String dailyPsKey;
    // private String shareModPsKey;
    // 
    // public void setDailyPsKey(String dailyPsKey) {
    // this.dailyPsKey = dailyPsKey;
    // }
    // 
    // public void setShareModPsKey(String shareModPsKey) {
    // this.shareModPsKey = shareModPsKey;
    // }
    // 
    // public EncryptODPSConfig(ODPSConfig config) {
    // super();
    // this.config = config;
    // }
    // 
    // public Integer getGroupSize() {
    // return config.getGroupSize();
    // }
    // 
    // public String getAccessKey() {
    // try {
    // return Secret.encrypt(config.getAccessKey(),
    // ConstantUtil.CRYPTKEY);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    // 
    // public DailyPartition getDailyPartition() {
    // // return config.getDailyPartition();
    // SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    // return new DailyPartition(this.dailyPsKey,
    // format.format(new Date()));
    // }
    // 
    // @Override
    // public String getGroupPartition() {
    // // return config.getGroupPartition();
    // return this.shareModPsKey;
    // }
    // 
    // public String getAccessId() {
    // try {
    // return Secret.encrypt(config.getAccessId(),
    // ConstantUtil.CRYPTKEY);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    // 
    // public String getProject() {
    // return config.getProject();
    // }
    // 
    // // public String getDailyPartition() {
    // // return config.getDailyPartition();
    // // }
    // 
    // public String getServiceEndPoint() {
    // // return config.getServiceEndPoint();
    // return "http://service-corp.odps.aliyun-inc.com/api";
    // }
    // 
    // public String getDatatunelEndPoint() {
    // // return config.getDatatunelEndPoint();
    // return getOdpsDataTunelEndPoint();
    // }
    // 
    // }
}
