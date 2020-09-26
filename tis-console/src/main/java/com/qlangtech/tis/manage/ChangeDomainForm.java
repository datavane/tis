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
package com.qlangtech.tis.manage;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ChangeDomainForm {

    private Integer bizid;

    private Integer appid;

    private Integer runEnviron;

    private String gobackurl;

    public String getGobackurl() {
        return gobackurl;
    }

    public void setGobackurl(String gobackurl) {
        this.gobackurl = gobackurl;
    }

    public Integer getBizid() {
        return bizid;
    }

    public void setBizid(Integer bizid) {
        this.bizid = bizid;
    }

    public Integer getAppid() {
        return appid;
    }

    public void setAppid(Integer appid) {
        this.appid = appid;
    }

    public Integer getRunEnviron() {
        return runEnviron;
    }

    public void setRunEnviron(Integer runEnviron) {
        this.runEnviron = runEnviron;
    }
}
