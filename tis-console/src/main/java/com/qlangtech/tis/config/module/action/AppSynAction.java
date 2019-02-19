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
package com.qlangtech.tis.config.module.action;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.dao.IDepartmentDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.DefaultFilter;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.manage.common.PropteryGetter;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.servlet.BasicServlet;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.AddAppAction;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.action.jarcontent.SaveFileContentAction;
import com.qlangtech.tis.runtime.pojo.ConfigPush;
import com.qlangtech.tis.runtime.pojo.ResSynManager;
import com.qlangtech.tis.trigger.biz.dal.dao.ITerminatorTriggerBizDalDAOFacade;

import junit.framework.Assert;

/*
 * 负责接收日常向线上发送的应用同步请求
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AppSynAction extends BasicModule {

    private static final long serialVersionUID = 1L;

    private ITerminatorTriggerBizDalDAOFacade triggerContext;

    /**
     * 接收从日常环境中推送上来的配置文件，<br>
     * 当第一次推送的时候线上还不存在索引实例的时候，会自动创建索引实例
     *
     * @param context
     * @throws Exception
     */
    @Func(PermissionConstant.APP_ADD)
    public void doInitAppFromDaily(Context context) throws Exception {
        if (ManageUtils.isDevelopMode()) {
            throw new RuntimeException("this method shall execute online");
        }
        RunEnvironment runtime = RunEnvironment.getSysRuntime();
        String content = null;
        try (InputStream reader = DefaultFilter.getReqeust().getInputStream()) {
            content = IOUtils.toString(reader, getEncode());
            if (StringUtils.isEmpty(content)) {
                throw new IllegalArgumentException("upload content can not be null");
            }
        }
        final ConfigPush configPush = (ConfigPush) BasicServlet.xstream.fromXML(content);
        final String collection = configPush.getCollection();
        // 校验当前的snapshot 版本是否就是传输上来的snapshot版本
        ServerGroup serverGroup = null;
        if (configPush.getRemoteSnapshotId() != null) {
            serverGroup = this.getServerGroupDAO().load(collection, (short) 0, /* groupIndex */
            runtime.getId());
            if (serverGroup.getPublishSnapshotId() != (configPush.getRemoteSnapshotId() + 0)) {
                this.addErrorMessage(context, "exist snapshotid:" + serverGroup.getPublishSnapshotId() + " is not equal push snapshotid:" + configPush.getRemoteSnapshotId());
                return;
            }
        }
        // List<UploadResource> resources = configPush.getUploadResources();
        Snapshot snapshot = null;
        SnapshotDomain snapshotDomain = null;
        Application app = null;
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.createCriteria().andProjectNameEqualTo(collection);
        List<Application> apps = this.getApplicationDAO().selectByExample(criteria);
        for (Application p : apps) {
            app = p;
            break;
        }
        if (app == null) {
            // 在服务端创建新应用
            app = new Application();
            Integer newAppid = this.createNewApp(context, configPush);
            app.setAppId(newAppid);
        }
        String snycDesc = "NEW CREATE";
        serverGroup = this.getServerGroupDAO().load(collection, (short) 0, /* groupIndex */
        runtime.getId());
        boolean newSnapshot = false;
        if (serverGroup == null || serverGroup.getPublishSnapshotId() == null) {
            snapshot = new Snapshot();
            snapshot.setSnId(-1);
            snapshot.setPreSnId(-1);
            snapshot.setAppId(app.getAppId());
            newSnapshot = true;
        } else {
            snycDesc = "PUSH FROM DAILY";
            snapshotDomain = this.getSnapshotViewDAO().getView(configPush.getRemoteSnapshotId());
            snapshot = snapshotDomain.getSnapshot();
        }
        if (snapshot == null) {
            throw new IllegalStateException("snapshot can not be null,collection:" + collection);
        }
        snapshot.setCreateUserId(0l);
        snapshot.setCreateUserName(configPush.getReception());
        // ///////////////////////////////////
        // 组装新的snapshot
        PropteryGetter pGetter = null;
        for (UploadResource res : configPush.getUploadResources()) {
            pGetter = ConfigFileReader.createPropertyGetter(res.getResourceType());
            // 校验配置是否相等
            if (!newSnapshot) {
                final String md5 = ConfigFileReader.md5file(res.getContent());
                if (StringUtils.equals(md5, pGetter.getMd5CodeValue(snapshotDomain))) {
                    this.addErrorMessage(context, "resource " + pGetter.getFileName() + " is newest,shall not be updated");
                    return;
                }
            }
            Integer newResId = ResSynManager.createNewResource(context, res.getContent(), ConfigFileReader.md5file(res.getContent()), pGetter, this, this);
            snapshot = pGetter.createNewSnapshot(newResId, snapshot);
        }
        serverGroup = new ServerGroup();
        serverGroup.setPublishSnapshotId(SaveFileContentAction.createNewSnapshot(snapshot, snycDesc, this, 0l, configPush.getReception()));
        serverGroup.setUpdateTime(new Date());
        ServerGroupCriteria serverGroupCriteria = new ServerGroupCriteria();
        serverGroupCriteria.createCriteria().andAppIdEqualTo(app.getAppId()).andRuntEnvironmentEqualTo(runtime.getId()).andGroupIndexEqualTo((short) 0);
        this.getServerGroupDAO().updateByExampleSelective(serverGroup, serverGroupCriteria);
        // /////////////////////////////////////
        this.addActionMessage(context, "synsuccess");
    }

    protected Integer createNewApp(Context context, final ConfigPush configPush) {
        Department department = configPush.getDepartment();
        Assert.assertNotNull("department can not be null", department);
        final String dptFullName = department.getFullName();
        Department dpt = getDpt(dptFullName);
        if (dpt == null) {
            // 该部门还没有被创建
            insertDepartment(dptFullName, getDepartmentDAO(), 0, 0);
            dpt = getDpt(dptFullName);
            Assert.assertNotNull("dpt can not be null", dpt);
        }
        Application app = new Application();
        app.setAppId(null);
        app.setDptId(dpt.getDptId());
        app.setDptName(dpt.getFullName());
        app.setCreateTime(new Date());
        app.setUpdateTime(new Date());
        //app.setIsAutoDeploy(true);
        app.setProjectName(configPush.getCollection());
        app.setRecept(configPush.getReception());
//        app.setNobleAppId(0);
//        app.setNobleAppName("default");
        // 创建应用
        Integer newid = AddAppAction.createApplication(app, context, this, triggerContext);
        return newid;
    }

    public static void insertDepartment(String departmentName, final IDepartmentDAO dptDAO, int iterateCount, // User
    Integer aliGroupDptId) // user
    {
        String[] dptary = StringUtils.split(departmentName, "-");
        Department department = null;
        Integer parentDptId = null;
        StringBuffer parentPath = new StringBuffer();
        for (int i = 0; i < dptary.length; i++) {
            parentPath.append(dptary[i]);
            if (!hasNode(dptDAO, parentPath.toString())) {
                // 新建一个节点
                // 插入新的部门
                department = new Department();
                department.setParentId((parentDptId == null) ? -1 : parentDptId);
                department.setFullName(parentPath.toString());
                department.setName(dptary[i]);
                if ((i + 1) == dptary.length) {
                   
                }
                department.setGmtCreate(new Date());
                department.setGmtModified(new Date());
                department.setLeaf((i + 1) == dptary.length);
                // System.out.println("inser :" + departmentName
                // + " iterateCount:" + iterateCount);
                parentDptId = dptDAO.insertSelective(department);
            } else {
                parentDptId = getParentId(dptDAO, parentPath.toString());
                if ((i + 1) == dptary.length) {
                    department = new Department();
                   
                    DepartmentCriteria q = new DepartmentCriteria();
                    q.createCriteria().andDptIdEqualTo(parentDptId);
                    dptDAO.updateByExampleSelective(department, q);
                    return;
                }
            }
            if (i + 1 < dptary.length) {
                parentPath.append("-");
            }
        }
    }

    private static Integer getParentId(final IDepartmentDAO dptDAO, String parentName) {
        if (StringUtils.isNotEmpty(parentName)) {
            DepartmentCriteria dptCriteria = new DepartmentCriteria();
            dptCriteria.createCriteria().andFullNameEqualTo(parentName);
            for (Department dpt : dptDAO.selectByExample(dptCriteria)) {
                return dpt.getDptId();
            }
        }
        return null;
    }

    private static boolean hasNode(final IDepartmentDAO dptDAO, String name) {
        DepartmentCriteria dptCriteria = new DepartmentCriteria();
        dptCriteria.createCriteria().andFullNameEqualTo(name);
        return StringUtils.isNotEmpty(name) && dptDAO.countByExample(dptCriteria) > 0;
    }

    @Autowired
    public void setTisTriggerBizDalDaoFacade(ITerminatorTriggerBizDalDAOFacade triggerDaoContext) {
        this.triggerContext = triggerDaoContext;
    }

    /**
     * 查询部门信息
     *
     * @param dptFullName
     * @return
     */
    private Department getDpt(String dptFullName) {
        DepartmentCriteria dptCriteria = new DepartmentCriteria();
        dptCriteria.createCriteria().andFullNameEqualTo(dptFullName);
        for (Department dpt : this.getDepartmentDAO().selectByExample(dptCriteria)) {
            return dpt;
        }
        return null;
    }
}
