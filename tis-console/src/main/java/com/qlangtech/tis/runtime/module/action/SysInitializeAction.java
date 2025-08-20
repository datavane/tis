/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.runtime.module.action;

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.model.UpdateCenter;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.SnapshotCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.Config.SysDBType;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.MockContext;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.manage.spring.TISDataSourceFactory;
import com.qlangtech.tis.manage.spring.TISDataSourceFactory.SystemDBInit;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.UploadJarAction.ConfigContentGetter;
import com.qlangtech.tis.utils.FreshmanReadmeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 系统初始化
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SysInitializeAction extends BasicModule {

  private static final long serialVersionUID = 1L;
  private static final Logger logger = LoggerFactory.getLogger(SysInitializeAction.class);

  public static final int DEPARTMENT_DEFAULT_ID = 2;
  private static final int DEPARTMENT_ROOT_ID = 1;
//  public static final int TEMPLATE_APPLICATION_DEFAULT_ID = 1;

  public static final String ADMIN_ID = "9999";
  public static final String ADMIN_NAME = "admin";

 // public static final String APP_NAME_TEMPLATE = "search4template";
  private static final Pattern PATTERN_ZK_ADDRESS = Pattern.compile("([^/]+)(/.+)$");

  private static Boolean _isSysInitialized;


  /**
   * 当使用TIS作为docker容器启动，本地data目录作为容器卷，初始状态是空的，需要将空的卷初始化
   *
   * @param context
   * @throws Exception
   */

  public void doInit(Context context) throws Exception {
    // 数据还没有创建 或者 库中还是空的（没有任何表）
    /**
     * 取得SQL并且初始化
     */
    File mysqlInitScript = new File(Config.getDataDir(), "sql/tis_console_mysql.sql");
    this.init(context, true, Config.getDbCfg().dbtype, mysqlInitScript);
  }

  @Override
  public String getReturnCode() {
    return super.getReturnCode();
  }

  /**
   * 执行系统初始化，数据库和zk节点初始化
   *
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    if (args.length < 2) {
      throw new IllegalArgumentException("args.length must big than 0");
    }
    File tisConsoleSqlFile = new File(args[0]);
    if (!tisConsoleSqlFile.exists()) {
      throw new IllegalStateException("tisConsoleSqlFile:" + tisConsoleSqlFile.getAbsolutePath() + " is not exist");
    }
    final String dbType = args[1];
    // initializeDB(tisConsoleSqlFile, SysDBType.parse(dbType));

    SysInitializeAction initAction = createSysInitializeAction();
    initAction.init(MockContext.instance, false, SysDBType.parse(dbType), tisConsoleSqlFile);

  }

  /**
   * @param context
   * @param dockerContainerInit docker容器初始化过程
   * @param sysDBType
   * @throws Exception
   */
  public void init(Context context, boolean dockerContainerInit, SysDBType sysDBType, File tisConsoleSqlFile) throws Exception {

    if (isSysInitialized()) {
      throw new IllegalStateException("tis has initialized:" + getSysInitializedTokenFile().getAbsolutePath());
    }
    Config.TisDbConfig dbCfg = Config.getDbCfg();
    if (sysDBType != dbCfg.dbtype) {
      throw new IllegalStateException("param sysDBType must match with dbCfg:" + dbCfg);
    }
    logger.info("dbCfg detail:{}", dbCfg);
    /**=======================================
     * 下载data目录
     =======================================*/
    if (dockerContainerInit) {
      // 说明是在docker 容器启动过程中
      this.copyDataTarToLocal();
      if (sysDBType == SysDBType.DERBY) {
        // data目录中已经将derby数据库初始化完成了
        touchSysInitializedToken();
        return;
      }
    }

    SystemDBInit dataSource = TISDataSourceFactory.createDataSource(sysDBType, dbCfg, false, true);

    if (dataSource.dbTisConsoleExist(dbCfg)) {
      // 判断数据库是否已经存在？
      UsrDptRelationCriteria c = new UsrDptRelationCriteria();
      c.createCriteria().andUsrIdEqualTo(ADMIN_ID);
      if (this.getUsrDptRelationDAO().countByExample(c) > 0) {
        touchSysInitializedToken();
        //  throw new IllegalStateException("system has initialized successful,shall not initialize again");
        this.addActionMessage(context, "system has initialized successful");
        return;
      }
    }


    initializeDB(tisConsoleSqlFile, sysDBType);

    // 添加一个系统管理员
    this.getUsrDptRelationDAO().addAdminUser();
    this.initializeDepartment();
   // this.initializeAppAndSchema();

    touchSysInitializedToken();
  }

  public static boolean isSysInitialized() {
    if (_isSysInitialized == null) {
      final File sysInitializedToken = getSysInitializedTokenFile();
      _isSysInitialized = sysInitializedToken.exists();
    }
    return _isSysInitialized;
  }



  static File getSysInitializedTokenFile() {
    return new File(Config.getDataDir(), "system_initialized_token");
  }

  private static void touchSysInitializedToken() throws Exception {
    final File sysInitializedToken = getSysInitializedTokenFile();
    //return sysInitializedToken.exists();
    FileUtils.touch(sysInitializedToken);
    _isSysInitialized = null;
  }


  private static void initializeDB(File tisConsoleSqlFile, SysDBType dbType) throws Exception {
    if (!tisConsoleSqlFile.exists()) {
      throw new IllegalStateException("db init script can not be none:" + tisConsoleSqlFile.getAbsolutePath());
    }
    Config.TisDbConfig dbCfg = Config.getDbCfg();
    TISDataSourceFactory.SystemDBInit dsProcess = null;

    try {
      dsProcess = TISDataSourceFactory.createDataSource(dbType, dbCfg, false, true);
      TISDataSourceFactory.systemDBInitThreadLocal.set(dsProcess);
      try (Connection conn = dsProcess.getDS().getConnection()) {
        try (Statement statement = conn.createStatement()) {

          // 初始化TIS数据库
          logger.info("init '" + dbCfg.dbname + "' db and initialize the tables");
          boolean containTisConsole = dsProcess.dbTisConsoleExist(dbCfg, statement);
          List<String> executeSqls = Lists.newArrayList();
          if (!containTisConsole) {
            boolean execSuccess = false;
            try {
              dsProcess.createSysDB(dbCfg, statement);

              for (String sql : convert2BatchSql(dsProcess, tisConsoleSqlFile)) {
                try {
                  if (dsProcess.shallSkip(sql)) {
                    continue;
                  }
                  executeSqls.add(sql);
                  statement.execute(sql);
                  //  statement.addBatch(sql);
                } catch (SQLException e) {
                  logger.error(sql, e);
                  throw e;
                }
              }
              // statement.executeBatch();
              // FileUtils.forceDelete(tisConsoleSqlFile);
              execSuccess = true;
            } catch (SQLException e) {
              throw new RuntimeException(executeSqls.stream().collect(Collectors.joining("\n")), e);
            } finally {
              if (!execSuccess) {
                dsProcess.dropDB(dbCfg, statement);
              }
            }
          }
        }
      }

      Objects.requireNonNull(dsProcess, "dataSource can not be null");

    } finally {
      try {
        dsProcess.close();
      } catch (Throwable e) {
      }
    }
  }


  static SysInitializeAction createSysInitializeAction() {
    SysInitializeAction initAction = new SysInitializeAction();
    //ClassPathXmlApplicationContext tis.application.context.xml src/main/resources/tis.application.mockable.context.xml
    ApplicationContext appContext = new ClassPathXmlApplicationContext(
      "classpath:/tis.application.context.xml", "classpath:/tis.application.mockable.context.xml");
    appContext.getAutowireCapableBeanFactory().autowireBeanProperties(
      initAction, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    return initAction;
  }

  /**
   * 将sql脚本文件转成jdbc batchsql 允许的sql格式
   *
   * @param tisConsoleSqlFile
   * @return
   * @throws Exception
   */
  private static List<String> convert2BatchSql(TISDataSourceFactory.SystemDBInit dataSource, File tisConsoleSqlFile) throws Exception {
    LineIterator lineIt = FileUtils.lineIterator(tisConsoleSqlFile, TisUTF8.getName());
    List<String> batchs = Lists.newArrayList();
    StringBuffer result = new StringBuffer();
    String line = null;
    while (lineIt.hasNext()) {
      line = StringUtils.trimToEmpty(lineIt.nextLine());
      if (StringUtils.startsWith(line, "/*") //
        || StringUtils.startsWith(line, "--") //
        || StringUtils.startsWith(line, "#") //
        || StringUtils.startsWith(line, "//")) {
        continue;
      }

      result.append(line).append(" ");

      if (StringUtils.endsWith(line, ";")) {
        batchs.add(StringUtils.trimToEmpty(dataSource.processSql(result)));
        result = new StringBuffer();
      }

    }
    // String convertSql = result.toString();
    File targetFile = new File(tisConsoleSqlFile.getParent(), tisConsoleSqlFile.getName() + ".convert");
    FileUtils.write(targetFile
      , batchs.stream().collect(Collectors.joining("\n")), TisUTF8.get(), false);
    return batchs;
  }

  void copyDataTarToLocal() throws IOException {
    File tmpDataDir = FileUtils.getTempDirectory();//  Config.getDataDir();
    final File dataDir = Config.getDataDir();
    UpdateCenter.copyDataTarToLocal(tmpDataDir, Optional.of(dataDir));
  }


//  public void initializeAppAndSchema() throws IOException {
//    this.getApplicationDAO().deleteByPrimaryKey(TEMPLATE_APPLICATION_DEFAULT_ID);
//    SnapshotCriteria snapshotQuery = new SnapshotCriteria();
//    snapshotQuery.createCriteria().andAppidEqualTo(TEMPLATE_APPLICATION_DEFAULT_ID);
//    this.getSnapshotDAO().deleteByExample(snapshotQuery);
//
//    ServerGroupCriteria serverGroupQuery = new ServerGroupCriteria();
//    serverGroupQuery.createCriteria().andAppIdEqualTo(TEMPLATE_APPLICATION_DEFAULT_ID);
//    this.getServerGroupDAO().deleteByExample(serverGroupQuery);
//
//    // 添加初始化模板配置
//    Application app = new Application();
//    // app.setAppId(TEMPLATE_APPLICATION_DEFAULT_ID);
//    app.setProjectName(APP_NAME_TEMPLATE);
//    app.setDptId(DEPARTMENT_DEFAULT_ID);
//    app.setDptName("default");
//    app.setIsDeleted("N");
//    app.setManager(ADMIN_NAME);
//    app.setUpdateTime(new Date());
//    app.setCreateTime(new Date());
//    app.setRecept(ADMIN_NAME);
//
//    Integer newAppId = this.getApplicationDAO().insertSelective(app);
//    if (newAppId != TEMPLATE_APPLICATION_DEFAULT_ID) {
//      throw new IllegalStateException("newAppId:" + newAppId + " must equal with " + TEMPLATE_APPLICATION_DEFAULT_ID);
//    }
//
//    app.setAppId(TEMPLATE_APPLICATION_DEFAULT_ID);
//    this.initializeSchemaConfig(app);
//  }


//  void initializeSchemaConfig(Application app) throws IOException {
//    Snapshot snap = new Snapshot();
//    snap.setCreateTime(new Date());
//    snap.setCreateUserId(9999l);
//    snap.setCreateUserName("admin");
//    snap.setUpdateTime(new Date());
//
//    snap.setAppId(app.getAppId());
//    try (InputStream schemainput = this.getClass().getResourceAsStream("/solrtpl/schema.xml.tpl")) {
//      ConfigContentGetter schema = new ConfigContentGetter(ConfigFileReader.FILE_SCHEMA,
//        IOUtils.toString(schemainput, BasicModule.getEncode()));
//      snap = UploadJarAction.processFormItem(this.getDaoContext(), schema, snap);
//    }
//    try (InputStream solrconfigInput = this.getClass().getResourceAsStream("/solrtpl/solrconfig.xml.tpl")) {
//      ConfigContentGetter solrConfig = new ConfigContentGetter(ConfigFileReader.FILE_SOLR,
//        IOUtils.toString(solrconfigInput, BasicModule.getEncode()));
//      snap = UploadJarAction.processFormItem(this.getDaoContext(), solrConfig, snap);
//    }
//    snap.setPreSnId(-1);
//    Integer snapshotId = this.getSnapshotDAO().insertSelective(snap);
//
//    GroupAction.createGroup(RunEnvironment.getSysRuntime(), AddAppAction.FIRST_GROUP_INDEX, app.getAppId(),
//      snapshotId, this.getServerGroupDAO());
//  }


  void initializeDepartment() {

    this.getDepartmentDAO().deleteByPrimaryKey(DEPARTMENT_DEFAULT_ID);
    this.getDepartmentDAO().deleteByPrimaryKey(DEPARTMENT_ROOT_ID);

    // 初始化部门
    Department dpt = new Department();
    //dpt.setDptId(1);
    dpt.setLeaf(false);
    dpt.setGmtCreate(new Date());
    dpt.setGmtModified(new Date());
    dpt.setName("tis");
    dpt.setFullName("/tis");
    dpt.setParentId(-1);
    Integer dptId = this.getDepartmentDAO().insertSelective(dpt);

    dpt = new Department();
    // dpt.setDptId(DEPARTMENT_DEFAULT_ID);
    dpt.setLeaf(true);
    dpt.setGmtCreate(new Date());
    dpt.setGmtModified(new Date());
    dpt.setName("default");
    dpt.setFullName("/tis/default");
    dpt.setParentId(dptId);
    dptId = this.getDepartmentDAO().insertSelective(dpt);
    if (dptId != DEPARTMENT_DEFAULT_ID) {
      throw new IllegalStateException("dptId:" + dptId + " must equal with:" + DEPARTMENT_DEFAULT_ID);
    }
  }


}
