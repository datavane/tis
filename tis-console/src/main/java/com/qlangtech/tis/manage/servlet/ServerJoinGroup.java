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

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-11 13:16
 */
public class ServerJoinGroup {
  private static final Pattern IP_PATTERN = Pattern.compile("//([\\d|\\.]+)");
  private boolean leader;
  private String ipAddress;
  private String replicBaseUrl;
  private short groupIndex;
  private boolean checked;

  public void setLeader(boolean leader) {
    this.leader = leader;
  }

  public void setIpAddress(String coreUrl) {
    this.ipAddress = coreUrl;
  }

  public void setReplicBaseUrl(String baseUrl) {
    this.replicBaseUrl = baseUrl;
  }

  public String getReplicBaseUrl() {
    return this.replicBaseUrl;
  }

  public void setGroupIndex(short i) {
    this.groupIndex = i;
  }

  public void setChecked(boolean b) {
    this.checked = b;
  }

  public short getGroupIndex() {
    return this.groupIndex;
  }

  public String getIpAddress() {
    return this.ipAddress;
  }

  public String getIp() {
    Matcher matcher = IP_PATTERN.matcher(StringUtils.trimToEmpty(this.getIpAddress()));
    if (matcher.find()) {
      return matcher.group(1);
    }
    return StringUtils.EMPTY;
  }
}
