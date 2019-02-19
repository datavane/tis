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
package com.qlangtech.tis.runtime.module.action.jarcontent;

import java.util.Date;
import junit.framework.Assert;
import com.alibaba.citrus.turbine.Context;
import com.opensymphony.xwork2.ModelDriven;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.form.SingleJarForm;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class SingleJarAction extends BasicModule implements ModelDriven<SingleJarForm> {

    private static final long serialVersionUID = 4619294097920803660L;

    // private ParserRequestContext parserRequestContext;
    private SingleJarForm jarForm = new SingleJarForm();

    @Override
    public SingleJarForm getModel() {
        return jarForm;
    }

    /**
     * 上传单个jar包
     *
     * @param form
     */
//    @Func(PermissionConstant.CONFIG_UPLOAD)
//    @SuppressWarnings("all")
//    public void doUploadSingleJar(Context context) throws Exception {
//        if (jarForm.getUploadfile() == null) {
//            this.addErrorMessage(context, "请上传Jar文件");
//            return;
//        }
//        Integer snapshotId = this.getInt("snapshot");
//        Assert.assertNotNull(snapshotId);
//        final Snapshot snapshot = this.getSnapshotDAO().loadFromWriteDB(snapshotId);
//        Assert.assertNotNull(snapshot);
//        // for (FileItem item : items) {
//        UploadResource resource = new UploadResource();
//        resource.setContent(jarForm.getContent());
//        resource.setCreateTime(new Date());
//        resource.setResourceType(ConfigFileReader.FILE_JAR.getFileName());
//        resource.setMd5Code(ConfigFileReader.md5file(resource.getContent()));
//        Integer resid = this.getUploadResourceDAO().insert(resource);
//        Snapshot createSnapshot = ConfigFileReader.FILE_JAR.createNewSnapshot(resid, snapshot);
//        createSnapshot.setSnId(null);
//        createSnapshot.setUpdateTime(new Date());
//        createSnapshot.setCreateTime(new Date());
//        try {
//            createSnapshot.setCreateUserId(Long.parseLong(this.getUserId()));
//        } catch (Throwable e) {
//        }
//        createSnapshot.setCreateUserName(this.getLoginUserName());
//        Integer id = this.getSnapshotDAO().insertSelective(createSnapshot);
//        this.addActionMessage(context, "已经成功创建了一条新Snapshot：" + id);
//    }
}
