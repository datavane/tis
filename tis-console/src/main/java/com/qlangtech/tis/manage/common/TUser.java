/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.common.apps.AppsFetcher;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TUser implements IUser {

    private final String id;

    private final String name;

    private Integer departmentid;

    private String department;

    private final UsrDptRelation usr;

    private final IAppsFetcher appsFetcher;

    private final RunContext runContext;

    private String wangwang;

    private String email;

    public TUser(UsrDptRelation usr, RunContext runContext) {
        // this(usr,runContext,);
        this.id = usr.getUsrId();
        this.name = usr.getUserName();
        this.runContext = runContext;
        this.usr = usr;
        this.appsFetcher = AppsFetcher.create(this, runContext);
    }

    // public TUser(UsrDptRelation usr, RunContext runContext) {
    // this.usr = usr;
    // this(String.valueOf(usr.getrId()), usr.getUserName(), runContext);
    // 
    // }
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

    public UsrDptRelation getUsr() {
        return usr;
    }

    public IAppsFetcher getAppsFetcher() {
        return appsFetcher;
    }

    @Override
    public String getWangwang() {
        return wangwang;
    }

    @Override
    public void setWangwang(String wangwang) {
        this.wangwang = wangwang;
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
        return usr.getRealName();
    }
}
