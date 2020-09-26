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
package com.qlangtech.tis.manage.common;

import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.common.apps.AppsFetcher;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-1-23
 */
public class TUser implements IUser {

    private final String id;

    private final String name;

    private Integer departmentid;

    private String department;

    private final UsrDptRelation usr;

    private final IAppsFetcher appsFetcher;

    private final RunContext runContext;

    private String email;

    public TUser(UsrDptRelation usr, RunContext runContext) {
        // this(usr,runContext,);
        this.id = usr.getUsrId();
        this.name = usr.getUserName();
        this.runContext = runContext;
        this.usr = usr;
        this.appsFetcher = AppsFetcher.create(this, runContext);
    }

    @Override
    public boolean hasLogin() {
        return true;
    }

    public TUser(UsrDptRelation usr, RunContext runContext, IAppsFetcher appsFetcher) {
        super();
        this.usr = usr;
        this.id = usr.getUsrId();
        this.name = usr.getUserName();
        this.runContext = runContext;
        this.appsFetcher = appsFetcher;
    }

    @JSONField(serialize = false)
    public UsrDptRelation getUsr() {
        return usr;
    }

    @JSONField(serialize = false)
    public IAppsFetcher getAppsFetcher() {
        return appsFetcher;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    public boolean hasGrantAuthority(String permissionCode) {
        return this.appsFetcher.hasGrantAuthority(permissionCode);
    }

    /**
     * 部門id
     *
     * @return
     */
    public Integer getDepartmentid() {
        return departmentid;
    }

    public void setDepartmentid(Integer departmentid) {
        this.departmentid = departmentid;
    }

    /**
     * 部门描述
     *
     * @return
     */
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return usr.getUserName();
    }
}
