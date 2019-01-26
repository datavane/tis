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
package com.qlangtech.tis.runtime.module.action;

import java.util.Date;
import java.util.regex.Pattern;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParameters;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParametersCriteria;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ConfigFileParametersAction extends BasicModule {

    private static final long serialVersionUID = 1L;

    private IResourceParametersDAO resourceParametersDAO;

    private static final Pattern PATTERN_KEY_NAME = Pattern.compile("[\\w|_]+");

    /**
     * 设置参数值
     *
     * @param context
     */
    @Func(PermissionConstant.GLOBAL_PARAMETER_SET)
    public void doSetParameter(Context context) {
        RunEnvironment runtime = RunEnvironment.getEnum(this.getString("runtime"));
        Long rpid = this.getLong("rpid");
        Assert.assertNotNull(rpid);
        Assert.assertNotNull(runtime);
        ResourceParameters param = new ResourceParameters();
        ResourceParametersCriteria criteria = new ResourceParametersCriteria();
        criteria.createCriteria().andRpIdEqualTo(rpid);
        Assert.assertNotNull(runtime);
        String keyvalue = this.getString("keyvalue");
        if (StringUtils.isBlank(keyvalue)) {
            this.addErrorMessage(context, "键值不能为空");
            return;
        }
        switch(runtime) {
            case DAILY:
                param.setDailyValue(keyvalue);
                break;
            case ONLINE:
                param.setOnlineValue(keyvalue);
                break;
            default:
                throw new IllegalArgumentException("runtime:" + runtime + " is invalid");
        }
        this.resourceParametersDAO.updateByExampleSelective(param, criteria);
        this.addActionMessage(context, "已经成功更新");
    }

    @Func(PermissionConstant.GLOBAL_PARAMETER_ADD)
    public void doAddParameter(Context context) {
        String keyName = this.getString("keyname");
        if (StringUtils.isBlank(keyName)) {
            this.addErrorMessage(context, "键名称不能为空");
            return;
        }
        if (!PATTERN_KEY_NAME.matcher(keyName).matches()) {
            this.addErrorMessage(context, "键名键值必须由字母和数字和下划线组成");
            return;
        }
        ResourceParametersCriteria criteria = new ResourceParametersCriteria();
        criteria.createCriteria().andKeyNameEqualTo(keyName);
        if (resourceParametersDAO.countByExample(criteria) > 0) {
            this.addErrorMessage(context, "该键值名" + keyName + "系统中已经存在");
            return;
        }
        ResourceParameters param = new ResourceParameters();
        param.setGmtCreate(new Date());
        param.setDailyValue(StringUtils.trimToNull(this.getString("dailyvalue")));
        param.setReadyValue(StringUtils.trimToNull(this.getString("readyvalue")));
        param.setOnlineValue(StringUtils.trimToNull(this.getString("onlinevalue")));
        param.setKeyName(keyName);
        resourceParametersDAO.insertSelective(param);
        this.addActionMessage(context, "成功添加配置全局变量：" + keyName);
    }

    public IResourceParametersDAO getResourceParametersDAO() {
        return resourceParametersDAO;
    }

    @Autowired
    public void setResourceParametersDAO(IResourceParametersDAO resourceParametersDAO) {
        this.resourceParametersDAO = resourceParametersDAO;
    }
}
