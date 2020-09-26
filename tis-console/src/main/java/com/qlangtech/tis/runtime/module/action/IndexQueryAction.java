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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.servlet.DownloadResource;
import com.qlangtech.tis.manage.servlet.DownloadServlet;
import com.qlangtech.tis.manage.servlet.QueryIndexServlet;
import com.qlangtech.tis.manage.servlet.QueryResutStrategy;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.pubhook.common.Nullable;
import com.qlangtech.tis.runtime.module.screen.IndexQuery;
import com.qlangtech.tis.runtime.module.screen.IndexQuery.QueryRequestContext;
import com.qlangtech.tis.runtime.module.screen.IndexQuery.QueryRequestWrapper;
import com.qlangtech.tis.runtime.module.screen.ViewPojo;
import com.qlangtech.tis.runtime.module.screen.ViewPojo.ResourcePrep;
import com.qlangtech.tis.solrdao.IBuilderContext;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;
import org.apache.commons.lang.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询索引
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-12-14
 */
public class IndexQueryAction extends BasicModule {

    /**
     */
    private static final long serialVersionUID = 1L;

    @Func(value = PermissionConstant.PERMISSION_INDEX_QUERY, sideEffect = false)
    public void doQuery(Context context) throws Exception {
        List<String> sfields = Lists.newArrayList(this.getRequest().getParameterValues("sfields"));
        final String query = StringUtils.defaultIfEmpty((this.getString("q")).replaceAll("\r|\n", StringUtils.EMPTY), "*:*");
        Integer shownum = null;
        shownum = this.getInt("shownum", 3);
        QueryRequestWrapper request = new QueryRequestWrapper(this.getRequest(), context);
        QueryRequestContext requestContext = new QueryRequestContext(request);
        final String sort = getString("sort");
        final String[] fqs = this.getStringArray("fq");
        final QueryResutStrategy queryResutStrategy = QueryIndexServlet.createQueryResutStrategy(this.getAppDomain(), request, getResponse(), getDaoContext());
        final List<ServerJoinGroup> serverlist = queryResutStrategy.queryProcess();
        QueryIndexServlet.execuetQuery(this, this.getAppDomain(), requestContext, this.getDaoContext(), queryResutStrategy, serverlist, query, sort, fqs, shownum, sfields);
    }

    /**
     * @param context
     */
    public void doGetServerNodes(Context context) throws Exception {
        AppDomainInfo domain = this.getAppDomain();
        if (domain instanceof Nullable) {
            throw new IllegalStateException("execute phase must be Collection aware");
        }
        QueryResutStrategy queryStrategy = QueryIndexServlet.createQueryResutStrategy(domain, new QueryRequestWrapper(getRequest(), context), getResponse(), getDaoContext());
        List<ServerJoinGroup> nodes = queryStrategy.queryProcess();
        List<PSchemaField> sfields = IndexQuery.getSfields(this.getRequest(), queryStrategy, nodes);
        Map<String, Object> props = Maps.newHashMap();
        props.put("nodes", queryStrategy.selectCandiate);
        props.put("fields", sfields.stream().map((c) -> c.getName()).collect(Collectors.toList()));
        this.setBizResult(context, props);
    }

    /**
     * 下载POJO
     */
    public void doDownloadPojo(Context context) throws Exception {
        ResourcePrep resourcePrep = new ResourcePrep() {

            @Override
            public void prepare(IBuilderContext builderContext) {
                getResponse().setContentType(DownloadResource.JAR_CONTENT_TYPE);
                DownloadServlet.setDownloadName(getResponse(), builderContext.getPojoName() + ".java");
            }
        };
        if (!(ViewPojo.downloadResource(context, this.getAppDomain(), this, getResponse().getWriter(), resourcePrep))) {
            return;
        }
    }
}
