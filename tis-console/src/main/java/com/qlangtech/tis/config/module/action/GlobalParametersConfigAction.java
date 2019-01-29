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

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.common.utils.KeyPair;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParameters;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParametersCriteria;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/*
 * 取得/设置全局配置参数
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class GlobalParametersConfigAction extends BasicModule {

    private static final long serialVersionUID = 1L;

    // http://127.0.0.1/config/config.ajax?action=global_parameters_config_action&event_submit_do_get_all=y&runtime=daily&resulthandler=advance_query_result
    public void doGetAll(Context context) throws Exception {
        RunEnvironment runtime = RunEnvironment.getEnum(this.getRequest().getParameter("runtime"));
        ResourceParametersCriteria criteria = new ResourceParametersCriteria();
        criteria.setOrderByClause("rp_id desc");
        List<ResourceParameters> paramsList = resourceParametersDAO.selectByExample(criteria, 1, 100);
        List<KeyPair> result = new ArrayList<KeyPair>();
        switch(runtime) {
            case DAILY:
                for (ResourceParameters p : paramsList) {
                    result.add(new KeyPair(p.getKeyName(), p.getDailyValue()));
                }
                break;
           
            case ONLINE:
     
                for (ResourceParameters p : paramsList) {
                    result.add(new KeyPair(p.getKeyName(), p.getOnlineValue()));
                }
                break;
            default:
                throw new IllegalStateException("illegal argument:" + runtime);
        }
        this.setBizObjResult(context, result);
    }

    private IResourceParametersDAO resourceParametersDAO;

    public IResourceParametersDAO getResourceParametersDAO() {
        return resourceParametersDAO;
    }

    @Autowired
    public void setResourceParametersDAO(IResourceParametersDAO resourceParametersDAO) {
        this.resourceParametersDAO = resourceParametersDAO;
    }
}
