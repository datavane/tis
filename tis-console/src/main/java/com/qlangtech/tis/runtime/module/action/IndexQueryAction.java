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

import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.manage.servlet.QueryIndexServlet;
import com.qlangtech.tis.manage.servlet.QueryResutStrategy;
import com.qlangtech.tis.runtime.module.screen.IndexQuery.QueryRequestContext;
import com.qlangtech.tis.runtime.module.screen.IndexQuery.QueryRequestWrapper;

/*
 * 查询索引
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexQueryAction extends BasicModule {

    /**
     */
    private static final long serialVersionUID = 1L;

    // private static final long serialVersionUID = 1L;
    // private static final Log log = LogFactory.getLog(IndexQueryAction.class);
    // 
    // /**
    // * 在主查询页面上进行查询
    // *
    // * @param context
    // * @throws Exception
    // */
    // public void doMainQuery(Context context) throws Exception {
    // 
    // }
    // 
    // //
    // http://l.admin.taobao.org/runtime/index_query.ajax?action=index_query_action&event_submit_do_query=y&resulthandler=advance_query_result&execsql=select+*+from+search4sucai%3B
    public void doQuery(Context context) throws Exception {
        List<String> sfields = Lists.newArrayList(this.getRequest().getParameterValues("sfields"));
        final String query = StringUtils.defaultIfBlank((this.getString("q")).replaceAll("\r|\n", StringUtils.EMPTY), "*:*");
        Integer shownum = null;
        // Integer.parseInt(request.getParameter("shownum"));
        shownum = this.getInt("shownum");
        if (shownum == null) {
            shownum = 3;
        }
        QueryRequestWrapper request = new QueryRequestWrapper(this.getRequest(), context);
        QueryRequestContext requestContext = new QueryRequestContext(request);
        // 是否需要打印scoreexplain信息
        // requestContext.queryDebug =
        // "true".equalsIgnoreCase(this.getString("debugQuery"));
        final String sort = getString("sort");
        final String fq = getString("fq");
        final QueryResutStrategy queryResutStrategy = QueryIndexServlet.createQueryResutStrategy(this.getAppDomain(), request, getResponse(), getDaoContext());
        final List<ServerJoinGroup> serverlist = queryResutStrategy.queryProcess();
        // this.setSfields(request, domain, requestContext, queryResutStrategy,
        // serverlist);
        // try {
        // this.forward(request, resp);
        // } catch (Exception e) {
        // log.error(e.getMessage(), e);
        // throw new ServletException(e);
        // }
        // if (true || queryResutStrategy.isResultAware()) {
        // final String querystr, final String sort, final String fq, final Integer
        // shownumf,
        // final List<String> showFields
        QueryIndexServlet.execuetQuery(this.getAppDomain(), requestContext, this.getDaoContext(), queryResutStrategy, serverlist, query, sort, fq, shownum, sfields);
    // }
    }
}
