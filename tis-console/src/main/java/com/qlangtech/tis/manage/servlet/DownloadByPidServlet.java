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
package com.qlangtech.tis.manage.servlet;

import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.pubhook.common.ConfigConstant;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通过包和snapshot来下载资源包
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-30
 */
public class DownloadByPidServlet extends DownloadServlet {

  private static final long serialVersionUID = -8824528411218625478L;

  // private static final Pattern download_pattern = Pattern
  // .compile(".+?/download/bypid/(\\d+)/snid/(\\d+)/("
  // + replace(ConfigConstant.FILE_APPLICATION + "|"
  // + ConfigConstant.FILE_DATA_SOURCE + "|"
  // + ConfigConstant.FILE_SCHEMA + "|"
  // + ConfigConstant.FILE_SOLOR) + "|"
  // + DownloadResource.JAR_NAME + ")");
  private static final Pattern download_pattern = Pattern.compile(".+?/download/snid/(\\d+)/(" + replace(ConfigConstant.FILE_SCHEMA + "|" + ConfigConstant.FILE_SOLR) + "|" + DownloadResource.JAR_NAME + ")");

  // @Override
  // protected void doGet(HttpServletRequest reqeust,
  // HttpServletResponse response) throws ServletException, IOException {
  //
  // }
  @Override
  protected DownloadResource getDownloadResource(Matcher matcher) {
    // AppPackage pack = this.getContext().getAppPackageDAO()
    // .selectByPrimaryKey(Integer.parseInt(matcher.group(1)));
    SnapshotDomain snapshot = this.getContext().getSnapshotViewDAO().getView(Integer.parseInt(matcher.group(1)));
    // Snapshot snapshot, String resourceName
    return new DownloadResource(this.getContext().getApplicationDAO().selectByPrimaryKey(snapshot.getAppId()), snapshot, matcher.group(2));
    // return super.getDownloadResource(matcher);
    // return resource;
  }

  @Override
  protected StringBuffer getAppendInfo(HttpServletRequest request, DownloadResource downloadRes) throws IOException {
    return new StringBuffer();
  }

  @Override
  protected Pattern getURLPattern() {
    return download_pattern;
  }
}
