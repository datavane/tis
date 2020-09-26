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
package com.qlangtech.tis.runtime.module.action;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParameters;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParametersCriteria;
import com.qlangtech.tis.manage.servlet.GlobalConfigServlet;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-9-21
 */
public class ConfigFileParametersAction extends BasicModule {

    private static final long serialVersionUID = 1L;

    private IResourceParametersDAO resourceParametersDAO;

    private static final Pattern PATTERN_KEY_NAME = Pattern.compile("[\\w|_]+");

    public void doGetParam(Context context) {
        Long rpid = this.getLong("rpid");
        Assert.assertNotNull(rpid);
        ResourceParameters param = resourceParametersDAO.selectByPrimaryKey(rpid);
        Assert.assertNotNull(param);
        final RunEnvironment runtime = RunEnvironment.getSysRuntime();
        param.setValue(GlobalConfigServlet.getParameterValue(param, runtime));
        this.setBizResult(context, param);
    }

    /**
     * 设置参数值
     *
     * @param context
     */
    @Func(PermissionConstant.GLOBAL_PARAMETER_SET)
    public void doSetParameter(Context context) {
        Long rpid = this.getLong("rpid");
        Assert.assertNotNull(rpid);
        // Assert.assertNotNull(runtime);
        ResourceParameters param = new ResourceParameters();
        ResourceParametersCriteria criteria = new ResourceParametersCriteria();
        criteria.createCriteria().andRpIdEqualTo(rpid);
        // Assert.assertNotNull(runtime);
        String keyvalue = this.getString("keyvalue");
        if (StringUtils.isBlank(keyvalue)) {
            this.addErrorMessage(context, "键值不能为空");
            return;
        }
        if (RunEnvironment.isOnlineMode()) {
            param.setOnlineValue(keyvalue);
        } else {
            param.setDailyValue(keyvalue);
        }
        // switch (runtime) {
        // case DAILY:
        // param.setDailyValue(keyvalue);
        // break;
        // case READY:
        // // paramValue = param.getReadyValue();
        // param.setReadyValue(keyvalue);
        // break;
        // case ONLINE:
        // param.setOnlineValue(keyvalue);
        // break;
        // default:
        // throw new IllegalArgumentException("runtime:" + runtime + " is
        // invalid");
        // }
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
        String value = StringUtils.trimToNull(this.getString("value"));
        if (StringUtils.isEmpty(value)) {
            this.addErrorMessage(context, "该键" + keyName + "对应键值不能为空");
            return;
        }
        if (RunEnvironment.isOnlineMode()) {
            param.setOnlineValue(value);
        } else {
            param.setDailyValue(value);
        }
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
