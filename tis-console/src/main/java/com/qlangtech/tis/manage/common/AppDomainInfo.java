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
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.manage.biz.dal.pojo.AppType;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class AppDomainInfo {

  private final Integer dptid;

  private final Integer appid;

  private final RunEnvironment runEnvironment;

  // private final RunContext context;
  private final Application app;

  // private String appName;
  private boolean isAutoDeploy;

  public boolean isAutoDeploy() {
    return isAutoDeploy;
  }

  public void setAutoDeploy(boolean isAutoDeploy) {
    this.isAutoDeploy = isAutoDeploy;
  }

  public AppDomainInfo(Integer bizid, Integer appid, RunEnvironment runEnvironment, Application app) {
    super();
    judgeNull(app);
    this.dptid = bizid;
    this.appid = appid;
    this.runEnvironment = runEnvironment;
    // this.context = context;
    this.app = app;
    // return application.getProjectName();
  }

  public final AppType getAppType() {
    return this.getApp().parseAppType();
  }

  public Application getApp() {
    if (this.app == null) {
      throw new IllegalStateException("app can not be null");
    }
    return app;
  }

  public String getAppName() {
    return this.app.getProjectName();
  }


  /**
   * 创建于应用无关的当前环境
   *
   * @param runEnvironment
   * @return
   */
  public static AppDomainInfo createAppNotAware(RunEnvironment runEnvironment) {
    return new EnvironmentAppDomainInfo(runEnvironment);
  }

  public static class EnvironmentAppDomainInfo extends AppDomainInfo {

    public EnvironmentAppDomainInfo(RunEnvironment runEnvironment) {
      super(-1, -1, runEnvironment, (Application) null);
    }

    @Override
    public String getAppName() {
      return StringUtils.EMPTY;
    }

    @Override
    protected void judgeNull(Application context) {
    }
  }

  protected void judgeNull(Application app) {
    // context) {
    if (app == null) {
      throw new IllegalArgumentException("app can not be null");
    }
  }

  public static String getRunEnvir(int runEnvironment) {
    return RunEnvironment.getEnum((short) runEnvironment).getDescribe();
  }

  public RunEnvironment getRunEnvironment() {
    return runEnvironment;
  }

  // public Integer getBizid() {
  // return dptid;
  // }
  public Integer getDptid() {
    return dptid;
  }

  public Integer getAppid() {
    return appid;
  }

  @Override
  public String toString() {
    return "{" +
      "appid=" + appid +
      ", runEnvironment=" + runEnvironment +
      '}';
  }
}
