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
import com.koubei.web.tag.pager.Pager;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.dao.IOperationLogDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.OperationLogCriteria;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月8日
 */
public class OperationLogAction extends BasicModule {

    private static final long serialVersionUID = 1L;

    private IOperationLogDAO operationLogDAO;

    /**
     * log 显示页面显示日志信息
     *
     * @param context
     */
    public void doGetInitData(Context context) {
        OperationLogCriteria query = createOperationLogCriteria();
        Pager pager = this.createPager();
        query.setOrderByClause("op_id desc");
        pager.setTotalCount(this.operationLogDAO.countByExample(query));
        this.setBizResult(context, new PaginationResult(pager, this.operationLogDAO.selectByExampleWithoutBLOBs(query, pager.getCurPage(), pager.getRowsPerPage())));
    }

    protected OperationLogCriteria createOperationLogCriteria() {
        final String appName = this.getAppDomain().getAppName();
        OperationLogCriteria lcriteria = new OperationLogCriteria();
        if (StringUtils.isBlank(appName)) {
            return lcriteria;
        }
        // RunEnvironment runtime =  RunEnvironment.getSysEnvironment();
        final RunEnvironment runtime = RunEnvironment.getSysRuntime();
        Assert.assertNotNull(appName);
        // Assert.assertNotNull(this.getString("tab"));
        // Assert.assertNotNull(this.getString("opt"));
        OperationLogCriteria.Criteria criteria = lcriteria.createCriteria().andAppNameEqualTo(appName);
        // }
        return lcriteria;
    }

    protected int getPageSize() {
        return PAGE_SIZE;
    }

    @Func(PermissionConstant.APP_BUILD_RESULT_VIEW)
    public void doGetDetail(Context context) throws Exception {
        this.disableNavigationBar(context);
        Integer opid = this.getInt("opid");
        Assert.assertNotNull(opid);
        // this.addActionMessage(context, operationLogDAO.loadFromWriteDB(opid).getOpDesc());
        this.setBizResult(context, operationLogDAO.loadFromWriteDB(opid));
    }

    @Autowired
    public void setOperationLogDAO(IOperationLogDAO operationLogDAO) {
        this.operationLogDAO = operationLogDAO;
    }
}
