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
package com.qlangtech.tis.offline.module.manager.impl;

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Sets;
import com.qlangtech.tis.db.parser.DBConfigParser;
import com.qlangtech.tis.db.parser.DBTokenizer;
import com.qlangtech.tis.db.parser.domain.DBConfig;
import com.qlangtech.tis.db.parser.domain.DBConfigSuit;
import com.qlangtech.tis.git.GitUtils;
import com.qlangtech.tis.git.GitUtils.GitBranchInfo;
import com.qlangtech.tis.git.GitUtils.GitUser;
import com.qlangtech.tis.git.GitUtils.JoinRule;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.OperationLog;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.offline.DbScope;
import com.qlangtech.tis.offline.module.action.OfflineDatasourceAction;
import com.qlangtech.tis.offline.module.pojo.ColumnMetaData;
import com.qlangtech.tis.offline.pojo.TISDb;
import com.qlangtech.tis.offline.pojo.TISTable;
import com.qlangtech.tis.offline.pojo.WorkflowPojo;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.workflow.dao.IComDfireTisWorkflowDAOFacade;
import com.qlangtech.tis.workflow.pojo.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.sql.*;
import java.util.Date;
import java.util.*;

/**
 *  ds和wf中涉及到db的处理
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年7月25日
 */
public class OfflineManager {

    // private static final String URL_ONLINE = "10.1.4.208:8080/config/config.ajax?action=offline_datasource_action";
    private IComDfireTisWorkflowDAOFacade workflowDAOFacade;

    public void setComDfireTisWorkflowDAOFacade(IComDfireTisWorkflowDAOFacade comDfireTisWorkflowDAOFacade) {
        this.workflowDAOFacade = comDfireTisWorkflowDAOFacade;
    }

    /**
     * 取得可以用的数据流
     *
     * @return 工作流列表
     */
    public List<WorkFlow> getUsableWorkflow() {
        WorkFlowCriteria query = new WorkFlowCriteria();
        query.createCriteria();
        query.setOrderByClause("id desc");
        return this.workflowDAOFacade.getWorkFlowDAO().selectByExample(query, 1, 100);
    }

    /**
     * description: 添加一个 数据源库 date: 2:30 PM 4/28/2017
     */
    // 
    public void addDatasourceDb(TISDb db, BasicModule action, Context context) throws Exception {
        // update db
        String dbName = db.getDbName();
        DatasourceDb datasourceDb = new DatasourceDb();
        datasourceDb.setName(dbName);
        datasourceDb.setSyncOnline(new Byte("0"));
        datasourceDb.setCreateTime(new Date());
        datasourceDb.setOpTime(new Date());
        DatasourceDbCriteria criteria = new DatasourceDbCriteria();
        criteria.createCriteria().andNameEqualTo(dbName);
        int exist = workflowDAOFacade.getDatasourceDbDAO().countByExample(criteria);
        if (exist > 0) {
            action.addErrorMessage(context, "已经有了同名(" + dbName + ")的数据库");
            return;
        }
        /**
         * 校验数据库连接是否正常
         */
        if (!testDbConnection(db, action, context).valid) {
            return;
        }
        int dbId = workflowDAOFacade.getDatasourceDbDAO().insertSelective(datasourceDb);
        GitUtils.$().createDatabase(db, "add db " + db.getDbName());
        action.addActionMessage(context, "数据库添加成功");
        action.setBizResult(context, dbId);
    }

    /**
     * description: 测试连接，true连接成功，false连接失败 date: 3:04 PM 5/25/2017
     */
    public TestDbConnection testDbConnection(TISDb database, BasicModule action, Context context) throws Exception {
        TestDbConnection testResult = new TestDbConnection();
        testResult.facade = database.isFacade();
        if (StringUtils.isBlank(database.getPassword()) || StringUtils.isBlank(database.getUserName())) {
            throw new IllegalArgumentException("password:" + database.getPassword() + " or username:[" + database.getUserName() + "] is illegal");
        }
        if (StringUtils.isBlank(database.getHost())) {
            throw new IllegalArgumentException("host can not be null");
        }
        // ///////////////////////////////////////////////////////////////////
        DBTokenizer tokenizer = new DBTokenizer("host:" + database.getHost());
        tokenizer.parse();
        DBConfigParser parser = new DBConfigParser(tokenizer.getTokenBuffer());
        parser.dbConfigResult.setName(database.getDbName());
        parser.dbConfigResult.setPort(Integer.parseInt(database.getPort()));
        if (!parser.parseHostDesc()) {
            action.addErrorMessage(context, database.getHost() + " 有误，请联系系统管理员");
            return testResult;
        }
        int dbCount = 0;
        DBConfig db = parser.dbConfigResult;
        for (Map.Entry<String, List<String>> entry : db.getDbEnum().entrySet()) {
            dbCount += entry.getValue().size();
        }
        testResult.dbCount = dbCount;
        if (database.isFacade() && dbCount > 1) {
            action.addErrorMessage(context, "门面数据库目标库不能为多库");
            return testResult;
        }
        StringBuilder connErrs = new StringBuilder();
        if (!db.vistDbURL(false, (dbName, jdbcUrl) -> {
            try {
                validateConnection(jdbcUrl, db, database.getUserName(), database.getPassword(), (conn) -> {
                    Statement s = null;
                    ResultSet result = null;
                    try {
                        s = conn.createStatement();
                        result = s.executeQuery("select 1");
                        if (result.next()) {
                            result.getString(1);
                        }
                    } finally {
                        try {
                            result.close();
                        } catch (SQLException e) {
                        }
                        try {
                            s.close();
                        } catch (SQLException e) {
                        }
                    }
                });
            } catch (Throwable e) {
                connErrs.append(dbName + " " + e.getMessage());
            }
        }, database.isFacade(), action, context)) {
            return testResult;
        }
        if (connErrs.length() > 0) {
            action.addErrorMessage(context, "连接异常:" + connErrs.toString());
            return testResult;
        }
        return testResult.success();
    }

    public static class TestDbConnection {

        public boolean valid = false;

        // DB门面连接
        private boolean facade = false;

        private int dbCount;

        private TestDbConnection success() {
            this.valid = true;
            return this;
        }

        public int getDbCount() {
            return this.dbCount;
        }

        public boolean isFacade() {
            return this.facade;
        }
    }

    public void visitConnection(DBConfig db, String ip, String dbName, String username, String password, IConnProcessor p) throws Exception {
        if (db == null) {
            throw new IllegalStateException("param db can not be null");
        }
        if (StringUtils.isEmpty(ip)) {
            throw new IllegalArgumentException("param ip can not be null");
        }
        if (StringUtils.isEmpty(dbName)) {
            throw new IllegalArgumentException("param dbName can not be null");
        }
        if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException("param username can not be null");
        }
        if (StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("param password can not be null");
        }
        if (p == null) {
            throw new IllegalArgumentException("param IConnProcessor can not be null");
        }
        Connection conn = null;
        final String jdbcUrl = "jdbc:mysql://" + ip + ":" + db.getPort() + "/" + dbName + "?useUnicode=yes&characterEncoding=utf8";
        try {
            validateConnection(jdbcUrl, db, username, password, p);
        } catch (Exception e) {
            throw new RuntimeException(jdbcUrl, e);
        }
    }

    private void validateConnection(String jdbcUrl, DBConfig db, String username, String password, IConnProcessor p) throws Exception {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            p.vist(conn);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    public interface IConnProcessor {

        public void vist(Connection conn) throws SQLException;
    }

    public void addDatasourceTable(TISTable table, BasicModule action, Context context, boolean updateMode) throws Exception {
        String tableName = table.getTableName();
        String tableLogicName = table.getTableLogicName();
        // 检查db是否存在
        final DatasourceDbCriteria dbCriteria = new DatasourceDbCriteria();
        dbCriteria.createCriteria().andIdEqualTo(table.getDbId());
        int sameDbCount = workflowDAOFacade.getDatasourceDbDAO().countByExample(dbCriteria);
        if (sameDbCount < 1) {
            action.addErrorMessage(context, "找不到这个数据库");
            return;
        }
        int dbId = table.getDbId();
        DatasourceTableCriteria tableCriteria = new DatasourceTableCriteria();
        if (updateMode) {
            // 检查表是否存在，如不存在则为异常状态
            tableCriteria.createCriteria().andIdEqualTo(table.getTabId()).andNameEqualTo(table.getTableName());
            int findTable = workflowDAOFacade.getDatasourceTableDAO().countByExample(tableCriteria);
            if (findTable < 1) {
                throw new IllegalStateException("tabid:" + table.getTabId() + ",tabName:" + table.getTableName() + " is not exist in db");
            }
        } else {
            // 检查表是否存在，如存在则退出
            tableCriteria.createCriteria().andDbIdEqualTo(dbId).andTableLogicNameEqualTo(tableLogicName);
            int sameTableCount = workflowDAOFacade.getDatasourceTableDAO().countByExample(tableCriteria);
            if (sameTableCount > 0) {
                action.addErrorMessage(context, "该数据库下已经有了相同逻辑名的表:" + tableLogicName);
                return;
            }
        }
        // 更新DB的最新操作时间
        DatasourceDb db = new DatasourceDb();
        db.setId(dbId);
        db.setOpTime(new Date());
        int dbUpdateRows = workflowDAOFacade.getDatasourceDbDAO().updateByExampleSelective(db, dbCriteria);
        if (dbUpdateRows < 1) {
            throw new IllegalStateException("db update faild");
        }
        // 添加表db
        DatasourceTable dsTable = null;
        Integer tableId;
        if (updateMode) {
            if ((tableId = table.getTabId()) == null) {
                throw new IllegalStateException("update process tabId can not be null");
            }
            tableCriteria = new DatasourceTableCriteria();
            // .andNameEqualTo(table.getTableName());
            tableCriteria.createCriteria().andIdEqualTo(table.getTabId());
            dsTable = new DatasourceTable();
            dsTable.setOpTime(new Date());
            workflowDAOFacade.getDatasourceTableDAO().updateByExampleSelective(dsTable, tableCriteria);
            dsTable.setId(tableId);
        } else {
            dsTable = new DatasourceTable();
            dsTable.setName(tableName);
            dsTable.setTableLogicName(tableLogicName);
            dsTable.setDbId(dbId);
            dsTable.setGitTag(tableLogicName);
            // 标示是否有同步到线上去
            dsTable.setSyncOnline(new Byte("0"));
            dsTable.setCreateTime(new Date());
            dsTable.setOpTime(new Date());
            tableId = workflowDAOFacade.getDatasourceTableDAO().insertSelective(dsTable);
            dsTable.setId(tableId);
        }
        // 添加 git
        GitUtils.$().createTableDaily(table, "add table " + tableLogicName);
        action.addActionMessage(context, "数据库表'" + tableName + "'" + (updateMode ? "更新" : "添加") + "成功");
        table.setSelectSql(null);
        table.setReflectCols(null);
        table.setTabId(dsTable.getId());
        action.setBizResult(context, table);
    }

    /**
     * 更新或者添加门面数据库配置信息
     *
     * @param dbid
     * @param db
     * @param action
     * @param context
     */
    public void updateFacadeDBConfig(Integer dbid, TISDb db, BasicModule action, Context context) throws Exception {
        if (dbid == null) {
            throw new IllegalArgumentException("dbid can not be null");
        }
        DatasourceDb ds = workflowDAOFacade.getDatasourceDbDAO().loadFromWriteDB(dbid);
        if (ds == null) {
            throw new IllegalStateException("dbid:" + dbid + " relevant datasourceDB can not be null");
        }
        List<String> children = GitUtils.$().listDbConfigPath(ds.getName());
        boolean isAdd = !children.contains(GitUtils.DB_CONFIG_META_NAME + DbScope.FACADE.getDBType());
        if (StringUtils.isEmpty(db.getPassword())) {
            if (isAdd) {
                throw new IllegalStateException("db password can not be empty in add process");
            } else {
                // 更新流程
                DBConfig dbConfig = GitUtils.$().getDbLinkMetaData(ds.getName(), DbScope.FACADE);
                db.setPassword(dbConfig.getPassword());
            }
        }
        db.setFacade(true);
        if (!this.testDbConnection(db, action, context).valid) {
            return;
        }
        String path = GitUtils.$().getDBConfigPath(ds.getName(), DbScope.FACADE);
        GitUtils.$().processDBConfig(db, path, "edit db" + db.getDbName(), isAdd, true);
    }

    /**
     * 修改数据库配置 date: 8:33 PM 6/15/2017
     */
    public void editDatasourceDb(TISDb db, BasicModule action, Context context) throws Exception {
        // 看看db里有没有这条数据
        String dbName = db.getDbName();
        if (StringUtils.isBlank(db.getDbId())) {
            throw new IllegalArgumentException("dbid can not be null");
        }
        DatasourceDb d = workflowDAOFacade.getDatasourceDbDAO().selectByPrimaryKey(// .minSelectByExample(criteria);
        Integer.parseInt(db.getDbId()));
        if (d == null) {
            action.addErrorMessage(context, "db中找不到这个数据库");
            return;
        }
        GitUtils.$().updateDatabase(db, "edit db " + db.getDbName());
        action.addActionMessage(context, "数据库修改成功");
        action.setBizResult(context, db.getDbId());
    }

    public void editDatasourceTable(TISTable table, BasicModule action, Context context) throws Exception {
        String dbName = table.getDbName();
        String tableLogicName = table.getTableLogicName();
        // 检查db是否存在
        DatasourceDbCriteria dbCriteria = new DatasourceDbCriteria();
        dbCriteria.createCriteria().andNameEqualTo(dbName);
        List<DatasourceDb> dbList = workflowDAOFacade.getDatasourceDbDAO().minSelectByExample(dbCriteria);
        if (CollectionUtils.isEmpty(dbList)) {
            action.addErrorMessage(context, "找不到这个数据库");
            return;
        }
        int dbId = dbList.get(0).getId();
        // 检查表是否存在
        DatasourceTableCriteria tableCriteria = new DatasourceTableCriteria();
        tableCriteria.createCriteria().andDbIdEqualTo(dbId).andTableLogicNameEqualTo(tableLogicName);
        List<DatasourceTable> tableList = workflowDAOFacade.getDatasourceTableDAO().minSelectByExample(tableCriteria);
        if (CollectionUtils.isEmpty(tableList)) {
            action.addErrorMessage(context, "该数据库下没有表" + tableLogicName);
            return;
        }
        int tableId = tableList.get(0).getId();
        // update git
        // String path = dbName + "/" + tableLogicName;
        GitUtils.$().createTableDaily(table, "edit table " + table.getTableLogicName());
        OperationLog operationLog = new OperationLog();
        operationLog.setUsrName(action.getLoginUserName());
        operationLog.setUsrId(action.getUserId());
        operationLog.setOpType("editDatasourceTable");
        action.addActionMessage(context, "数据库表修改成功");
        action.setBizResult(context, tableId);
    }

    /**
     * description: 获取所有的工作流数据库 date: 2:30 PM 4/28/2017
     */
    public List<Option> getUsableDbNames() {
        DatasourceDbCriteria criteria = new DatasourceDbCriteria();
        criteria.createCriteria();
        List<DatasourceDb> dbList = workflowDAOFacade.getDatasourceDbDAO().selectByExample(criteria);
        List<Option> dbNameList = new LinkedList<>();
        for (DatasourceDb datasourceDb : dbList) {
            dbNameList.add(new Option(datasourceDb.getName(), String.valueOf(datasourceDb.getId())));
        }
        // Collections.sort(dbNameList);
        return dbNameList;
    }

    /**
     * 取得某個DB下的所有的表
     *
     * @param dbName
     * @return
     * @throws Exception
     */
    public List<String> getTables(String dbName) throws Exception {
        final List<String> tabs = new ArrayList<>();
        // String dbEnumName = null;
        // try {
        // final Map<String, BasicDataSource> dsMap =
        // offlineManager.getDataSources(dbName);
        final DBConfig dbConfig = GitUtils.$().getDbLinkMetaData(dbName, DbScope.DETAILED);
        dbConfig.vistDbName((config, ip, databaseName) -> {
            visitConnection(config, ip, databaseName, config.getUserName(), config.getPassword(), (conn) -> {
                Statement statement = null;
                ResultSet resultSet = null;
                try {
                    statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    statement.execute("show tables");
                    resultSet = statement.getResultSet();
                    while (resultSet.next()) {
                        tabs.add(resultSet.getString(1));
                    }
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                }
            });
            return true;
        });
        return tabs;
    }

    /**
     * description: 获得所有的
     * <p>
     * date: 3:37 PM 5/11/2017
     */
    // 
    public List<ColumnMetaData> getTableMetadata(String dbName, String table) {
        if (StringUtils.isBlank(table)) {
            throw new IllegalArgumentException("param table can not be null");
        }
        List<ColumnMetaData> columns = new ArrayList<>();
        // String dbEnumName = null;
        try {
            // final Map<String, BasicDataSource> dsMap =
            // offlineManager.getDataSources(dbName);
            final DBConfig dbConfig = GitUtils.$().getDbLinkMetaData(dbName, DbScope.DETAILED);
            dbConfig.vistDbName((config, ip, dbname) -> {
                visitConnection(config, ip, dbname, config.getUserName(), config.getPassword(), (conn) -> {
                    // Statement statement = null;
                    // ResultSet resultSet = null;
                    DatabaseMetaData metaData1 = null;
                    ResultSet primaryKeys = null;
                    ResultSet columns1 = null;
                    try {
                        metaData1 = conn.getMetaData();
                        primaryKeys = metaData1.getPrimaryKeys(null, null, table);
                        columns1 = metaData1.getColumns(null, null, table, null);
                        Set<String> pkCols = Sets.newHashSet();
                        while (primaryKeys.next()) {
                            // $NON-NLS-1$
                            String columnName = primaryKeys.getString("COLUMN_NAME");
                            pkCols.add(columnName);
                        }
                        int i = 0;
                        String colName = null;
                        while (columns1.next()) {
                            columns.add(new ColumnMetaData((i++), (colName = columns1.getString("COLUMN_NAME")), columns1.getInt("DATA_TYPE"), pkCols.contains(colName)));
                        }
                    // metaData1.c
                    // statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    // resultSet = statement.executeQuery("select * from " + table + " limit 1");
                    // ResultSetMetaData metaData = resultSet.getMetaData();
                    // int columnCnt = metaData.getColumnCount();
                    // for (int i = 1; i <= columnCnt; i++) {
                    // columns.add(
                    // new ColumnMetaData((i - 1), metaData.getColumnLabel(i), metaData.getColumnType(i)));
                    // }
                    } finally {
                        closeResultSet(columns1);
                        closeResultSet(primaryKeys);
                    // if (columns1 != null) {
                    // columns1.close();
                    // }
                    // if (primaryKeys != null) {
                    // primaryKeys.close();
                    // }
                    }
                });
                return true;
            });
            return columns;
        // for (Map.Entry<String, BasicDataSource> dsEntry :
        // dsMap.entrySet()) {
        // dbEnumName = dsEntry.getKey();
        // BasicDataSource dataSource = dsEntry.getValue();
        // 
        // Connection connection = dataSource.getConnection();
        // Statement statement =
        // connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
        // ResultSet.CONCUR_READ_ONLY);
        // statement.execute("select * from " + table + " limit 0");
        // ResultSet resultSet = statement.getResultSet();
        // ResultSetMetaData metaData = resultSet.getMetaData();
        // int columnCnt = metaData.getColumnCount();
        // List<ColumnMetaData> columns = new ArrayList<>();
        // for (int i = 1; i <= columnCnt; i++) {
        // columns.add(new ColumnMetaData((i - 1),
        // metaData.getColumnLabel(i), metaData.getColumnType(i)));
        // }
        // columnMetaDataColumns = columns;
        // if (!repeat) {
        // break;
        // }
        // }
        } catch (Exception e) {
            // return null;
            throw new RuntimeException(e);
        }
    }

    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
                ;
            }
        }
    }

    /**
     * description: 获取数据源表 date: 6:21 PM 5/18/2017
     */
    public List<DatasourceTable> getDatasourceTables() {
        DatasourceTableCriteria criteria = new DatasourceTableCriteria();
        criteria.createCriteria();
        List<DatasourceTable> tables = workflowDAOFacade.getDatasourceTableDAO().selectByExample(criteria);
        DatasourceDbCriteria dbCriteria = new DatasourceDbCriteria();
        dbCriteria.createCriteria();
        List<DatasourceDb> dbs = workflowDAOFacade.getDatasourceDbDAO().selectByExample(dbCriteria);
        Map<Integer, DatasourceDb> dbMap = new HashMap<>();
        for (DatasourceDb datasourceDb : dbs) {
            dbMap.put(datasourceDb.getId(), datasourceDb);
        }
        // 把所有表名统计一遍
        Map<String, Integer> tableNameCntMap = new HashMap<>();
        for (DatasourceTable datasourceTable : tables) {
            String name = datasourceTable.getTableLogicName();
            if (tableNameCntMap.containsKey(name)) {
                tableNameCntMap.put(name, tableNameCntMap.get(name) + 1);
            } else {
                tableNameCntMap.put(name, 1);
            }
        }
        for (DatasourceTable datasourceTable : tables) {
            String name = datasourceTable.getTableLogicName();
            if (tableNameCntMap.get(name) > 1) {
                datasourceTable.setTableLogicName(dbMap.get(datasourceTable.getDbId()).getName() + "/" + datasourceTable.getTableLogicName());
            }
        }
        Collections.sort(tables);
        return tables;
    }

    // 
    // /**
    // * description: 添加工作流 date: 11:10 AM 5/19/2017
    // */
    // 
    // public void addWorkflow(WorkflowPojo pojo, BasicModule action, Context
    // context) {
    // String dataStreamName = pojo.getName();
    // 
    // WorkFlowCriteria criteria = new WorkFlowCriteria();
    // criteria.createCriteria().andNameEqualTo(dataStreamName);
    // List<WorkFlow> workflowList =
    // workflowDAOFacade.getWorkFlowDAO().selectByExample(criteria);
    // 
    // // 1、检测是否重名
    // if (!CollectionUtils.isEmpty(workflowList)) {
    // action.addErrorMessage(context, "已经有了同名的工作流");
    // return;
    // }
    // 
    // // 2、检测xml是否正确
    // JoinRule task = pojo.getTask();
    // if (task == null || StringUtils.isBlank(task.getContent())) {
    // action.addErrorMessage(context, "脚本内容不能为空");
    // return;
    // }
    // 
    // if (!isXmlValid(task)) {
    // action.addErrorMessage(context, "XML解析失败，请检测XML格式");
    // return;
    // }
    // 
    // // 3、add git
    // JSONObject jsonObject = new JSONObject();
    // jsonObject.put("name", dataStreamName);
    // // jsonObject.put("tables", StringUtils.join(pojo.getDependTableIds(), ","));
    // jsonObject.put("task", pojo.getTask());
    // try {
    // // 创建git分支
    // 
    // // 在分支上添加 git配置文件
    // // GitUtils.$().createWorkflowFile(dataStreamName, dataStreamName /*
    // branchName
    // // */,
    // // Sets.newHashSet(pojo.getDependTableIds()), task, "add workflow " +
    // // dataStreamName);
    // } catch (Exception e) {
    // action.addErrorMessage(context, "GIT文件添加失败");
    // action.addErrorMessage(context, e.getMessage());
    // return;
    // }
    // 
    // // 4、add db
    // WorkFlow workFlow = new WorkFlow();
    // workFlow.setName(dataStreamName);
    // IUser user = action.getUser();
    // workFlow.setOpUserId(1);
    // workFlow.setOpUserName(user.getName());
    // workFlow.setGitPath(dataStreamName);
    // workFlow.setCreateTime(new Date());
    // workFlow.setInChange(new Byte("1"));
    // int workflowId = this.workflowDAOFacade.getWorkFlowDAO().insert(workFlow);
    // action.addActionMessage(context, "全量工作流添加成功");
    // 
    // // 5、变更记录表添加一条变更
    // WorkFlowPublishHistory history = new WorkFlowPublishHistory();
    // history.setCreateTime(new Date());
    // history.setOpUserId(1);
    // history.setOpUserName(user.getName());
    // history.setWorkflowId(workflowId);
    // history.setWorkflowName(dataStreamName);
    // history.setPublishState(new Byte("3"));
    // history.setType(new Byte("1"));
    // history.setPublishReason("添加工作流" + dataStreamName);
    // history.setInUse(false);
    // this.workflowDAOFacade.getWorkFlowPublishHistoryDAO().insert(history);
    // }
    public void editWorkflow(WorkflowPojo pojo, BasicModule action, Context context) throws Exception {
        String name = pojo.getName();
        WorkFlowCriteria criteria = new WorkFlowCriteria();
        criteria.createCriteria().andNameEqualTo(name);
        List<WorkFlow> workflowList = workflowDAOFacade.getWorkFlowDAO().selectByExample(criteria);
        // 1、检测是否存在
        if (CollectionUtils.isEmpty(workflowList)) {
            action.addErrorMessage(context, "没有名字为" + name + "的工作流");
            return;
        }
        WorkFlow workFlow = workflowList.get(0);
        if (workFlow.getInChange().intValue() == 0) {
            action.addErrorMessage(context, "工作流不在变更中");
            return;
        }
        // 2、检测xml是否正确
        JoinRule task = pojo.getTask();
        if (task == null || StringUtils.isBlank(task.getContent())) {
            action.addErrorMessage(context, "脚本内容不能为空");
            return;
        }
        if (!isXmlValid(task)) {
            action.addErrorMessage(context, "XML解析失败，请检测XML格式");
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        // jsonObject.put("tables", StringUtils.join(pojo.getDependTableIds(), ","));
        jsonObject.put("task", pojo.getTask());
        try {
            GitUtils.$().updateWorkflowFile(name, name, jsonObject.toString(1), "update workflow " + name);
        } catch (Exception e) {
            action.addErrorMessage(context, "git配置文件修改失败");
            action.addErrorMessage(context, e.getMessage());
        }
    }

    /**
     * description: 获取所有的数据源 date: 7:43 PM 5/19/2017
     */
    public Collection<OfflineDatasourceAction.DatasourceDb> getDatasourceInfo() throws Exception {
        DatasourceDbCriteria criteria = new DatasourceDbCriteria();
        criteria.createCriteria();
        List<DatasourceDb> dbList = workflowDAOFacade.getDatasourceDbDAO().selectByExample(criteria);
        DatasourceTableCriteria tableCriteria = new DatasourceTableCriteria();
        tableCriteria.createCriteria();
        List<DatasourceTable> tableList = workflowDAOFacade.getDatasourceTableDAO().selectByExample(tableCriteria);
        Map<Integer, OfflineDatasourceAction.DatasourceDb> dbsMap = new HashMap<>();
        for (DatasourceDb datasourceDb : dbList) {
            OfflineDatasourceAction.DatasourceDb datasourceDb1 = new OfflineDatasourceAction.DatasourceDb();
            datasourceDb1.setId(datasourceDb.getId());
            datasourceDb1.setName(datasourceDb.getName());
            datasourceDb1.setSyncOnline(datasourceDb.getSyncOnline());
            dbsMap.put(datasourceDb.getId(), datasourceDb1);
        }
        for (DatasourceTable datasourceTable : tableList) {
            int dbId = datasourceTable.getDbId();
            if (dbsMap.containsKey(dbId)) {
                OfflineDatasourceAction.DatasourceDb datasourceDb = dbsMap.get(dbId);
                datasourceDb.addTable(datasourceTable);
            } else {
                throw new IllegalStateException(datasourceTable + "找不到对应的db, dbId=" + datasourceTable.getDbId());
            }
        }
        return dbsMap.values();
    }

    public DBConfigSuit getDbConfig(Integer dbId) {
        DatasourceDb db = getDB(dbId);
        DBConfigSuit dbSuit = new DBConfigSuit();
        List<String> childPath = GitUtils.$().listDbConfigPath(db.getName());
        if (childPath.size() < 1) {
            throw new IllegalStateException("db:" + db.getName() + " relevant db meta config is empty");
        }
        DBConfig detailed = GitUtils.$().getDbConfig(db.getName(), DbScope.DETAILED);
        dbSuit.setDetailed(detailed);
        if (childPath.contains(GitUtils.DB_CONFIG_META_NAME + DbScope.FACADE.getDBType())) {
            // 
            DBConfig facade = GitUtils.$().getDbConfig(db.getName(), DbScope.FACADE);
            dbSuit.setFacade(facade);
        }
        return dbSuit;
    }

    public DatasourceDb getDB(Integer dbId) {
        DatasourceDb db = workflowDAOFacade.getDatasourceDbDAO().selectByPrimaryKey(dbId);
        if (db == null) {
            throw new IllegalStateException("dbid:" + dbId + " can not find relevant db object in DB");
        }
        return db;
    }

    public TISTable getTableConfig(Integer tableId) {
        DatasourceTable tab = this.workflowDAOFacade.getDatasourceTableDAO().selectByPrimaryKey(tableId);
        DatasourceDb db = this.workflowDAOFacade.getDatasourceDbDAO().selectByPrimaryKey(tab.getDbId());
        TISTable t = GitUtils.$().getTableConfig(db.getName(), tab.getTableLogicName());
        t.setTabId(tableId);
        t.setDbId(db.getId());
        return t;
    }

    public WorkflowPojo getWorkflowConfig(Integer workflowId, boolean isMaster) {
        WorkFlow workFlow = this.workflowDAOFacade.getWorkFlowDAO().selectByPrimaryKey(workflowId);
        if (workFlow == null) {
            throw new IllegalStateException("workflow obj is null");
        }
        return GitUtils.$().getWorkflow(workFlow.getName(), GitBranchInfo.$("xxxxxxxxxxxxxx"));
    }

    // public WorkflowPojo getWorkflowConfig(String name, boolean isMaster) {
    // return
    // }
    // public WorkflowPojo getWorkflowConfig(String name, String sha) {
    // return GitUtils.$().getWorkflowSha(GitUtils.WORKFLOW_GIT_PROJECT_ID, sha,
    // name);
    // }
    public void deleteWorkflow(int id, BasicModule action, Context context) {
        WorkFlow workFlow = workflowDAOFacade.getWorkFlowDAO().selectByPrimaryKey(id);
        if (workFlow == null) {
            action.addErrorMessage(context, "数据库没有这条记录");
            return;
        }
        if (workFlow.getInChange().intValue() != 0) {
            action.addErrorMessage(context, "该工作流在变更中，无法删除");
            return;
        }
        ApplicationCriteria applicationCriteria = new ApplicationCriteria();
        applicationCriteria.createCriteria().andWorkFlowIdEqualTo(id);
        List<Application> applications = action.getApplicationDAO().selectByExample(applicationCriteria);
        if (!CollectionUtils.isEmpty(applications)) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Application application : applications) {
                stringBuilder.append(application.getProjectName()).append(", ");
            }
            action.addErrorMessage(context, "请先删除与该工作流相关的索引，相关索引为" + stringBuilder.toString());
            return;
        }
        try {
            GitUser user = GitUser.dft();
            // delete git
            GitUtils.$().deleteWorkflow(workFlow.getName(), user);
            // delete db
            workflowDAOFacade.getWorkFlowDAO().deleteByPrimaryKey(id);
            // TODO 删除线上db的数据，发送一个http请求
            action.addActionMessage(context, "工作流删除成功");
        } catch (Exception e) {
            action.addErrorMessage(context, "工作流删除失败");
            action.addErrorMessage(context, e.getMessage());
        }
    }

    /**
     * 校验是否存在相同的表
     *
     * @param tableLogicName
     * @return
     */
    public boolean checkTableLogicNameRepeat(String tableLogicName, DatasourceDb db) {
        if (db == null) {
            throw new IllegalStateException(" database can not be null");
        }
        DatasourceTableCriteria criteria = new DatasourceTableCriteria();
        criteria.createCriteria().andTableLogicNameEqualTo(tableLogicName).andDbIdEqualTo(db.getId());
        int tableCount = workflowDAOFacade.getDatasourceTableDAO().countByExample(criteria);
        return tableCount > 0;
    }

    // 
    // public void syncDb(int id, String dbName, BasicModule action, Context
    // context) {
    // DatasourceDbCriteria criteria = new DatasourceDbCriteria();
    // criteria.createCriteria().andIdEqualTo(id).andNameEqualTo(dbName);
    // List<DatasourceDb> dbList =
    // this.workflowDAOFacade.getDatasourceDbDAO().selectByExample(criteria);
    // 
    // // 1. 检查是否有这个数据库
    // if (CollectionUtils.isEmpty(dbList)) {
    // action.addErrorMessage(context, "找不到该数据库，id为" + id + "，数据库名为" + dbName);
    // return;
    // }
    // 
    // // 2. 看看数据库的状态
    // DatasourceDb datasourceDb = dbList.get(0);
    // if (datasourceDb.getSyncOnline().intValue() == 1) {
    // action.addErrorMessage(context, "该数据库已经同步了");
    // return;
    // }
    // 
    // // 3. 看看git线上有没有这个文件
    // String gitPah = dbName + "/db_config";
    // boolean onlineGitFileExisted =
    // GitUtils.$().isFileExisted(GitUtils.DATASOURCE_PROJECT_ID_ONLINE,
    // gitPah);
    // if (onlineGitFileExisted) {
    // action.addErrorMessage(context, "线上git已经有该数据库的配置文件了");
    // return;
    // }
    // 
    // // 4. git同步配置文件到线上
    // 
    // try {
    // String file = GitUtils.$().getFileContent(GitUtils.DATASOURCE_PROJECT_ID,
    // gitPah);
    // GitUtils.$().createDatasourceFileOnline(gitPah, file, "add db " +
    // dbName);
    // } catch (Exception e) {
    // action.addErrorMessage(context, "git同步配置文件到线上失败");
    // return;
    // }
    // 
    // // 5. db记录同步到线上
    // // TODO 等线上配好数据库
    // List<HttpUtils.PostParam> params = new LinkedList<>();
    // params.add(new HttpUtils.PostParam("id",
    // datasourceDb.getId().toString()));
    // params.add(new HttpUtils.PostParam("name", datasourceDb.getName()));
    // String url = URL_ONLINE +
    // "&&event_submit_do_sync_db_record=true&resulthandler=advance_query_result";
    // try {
    // HttpUtils.post(new URL(url), params, new PostFormStreamProcess<Boolean>()
    // {
    // 
    // public Boolean p(int status, InputStream stream, String md5) {
    // try {
    // return Boolean.parseBoolean(IOUtils.toString(stream, "utf8"));
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }
    // });
    // } catch (MalformedURLException e) {
    // e.printStackTrace();
    // }
    // 
    // // 6. 日常db状态更改
    // datasourceDb.setSyncOnline(new Byte("1"));
    // this.workflowDAOFacade.getDatasourceDbDAO().updateByExample(datasourceDb,
    // criteria);
    // }
    // 
    // public void syncTable(int id, String tableLogicName, BasicModule action,
    // Context context) {
    // DatasourceTableCriteria criteria = new DatasourceTableCriteria();
    // criteria.createCriteria().andIdEqualTo(id).andTableLogicNameEqualTo(tableLogicName);
    // List<DatasourceTable> tableList =
    // this.workflowDAOFacade.getDatasourceTableDAO().selectByExample(criteria);
    // 
    // // 1. 检查是否有这个数据库
    // if (CollectionUtils.isEmpty(tableList)) {
    // action.addErrorMessage(context, "找不到该数据库表，id为" + id + "，数据库逻辑名为" +
    // tableLogicName);
    // return;
    // }
    // 
    // // 2. 看看数据库的状态
    // DatasourceTable datasourceTable = tableList.get(0);
    // if (datasourceTable.getSyncOnline().intValue() == 1) {
    // action.addErrorMessage(context, "该数据库表已经同步了");
    // return;
    // }
    // int dbId = datasourceTable.getDbId();
    // DatasourceDb datasourceDb =
    // this.workflowDAOFacade.getDatasourceDbDAO().selectByPrimaryKey(dbId);
    // 
    // // 3. 看看git线上有没有这个文件
    // String gitPah = datasourceDb.getName() + "/" + tableLogicName;
    // boolean onlineGitFileExisted =
    // GitUtils.$().isFileExisted(GitUtils.DATASOURCE_PROJECT_ID_ONLINE,
    // gitPah);
    // if (onlineGitFileExisted) {
    // action.addErrorMessage(context, "线上git已经有该数据库表的配置文件了");
    // return;
    // }
    // 
    // // 4. git同步配置文件到线上
    // try {
    // String file = GitUtils.$().getFileContent(GitUtils.DATASOURCE_PROJECT_ID,
    // gitPah);
    // GitUtils.$().createDatasourceFileOnline(gitPah, file, "add table " +
    // gitPah);
    // } catch (Exception e) {
    // action.addErrorMessage(context, "git同步配置文件到线上失败");
    // return;
    // }
    // 
    // // 5. db记录同步到线上
    // // TODO 等线上配好数据库
    // List<HttpUtils.PostParam> params = new LinkedList<>();
    // params.add(new HttpUtils.PostParam("id",
    // datasourceTable.getId().toString()));
    // params.add(new HttpUtils.PostParam("name", datasourceTable.getName()));
    // params.add(new HttpUtils.PostParam("table_logic_name",
    // datasourceTable.getTableLogicName()));
    // params.add(new HttpUtils.PostParam("db_id",
    // datasourceTable.getDbId().toString()));
    // params.add(new HttpUtils.PostParam("git_tag",
    // datasourceTable.getGitTag()));
    // String url = URL_ONLINE +
    // "&&event_submit_do_sync_table_record=true&resulthandler=advance_query_result";
    // try {
    // HttpUtils.post(new URL(url), params, new PostFormStreamProcess<Boolean>()
    // {
    // 
    // public Boolean p(int status, InputStream stream, String md5) {
    // try {
    // return Boolean.parseBoolean(IOUtils.toString(stream, "utf8"));
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }
    // });
    // } catch (MalformedURLException e) {
    // e.printStackTrace();
    // }
    // 
    // // 6. 日常db状态更改
    // datasourceTable.setSyncOnline(new Byte("1"));
    // this.workflowDAOFacade.getDatasourceTableDAO().updateByExample(datasourceTable,
    // criteria);
    // }
    public void syncDbRecord(DatasourceDb datasourceDb, BasicModule action, Context context) {
        try {
            this.workflowDAOFacade.getDatasourceDbDAO().insert(datasourceDb);
            action.setBizResult(context, true);
        } catch (Exception e) {
            action.addErrorMessage(context, e.getMessage());
            action.setBizResult(context, false);
        }
    }

    public void syncTableRecord(DatasourceTable datasourceTable, BasicModule action, Context context) {
        try {
            this.workflowDAOFacade.getDatasourceTableDAO().insert(datasourceTable);
            action.setBizResult(context, true);
        } catch (Exception e) {
            action.addErrorMessage(context, e.getMessage());
            action.setBizResult(context, false);
        }
    }

    private static boolean isXmlValid(JoinRule xmlStr) {
        boolean result = true;
        try {
            StringReader sr = new StringReader(xmlStr.getContent());
            InputSource is = new InputSource(sr);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(is);
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    // /**
    // * description: 使某个table的dump失效 date: 11:20 AM 6/17/2017
    // */
    // public void disableTableDump(int tableId, BasicModule action, Context context) {
    // TableDumpCriteria criteria = new TableDumpCriteria();
    // criteria.createCriteria().andDatasourceTableIdEqualTo(tableId).andStateEqualTo(new Byte("1")).andIsValidEqualTo(new Byte("1"));
    // criteria.setOrderByClause("op_time desc");
    // // 取出最新的一个合法的dump
    // List<TableDump> tableDumps = this.workflowDAOFacade.getTableDumpDAO().selectByExampleWithoutBLOBs(criteria, 1, 1);
    // if (CollectionUtils.isEmpty(tableDumps)) {
    // return;
    // }
    // TableDump tableDump = tableDumps.get(0);
    // tableDump.setIsValid(new Byte("0"));
    // criteria = new TableDumpCriteria();
    // criteria.createCriteria().andIdEqualTo(tableDump.getId());
    // // 更新它
    // this.workflowDAOFacade.getTableDumpDAO().updateByExampleSelective(tableDump, criteria);
    // }
    // /**
    // * description: 使某个db的所有table的dump失效 date: 11:20 AM 6/17/2017
    // */
    // public void disableDbDump(int dbId, BasicModule action, Context context) {
    // DatasourceTableCriteria tableCriteria = new DatasourceTableCriteria();
    // tableCriteria.createCriteria().andDbIdEqualTo(dbId);
    // List<DatasourceTable> datasourceTables = this.workflowDAOFacade.getDatasourceTableDAO().selectByExample(tableCriteria);
    // if (CollectionUtils.isEmpty(datasourceTables)) {
    // return;
    // }
    // // 对所有的table处理
    // for (DatasourceTable datasourceTable : datasourceTables) {
    // this.disableTableDump(datasourceTable.getId(), action, context);
    // }
    // }
    public void deleteDatasourceDbById(int dbId, BasicModule action, Context context) {
        // 1 先检查db是否存在
        DatasourceDb db = this.workflowDAOFacade.getDatasourceDbDAO().selectByPrimaryKey(dbId);
        if (db == null) {
            action.addErrorMessage(context, "找不到该db，db id = " + dbId);
            return;
        }
        // 2 检查还有表没
        DatasourceTableCriteria tableCriteria = new DatasourceTableCriteria();
        tableCriteria.createCriteria().andDbIdEqualTo(dbId);
        List<DatasourceTable> datasourceTables = this.workflowDAOFacade.getDatasourceTableDAO().minSelectByExample(tableCriteria);
        if (!CollectionUtils.isEmpty(datasourceTables)) {
            action.addErrorMessage(context, "该数据库下仍然有数据表，请先删除所有的表");
            return;
        }
        GitUser user = GitUser.dft();
        // 3 删除git
        GitUtils.$().deleteDb(db.getName(), user);
        // 4 删除db
        this.workflowDAOFacade.getDatasourceDbDAO().deleteByPrimaryKey(dbId);
        action.addActionMessage(context, "成功删除'" + db.getName() + "'");
    }

    public void deleteDatasourceTableById(int tableId, BasicModule action, Context context) {
        // 1 检查表是否存在
        DatasourceTable datasourceTable = this.workflowDAOFacade.getDatasourceTableDAO().selectByPrimaryKey(tableId);
        if (datasourceTable == null) {
            action.addErrorMessage(context, "找不到该表，table id = " + tableId);
            return;
        }
        // 2 检查对应的db是否存在
        Integer dbId = datasourceTable.getDbId();
        DatasourceDb datasourceDb = this.workflowDAOFacade.getDatasourceDbDAO().selectByPrimaryKey(dbId);
        if (datasourceDb == null) {
            action.addErrorMessage(context, "找不到该表对应的数据库，db id = " + dbId);
            return;
        }
        // 3 检查是否有工作流用到了该table
        WorkFlowCriteria workFlowCriteria = new WorkFlowCriteria();
        workFlowCriteria.createCriteria();
        List<WorkFlow> workFlows = this.workflowDAOFacade.getWorkFlowDAO().selectByExample(workFlowCriteria);
        for (WorkFlow workFlow : workFlows) {
        // WorkflowPojo workflowPojo = getWorkflowConfig(workFlow.getName(), true);
        // if (!CollectionUtils.isEmpty(workflowPojo.getDependTableIds())
        // && workflowPojo.getDependTableIds().contains(datasourceTable.getId())) {
        // action.addErrorMessage(context,
        // "数据库下面的表" + datasourceTable.getTableLogicName() + "仍然被工作流" +
        // workflowPojo.getName() + "占用");
        // return;
        // }
        }
        // 4 删除git
        try {
            GitUser user = GitUser.dft();
            RunEnvironment runEnvironment = action.getAppDomain().getRunEnvironment();
            if (RunEnvironment.DAILY.equals(runEnvironment)) {
                GitUtils.$().deleteTableDaily(datasourceDb.getName(), datasourceTable.getTableLogicName(), user);
            } else if (RunEnvironment.ONLINE.equals(runEnvironment)) {
                GitUtils.$().deleteTableOnline(datasourceDb.getName(), datasourceTable.getTableLogicName(), user);
            } else {
                action.addErrorMessage(context, "当前运行环境" + runEnvironment + "既不是daily也不是online");
                return;
            }
        } catch (Exception e) {
            action.addErrorMessage(context, "删除数据库失败");
            action.addErrorMessage(context, e.getMessage());
            return;
        }
        // 5 删除db
        this.workflowDAOFacade.getDatasourceTableDAO().deleteByPrimaryKey(tableId);
        action.addActionMessage(context, "成功删除table");
    }

    public void deleteWorkflowChange(int workflowId, BasicModule action, Context context) {
        WorkFlow workFlow = this.workflowDAOFacade.getWorkFlowDAO().selectByPrimaryKey(workflowId);
        if (workFlow == null) {
            action.addErrorMessage(context, "找不到id为" + workflowId + "的工作流");
            return;
        }
        if (workFlow.getInChange().intValue() == 0) {
            action.addErrorMessage(context, "id为" + workflowId + "的工作流不在变更中");
            return;
        }
        try {
            GitUtils.$().deleteWorkflowBranch(workFlow.getName());
        } catch (Exception e) {
            e.printStackTrace();
            action.addErrorMessage(context, "删除分支" + workFlow.getName() + "失败");
            action.addErrorMessage(context, e.getMessage());
            return;
        }
        int inChange = workFlow.getInChange().intValue();
        if (inChange == 1) {
            // 新建工作流变更中 直接删除db
            this.workflowDAOFacade.getWorkFlowDAO().deleteByPrimaryKey(workflowId);
        } else if (inChange == 2) {
            // 对已有的工作流进行变更
            workFlow.setInChange(new Byte("0"));
            WorkFlowCriteria criteria = new WorkFlowCriteria();
            criteria.createCriteria().andIdEqualTo(workflowId);
            this.workflowDAOFacade.getWorkFlowDAO().updateByExample(workFlow, criteria);
        }
        // 更新变更记录
        WorkFlowPublishHistoryCriteria criteria1 = new WorkFlowPublishHistoryCriteria();
        criteria1.createCriteria().andWorkflowIdEqualTo(workflowId).andPublishStateEqualTo(new Byte("3"));
        List<WorkFlowPublishHistory> workFlowPublishHistories = this.workflowDAOFacade.getWorkFlowPublishHistoryDAO().selectByExampleWithoutBLOBs(criteria1);
        if (CollectionUtils.isEmpty(workFlowPublishHistories)) {
            action.addErrorMessage(context, "找不到这条变更记录");
            return;
        } else {
            WorkFlowPublishHistory workFlowPublishHistory = workFlowPublishHistories.get(0);
            workFlowPublishHistory.setPublishState(new Byte("2"));
            this.workflowDAOFacade.getWorkFlowPublishHistoryDAO().updateByExampleWithoutBLOBs(workFlowPublishHistory, criteria1);
        }
        action.addActionMessage(context, "删除变更成功");
    }

    public void confirmWorkflowChange(int workflowId, BasicModule action, Context context) {
        WorkFlow workFlow = this.workflowDAOFacade.getWorkFlowDAO().selectByPrimaryKey(workflowId);
        if (workFlow == null) {
            action.addErrorMessage(context, "找不到id为" + workflowId + "的工作流");
            return;
        }
        int inChange = workFlow.getInChange().intValue();
        if (inChange == 0) {
            action.addErrorMessage(context, "id为" + workflowId + "的工作流不在变更中");
            return;
        }
        // git 合并
        try {
            GitUtils.$().mergeWorkflowChange(workFlow.getName());
        } catch (Exception e) {
            action.addErrorMessage(context, "git分支合并失败");
            action.addErrorMessage(context, "此次变更没有任何配置变动，请撤销变更");
            action.addErrorMessage(context, e.getMessage());
            return;
        }
        // db 变更
        if (inChange == 1) {
        // 新建工作流变更中 同步到线上
        // TODO 发送一个请求给线上的console 要求加入一条db记录
        }
        // 对已有的工作流进行变更
        workFlow.setInChange(new Byte("0"));
        WorkFlowCriteria criteria = new WorkFlowCriteria();
        criteria.createCriteria().andIdEqualTo(workflowId);
        this.workflowDAOFacade.getWorkFlowDAO().updateByExample(workFlow, criteria);
        // 变更历史
        WorkFlowPublishHistoryCriteria criteria1 = new WorkFlowPublishHistoryCriteria();
        criteria1.createCriteria().andWorkflowIdEqualTo(workflowId).andPublishStateEqualTo(new Byte("3"));
        List<WorkFlowPublishHistory> workFlowPublishHistories = this.workflowDAOFacade.getWorkFlowPublishHistoryDAO().selectByExampleWithoutBLOBs(criteria1);
        if (CollectionUtils.isEmpty(workFlowPublishHistories)) {
            action.addErrorMessage(context, "找不到这条变更记录");
            return;
        } else {
            // 把之前在使用中的变为未使用
            WorkFlowPublishHistoryCriteria inUseCriteria = new WorkFlowPublishHistoryCriteria();
            inUseCriteria.createCriteria().andWorkflowIdEqualTo(workflowId).andInUseEqualTo(true);
            WorkFlowPublishHistory notInUse = new WorkFlowPublishHistory();
            notInUse.setInUse(false);
            this.workflowDAOFacade.getWorkFlowPublishHistoryDAO().updateByExampleSelective(notInUse, inUseCriteria);
            // 把新建的记录给确定下来
            WorkFlowPublishHistory workFlowPublishHistory = workFlowPublishHistories.get(0);
            workFlowPublishHistory.setPublishState(new Byte("1"));
            workFlowPublishHistory.setGitSha1(GitUtils.$().getLatestSha(GitUtils.WORKFLOW_GIT_PROJECT_ID));
            workFlowPublishHistory.setInUse(true);
            this.workflowDAOFacade.getWorkFlowPublishHistoryDAO().updateByExampleSelective(workFlowPublishHistory, criteria1);
        }
        action.addActionMessage(context, "变更提交成功");
    }

    public List<WorkFlowPublishHistory> getWorkflowChanges(int page) {
        WorkFlowPublishHistoryCriteria criteria = new WorkFlowPublishHistoryCriteria();
        criteria.createCriteria();
        criteria.setOrderByClause("id desc");
        return this.workflowDAOFacade.getWorkFlowPublishHistoryDAO().selectByExampleWithBLOBs(criteria, page, 20);
    }

    public List<WorkFlowPublishHistory> getWorkflowChanges(int page, Integer workflowId) {
        WorkFlowPublishHistoryCriteria criteria = new WorkFlowPublishHistoryCriteria();
        criteria.createCriteria().andWorkflowIdEqualTo(workflowId);
        criteria.setOrderByClause("id desc");
        return this.workflowDAOFacade.getWorkFlowPublishHistoryDAO().selectByExampleWithBLOBs(criteria, page, 20);
    }

    public void createWorkflowChange(String changeReason, String workflowName, BasicModule action, Context context) {
        WorkFlowCriteria workFlowCriteria = new WorkFlowCriteria();
        workFlowCriteria.createCriteria().andNameEqualTo(workflowName);
        List<WorkFlow> workFlows = this.workflowDAOFacade.getWorkFlowDAO().selectByExample(workFlowCriteria);
        if (CollectionUtils.isEmpty(workFlows)) {
            action.addErrorMessage(context, "找不到工作流" + workflowName);
            return;
        }
        WorkFlow workFlow = workFlows.get(0);
        if (workFlow.getInChange().intValue() != 0) {
            action.addErrorMessage(context, "工作流" + workflowName + "已经在变更状态，不能再新建变更了");
            return;
        }
        // 1 git新建分支
        try {
            GitUtils.$().createWorkflowBarnch(workflowName);
        } catch (Exception e) {
            action.addErrorMessage(context, "git分支创建失败");
            action.addErrorMessage(context, e.getMessage());
            return;
        }
        // 2 创建一个新的变更
        WorkFlowPublishHistory workFlowPublishHistory = new WorkFlowPublishHistory();
        workFlowPublishHistory.setCreateTime(new Date());
        workFlowPublishHistory.setOpUserId(1);
        workFlowPublishHistory.setOpUserName(action.getUser().getName());
        workFlowPublishHistory.setWorkflowId(workFlow.getId());
        workFlowPublishHistory.setWorkflowName(workflowName);
        workFlowPublishHistory.setPublishState(new Byte("3"));
        workFlowPublishHistory.setType(new Byte("2"));
        workFlowPublishHistory.setPublishReason(changeReason);
        workFlowPublishHistory.setInUse(false);
        this.workflowDAOFacade.getWorkFlowPublishHistoryDAO().insert(workFlowPublishHistory);
        // 3 工作流状态改为变更中
        workFlow.setInChange(new Byte("2"));
        this.workflowDAOFacade.getWorkFlowDAO().updateByExample(workFlow, workFlowCriteria);
    }
    // public void useWorkflowChange(Integer id, BasicModule action, Context
    // context) {
    // WorkFlowPublishHistory workFlowPublishHistory =
    // this.workflowDAOFacade.getWorkFlowPublishHistoryDAO()
    // .selectByPrimaryKey(id);
    // // 1. 检查变更是否存在
    // if (workFlowPublishHistory == null) {
    // action.addErrorMessage(context, "找不到id为" + id + "的变更");
    // return;
    // }
    // 
    // // 2. 检查变更是否在使用中
    // if (workFlowPublishHistory.getInUse()) {
    // action.addErrorMessage(context, "id为" + id + "的变更正在使用中");
    // return;
    // }
    // 
    // // 3. 检查变更是否已经提交
    // if (workFlowPublishHistory.getPublishState().intValue() != 1) {
    // action.addErrorMessage(context, "id为" + id + "的变更还没有完成，请在完成之后再使用变更");
    // return;
    // }
    // 
    // // 4. 检查变更类型是否为'修改'
    // if (workFlowPublishHistory.getType().intValue() != 2) {
    // action.addErrorMessage(context, "只有变更类型为'修改'的变更才能回滚");
    // return;
    // }
    // 
    // int workflowId = workFlowPublishHistory.getWorkflowId();
    // WorkFlow workFlow =
    // this.workflowDAOFacade.getWorkFlowDAO().selectByPrimaryKey(workflowId);
    // 
    // // // 5. 检查对应的工作流是否存在
    // // if (workFlow == null) {
    // // action.addErrorMessage(context, "找不到id为" + workflowId + "的变更");
    // // return;
    // // }
    // //
    // // // 6. 检查工作流的状态是否为变更中
    // // if (workFlow.getInChange().intValue() != 0) {
    // // action.addErrorMessage(context, "工作流'" + workFlow.getName() +
    // // "'还在变更中");
    // // return;
    // // }
    // 
    // String workflowName = workFlowPublishHistory.getWorkflowName();
    // // 7. 获取此版本的文件
    // WorkflowPojo gitWorkflowPojo =
    // GitUtils.$().getWorkflowSha(GitUtils.WORKFLOW_GIT_PROJECT_ID,
    // workFlowPublishHistory.getGitSha1(), workflowName);
    // JSONObject jsonObject = new JSONObject();
    // jsonObject.put("name", gitWorkflowPojo.getName());
    // jsonObject.put("tables",
    // StringUtils.join(gitWorkflowPojo.getDependTableIds(), ","));
    // jsonObject.put("task", gitWorkflowPojo.getTask());
    // 
    // // 8. 更新git，重新做一次提交
    // try {
    // GitUtils.$().updateWorkflowFile(workflowName, "master",
    // jsonObject.toString(1),
    // "revert workflow " + workflowName + " and sha=" +
    // workFlowPublishHistory.getGitSha1());
    // } catch (JSONException e) {
    // action.addErrorMessage(context, "回滚文件失败");
    // action.addErrorMessage(context, e.getMessage());
    // return;
    // }
    // 
    // // 9. 把原来的使用中变为未使用
    // WorkFlowPublishHistoryCriteria inUseCriteria = new
    // WorkFlowPublishHistoryCriteria();
    // inUseCriteria.createCriteria().andWorkflowIdEqualTo(workFlowPublishHistory.getWorkflowId())
    // .andInUseEqualTo(true);
    // WorkFlowPublishHistory noInUse = new WorkFlowPublishHistory();
    // noInUse.setInUse(false);
    // this.workflowDAOFacade.getWorkFlowPublishHistoryDAO().updateByExampleSelective(noInUse,
    // inUseCriteria);
    // 
    // // 10. 把最新的变为使用中
    // inUseCriteria.clear();
    // inUseCriteria.createCriteria().andIdEqualTo(id);
    // workFlowPublishHistory.setInUse(true);
    // this.workflowDAOFacade.getWorkFlowPublishHistoryDAO().updateByExampleSelective(workFlowPublishHistory,
    // inUseCriteria);
    // 
    // // 11. 把最新的变更传过去
    // action.setBizResult(context, this.getWorkflowChanges(1, workflowId));
    // }
}
