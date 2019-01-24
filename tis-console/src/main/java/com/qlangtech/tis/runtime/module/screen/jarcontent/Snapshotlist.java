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
package com.qlangtech.tis.runtime.module.screen.jarcontent;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.koubei.web.tag.pager.Pager;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.SnapshotCriteria;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.pubhook.common.Nullable;
import com.qlangtech.tis.runtime.module.screen.BasicScreen;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class Snapshotlist extends BasicScreen {

    /**
     */
    private static final long serialVersionUID = 1L;

    @Override
    @Func(PermissionConstant.CONFIG_SNAPSHOT_LIST)
    public void execute(Context context) throws Exception {
        this.enableChangeDomain(context);
        AppDomainInfo domain = this.getAppDomain();
        if (domain instanceof Nullable) {
            // 是否已经选择了域
            return;
        }
        context.put("hasselectdomain", true);
        SnapshotCriteria query = new SnapshotCriteria();
        // snapshot 一览先不考虑有 版本设置
        query.createCriteria().andAppidEqualTo(domain.getAppid());
        // .andIndexHasConfirm();
        // .andRunEnvironmentEqualTo(domain.getRunId());
        query.setOrderByClause("snapshot.sn_id desc");
        getPager().setTotalCount(this.getSnapshotDAO().countByExample(query));
        context.put("snapshotlist", this.getSnapshotDAO().selectByExample(query, getPager().getCurPage(), getPager().getRowsPerPage()));
    // context.put("pager", pager);
    }

    private Pager pager;

    @SuppressWarnings("all")
    @Override
    protected StringBuffer getPagerUrl() {
        StringBuffer result = new StringBuffer(this.getRequest().getRequestURL());
        Map params = this.getRequest().getParameterMap();
        if (!params.isEmpty()) {
            result.append("?");
        }
        String[] value = null;
        boolean first = true;
        for (Object key : params.keySet()) {
            if (!StringUtils.equalsIgnoreCase(String.valueOf(key), "page")) {
                if (!first) {
                    result.append("&");
                }
                if (params.get(key) instanceof String[]) {
                    value = (String[]) params.get(key);
                } else {
                    value = new String[] { String.valueOf(params.get(key)) };
                }
                result.append(key).append("=").append(value[0]);
                first = false;
            }
        }
        // }
        return result;
    }

    public Pager getPager() {
        if (pager == null) {
            pager = this.createPager();
        }
        return pager;
    }
}
