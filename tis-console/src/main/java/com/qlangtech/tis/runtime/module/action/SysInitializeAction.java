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

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.*;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.UploadJarAction.ConfigContentGetter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * 系统初始化
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SysInitializeAction extends BasicModule {

  private static final long serialVersionUID = 1L;

  // private IResourceParametersDAO resourceParametersDAO;
  private static final int DEPARTMENT_DEFAULT_ID = 2;
  private static final int DEPARTMENT_ROOT_ID = 1;
  public static final int TEMPLATE_APPLICATION_DEFAULT_ID = 1;

  public static final String ADMIN_ID = "9999";

  public static final String APP_NAME_TEMPLATE = "search4template";


  public static boolean isSysInitialized() {
    final File sysInitializedToken = getSysInitializedTokenFile();
    return sysInitializedToken.exists();
  }

  private static File getSysInitializedTokenFile() {
    return new File(Config.getDataDir(), "system_initialized_token");
  }

  public static void touchSysInitializedToken() throws Exception {
    final File sysInitializedToken = getSysInitializedTokenFile();
    //return sysInitializedToken.exists();
    FileUtils.touch(sysInitializedToken);
  }

  /**
   * 系统初始化
   *
   * @param context
   */
  public void doInit(Context context) throws Exception {

    // final File sysInitializedToken = new File(Config.getDataDir(), "system_initialized_token");
    if (isSysInitialized()) {
      throw new IllegalStateException("tis has initialized");
    }

    UsrDptRelationCriteria c = new UsrDptRelationCriteria();
    c.createCriteria().andUsrIdEqualTo(ADMIN_ID);
    if (this.getUsrDptRelationDAO().countByExample(c) > 1) {
      touchSysInitializedToken();
      this.addActionMessage(context, "系统已经完成初始化，请继续使用");
      return;
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
    this.initializeSchemaConfig(context, app);
    touchSysInitializedToken();
    Thread.sleep(1000);
    this.addActionMessage(context, "初始化系统参数完成");
  }

  void initializeSchemaConfig(Context context, Application app) throws IOException {
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

    GroupAction.createGroup(context, RunEnvironment.getSysRuntime(), AddAppAction.FIRST_GROUP_INDEX, app.getAppId(),
      snapshotId, this);
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
