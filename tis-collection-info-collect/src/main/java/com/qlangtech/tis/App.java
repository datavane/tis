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
package com.qlangtech.tis;

import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class App {

    private static final String DPT_SEPARATE = "/";

    private String dpt;

    public String getBu() {
        String[] dpts = StringUtils.split(dpt, DPT_SEPARATE);
        for (int i = 0; i < dpts.length; i++) {
            if (StringUtils.isNotBlank(dpts[i])) {
                return dpts[i];
            }
        }
        throw new IllegalStateException("is not a illeal dpt name:" + dpt);
    }

    private String serviceName;

    private Integer appid;

    public String getDpt() {
        String[] dpts = StringUtils.split(dpt, DPT_SEPARATE);
        // if (dpts.length > 2) {
        // return dpts[dpts.length - 2] + DPT_SEPARATE + dpts[dpts.length - 1];
        // }
        // return StringUtils.substringAfterLast(dpt, DPT_SEPARATE);
        StringBuffer dptName = new StringBuffer();
        boolean hasFindBu = false;
        for (int i = 0; i < dpts.length; i++) {
            if (!hasFindBu && StringUtils.isNotBlank(dpts[i])) {
                hasFindBu = true;
                continue;
            }
            if (StringUtils.isNotBlank(dpts[i])) {
                dptName.append(dpts[i]);
                if ((i + 1) < dpts.length) {
                    dptName.append(DPT_SEPARATE);
                }
            }
        }
        return dptName.toString();
    }

    public Integer getAppid() {
        return appid;
    }

    public void setAppid(Integer appid) {
        this.appid = appid;
    }

    public void setDpt(String dpt) {
        this.dpt = dpt;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
