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

import junit.framework.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParameters;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ConfigFileParametersSet extends BasicManageScreen {

    private static final long serialVersionUID = 1L;

    private IResourceParametersDAO resourceParametersDAO;

    @Override
    public void execute(Context context) throws Exception {
        this.disableNavigationBar(context);
        Long rpid = this.getLong("rpid");
        Assert.assertNotNull(rpid);
        ResourceParameters param = resourceParametersDAO.selectByPrimaryKey(rpid);
        Assert.assertNotNull(param);
        context.put("param", param);
        RunEnvironment runtime = RunEnvironment.getEnum(this.getString("runtime"));
        context.put("runtime", runtime);
        String paramValue = getParameterValue(param, runtime);
        context.put("paramValue", paramValue);
    }

    public static String getParameterValue(ResourceParameters param, RunEnvironment runtime) {
        Assert.assertNotNull(param);
        Assert.assertNotNull(runtime);
        String paramValue = null;
        switch(runtime) {
            case DAILY:
                paramValue = param.getDailyValue();
                break;
            // break;
            case KR:
            case THA:
            case ONLINE2:
            case ONLINE:
                paramValue = param.getOnlineValue();
                break;
            default:
                throw new IllegalArgumentException("runtime:" + runtime + " is invalid");
        }
        return paramValue;
    }

    public IResourceParametersDAO getResourceParametersDAO() {
        return resourceParametersDAO;
    }

    @Autowired
    public void setResourceParametersDAO(IResourceParametersDAO resourceParametersDAO) {
        this.resourceParametersDAO = resourceParametersDAO;
    }
}
