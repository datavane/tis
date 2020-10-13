/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.runtime.module.action;

import com.qlangtech.tis.coredefine.module.action.CoreAction;
import com.qlangtech.tis.manage.biz.dal.pojo.*;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.UploadJarAction.ConfigContentGetter;
import com.qlangtech.tis.solrj.util.ZkUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统初始化
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SysInitializeAction extends BasicModule {

  private static final long serialVersionUID = 1L;

  private static final int DEPARTMENT_DEFAULT_ID = 2;
  private static final int DEPARTMENT_ROOT_ID = 1;
  public static final int TEMPLATE_APPLICATION_DEFAULT_ID = 1;

  public static final String ADMIN_ID = "9999";

  public static final String APP_NAME_TEMPLATE = "search4template";
  private static final Pattern PATTERN_ZK_ADDRESS = Pattern.compile("([^/]+)(/.+)$");


  public static boolean isSysInitialized() {
    final File sysInitializedToken = getSysInitializedTokenFile();
    return sysInitializedToken.exists();
  }

  private static File getSysInitializedTokenFile() {
    return new File(Config.getDataDir(), "system_initialized_token");
  }

  private static void touchSysInitializedToken() throws Exception {
    final File sysInitializedToken = getSysInitializedTokenFile();
    //return sysInitializedToken.exists();
    FileUtils.touch(sysInitializedToken);
  }

  public static void main(String[] args) throws Exception {
    SysInitializeAction initAction = new SysInitializeAction();
    //ClassPathXmlApplicationContext tis.application.context.xml
    ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath:/tis.application.context.xml");
    appContext.getAutowireCapableBeanFactory().autowireBeanProperties(
      initAction, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    initAction.doInit();
  }

  public void doInit() throws Exception {

    // final File sysInitializedToken = new File(Config.getDataDir(), "system_initialized_token");
    if (isSysInitialized()) {
      throw new IllegalStateException("tis has initialized");
    }

    if (!initializeZkPath()) {
      // 初始化ZK失败
      return;
    }


    UsrDptRelationCriteria c = new UsrDptRelationCriteria();
    c.createCriteria().andUsrIdEqualTo(ADMIN_ID);
    if (this.getUsrDptRelationDAO().countByExample(c) > 0) {
      touchSysInitializedToken();
      //this.addActionMessage("系统已经完成初始化，请继续使用");
      throw new IllegalStateException("system has initialized successful,shall not initialize again");
      //return;
    }

    // 添加一个系统管理员
    this.getUsrDptRelationDAO().addAdminUser();

    this.initializeDepartment();

    this.getApplicationDAO().deleteByPrimaryKey(TEMPLATE_APPLICATION_DEFAULT_ID);
    SnapshotCriteria snapshotQuery = new SnapshotCriteria();
    snapshotQuery.createCriteria().andAppidEqualTo(TEMPLATE_APPLICATION_DEFAULT_ID);
    this.getSnapshotDAO().deleteByExample(snapshotQuery);

    ServerGroupCriteria serverGroupQuery = new ServerGroupCriteria();
    serverGroupQuery.createCriteria().andAppIdEqualTo(TEMPLATE_APPLICATION_DEFAULT_ID);
    this.getServerGroupDAO().deleteByExample(serverGroupQuery);

    // 添加初始化模板配置
    Application app = new Application();
    app.setAppId(TEMPLATE_APPLICATION_DEFAULT_ID);
    app.setProjectName(APP_NAME_TEMPLATE);
    app.setDptId(DEPARTMENT_DEFAULT_ID);
    app.setDptName("default");
    app.setIsDeleted("N");
    app.setManager("admin");
    app.setUpdateTime(new Date());
    app.setCreateTime(new Date());
    app.setRecept("admin");

    // final Integer newid =
    this.getApplicationDAO().insertSelective(app);

    // int newAppid = AddAppAction.createApplication(app, context, this,
    // this.triggerContext);
    app.setAppId(TEMPLATE_APPLICATION_DEFAULT_ID);
    this.initializeSchemaConfig(app);
    touchSysInitializedToken();
    //Thread.sleep(1000);
    // this.addActionMessage(context, "初始化系统参数完成");
  }

  // 初始化ZK内容
  private boolean initializeZkPath() {

    Matcher matcher = PATTERN_ZK_ADDRESS.matcher(Config.getZKHost());
    if (!matcher.matches()) {
      //this.addErrorMessage(context, "ZK地址:" + Config.getZKHost() + "不符合规范:" + PATTERN_ZK_ADDRESS);
      throw new IllegalStateException("zk address " + Config.getZKHost() + " is not match " + PATTERN_ZK_ADDRESS);
      // return false;
    }

    final String zkServer = matcher.group(1);
    String zkSubDir = StringUtils.trimToEmpty(matcher.group(2));
    if (StringUtils.endsWith(zkSubDir, "/")) {
      zkSubDir = StringUtils.substring(zkSubDir, 0, zkSubDir.length() - 1);
      // p.setValue(StringUtils.substring(p.getValue(), 0, p.getValue().length() - 1));
    }

    ZooKeeper zk = null;
    try {
      zk = new ZooKeeper(zkServer, 50000, null);
      zk.getChildren("/", false);

      // ZkUtils.guaranteeExist(zk, zkSubDir + ZkStateReader.CLUSTER_STATE, "{}".getBytes());
      ZkUtils.guaranteeExist(zk, zkSubDir + "/tis");
      ZkUtils.guaranteeExist(zk, zkSubDir + "/tis-lock/dumpindex");
      ZkUtils.guaranteeExist(zk, zkSubDir + "/configs/" + CoreAction.DEFAULT_SOLR_CONFIG);

    } catch (Throwable e) {
      // this.addErrorMessage(context, "ZK地址:" + zkServer + ",不能连接Zookeeper主机");
      // return false;
      throw new IllegalStateException("zk address:" + zkServer + " can not connect Zookeeper server", e);
    } finally {
      try {
        zk.close();
      } catch (Throwable e) {

      }
    }

    return true;
  }

  void initializeSchemaConfig(Application app) throws IOException {
    Snapshot snap = new Snapshot();
    snap.setCreateTime(new Date());
    snap.setCreateUserId(9999l);
    snap.setCreateUserName("admin");
    snap.setUpdateTime(new Date());

    snap.setAppId(app.getAppId());
    try (InputStream schemainput = this.getClass().getResourceAsStream("/solrtpl/schema.xml.tpl")) {
      ConfigContentGetter schema = new ConfigContentGetter(ConfigFileReader.FILE_SCHEMA,
        IOUtils.toString(schemainput, getEncode()));
      snap = UploadJarAction.processFormItem(this.getDaoContext(), schema, snap);
    }
    try (InputStream solrconfigInput = this.getClass().getResourceAsStream("/solrtpl/solrconfig.xml.tpl")) {
      ConfigContentGetter solrConfig = new ConfigContentGetter(ConfigFileReader.FILE_SOLR,
        IOUtils.toString(solrconfigInput, getEncode()));
      snap = UploadJarAction.processFormItem(this.getDaoContext(), solrConfig, snap);
    }
    snap.setPreSnId(-1);
    Integer snapshotId = this.getSnapshotDAO().insertSelective(snap);

    GroupAction.createGroup(RunEnvironment.getSysRuntime(), AddAppAction.FIRST_GROUP_INDEX, app.getAppId(),
      snapshotId, this.getServerGroupDAO());
  }

  void initializeDepartment() {

    this.getDepartmentDAO().deleteByPrimaryKey(DEPARTMENT_DEFAULT_ID);
    this.getDepartmentDAO().deleteByPrimaryKey(DEPARTMENT_ROOT_ID);

    // 初始化部门
    Department dpt = new Department();
    dpt.setDptId(1);
    dpt.setLeaf(false);
    dpt.setGmtCreate(new Date());
    dpt.setGmtModified(new Date());
    dpt.setName("tis");
    dpt.setFullName("/tis");
    dpt.setParentId(-1);
    this.getDepartmentDAO().insertSelective(dpt);

    dpt = new Department();
    dpt.setDptId(DEPARTMENT_DEFAULT_ID);
    dpt.setLeaf(true);
    dpt.setGmtCreate(new Date());
    dpt.setGmtModified(new Date());
    dpt.setName("default");
    dpt.setFullName("/tis/default");
    dpt.setParentId(1);
    this.getDepartmentDAO().insertSelective(dpt);
  }
}
