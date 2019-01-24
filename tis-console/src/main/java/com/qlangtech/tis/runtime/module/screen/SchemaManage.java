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
package com.qlangtech.tis.runtime.module.screen;

import org.apache.commons.logging.LogFactory;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.common.AppDomainInfo;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SchemaManage extends BasicScreen {

    private static final long serialVersionUID = 1L;

    private static org.apache.commons.logging.Log log = LogFactory.getLog(SchemaManage.class);

    @Override
    public void execute(Context context) throws Exception {
        AppDomainInfo app = this.getAppDomain();
    // NewAppInfo appinfo = this.getAppinfoFromTair();
    // 
    // 
    // ApplicationExtend appExtend = this.getApplicationExtendDAO()
    // .selectByAppId(appinfo.getAppId());
    // //context.put("indexname", appinfo.getAppName());
    // context.put("aid", appinfo.getAppId());
    // context.put("status", this.isCoreExisted(appinfo.getAppName()));
    // context.put("projectname", appinfo.getAppName());
    // 
    // context.put("datatype", appExtend.getSourceType());
    // TriggerLockInfo lockInfo = this.getCache().getObj(
    // ConstantUtil.LOCKKEY_PREFIX + appinfo.getAppId());
    // appinfo.setAppId(appinfo.getAppId());
    // appinfo.setAppName(appinfo.getAppName());
    // 
    // if (lockInfo != null) {
    // context.put("remaintime",
    // ConstantUtil.ENSPIRETIME_LOCK + lockInfo.getDatetime()
    // - (System.currentTimeMillis() / 1000));
    // } else {
    // context.put("remaintime", 0);
    // }
    }
}
