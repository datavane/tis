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

import java.util.Arrays;
import junit.framework.Assert;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.OperationLogCriteria;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class OperationLogSpecialApp extends OperationLog {

    /**
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void execute(Context context) throws Exception {
        super.execute(context);
        this.disableNavigationBar(context);
    }

    protected int getPageSize() {
        return 20;
    }

    protected OperationLogCriteria createOperationLogCriteria() {
        // AppDomainInfo domain = this.getAppDomain();
        final String appName = this.getString("appname");
        RunEnvironment runtime = RunEnvironment.getEnum(this.getString("runtime"));
        OperationLogCriteria lcriteria = new OperationLogCriteria();
        Assert.assertNotNull(appName);
        Assert.assertNotNull(this.getString("tab"));
        Assert.assertNotNull(this.getString("opt"));
        OperationLogCriteria.Criteria criteria = lcriteria.createCriteria().andAppNameEqualTo(appName).andRuntimeEqualTo(runtime.getId()).andTabNameEqualTo(this.getString("tab"));
        String[] opts = this.getRequest().getParameterValues("opt");
        if (opts.length < 2) {
            criteria.andOpTypeEqualTo(this.getString("opt"));
        } else {
            criteria.andOpTypeIn(Arrays.asList(opts));
        }
        return lcriteria;
    }

    protected StringBuffer getPagerUrl() {
        StringBuffer result = new StringBuffer("?tab=");
        result.append(this.getString("tab"));
        result.append("&opt=").append(this.getString("opt"));
        return result;
    }
}
