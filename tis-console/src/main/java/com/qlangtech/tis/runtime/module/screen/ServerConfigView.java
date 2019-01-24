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

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.manage.servlet.QueryResutStrategy;
import com.qlangtech.tis.manage.servlet.SolrCloudQueryResutStrategy;
import com.qlangtech.tis.manage.servlet.SolrQueryModuleCreatorAdapter;
import com.qlangtech.tis.runtime.module.screen.IndexQuery.QueryRequestContext;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;

/*
 * 服务器中配置文件查看
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class ServerConfigView extends BasicScreen {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(Context context) throws Exception {
        final QueryResutStrategy queryResutStrategy = SolrCloudQueryResutStrategy.create(this.getAppDomain(), new SolrQueryModuleCreatorAllAdapter() {

            @Override
            public void selectedCanidateServers(Collection<String> selectedCanidateServers) {
                getRequest().setAttribute("selectedCanidateServers", selectedCanidateServers);
            }

            @Override
            public void setQuerySelectServerCandiate(Map<Short, List<ServerJoinGroup>> servers) {
                getRequest().setAttribute("querySelectServerCandiate", servers);
            }
        }, this);
        queryResutStrategy.queryProcess();
    }

    public static class SolrQueryModuleCreatorAllAdapter extends SolrQueryModuleCreatorAdapter {

        @Override
        public SolrParams build(String querystr, String sort, String fq, Integer shownumf, List<String> showField) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ParseResult processSchema(InputStream schemaStream) {
            return null;
        }

        @Override
        public String[] getParameterValues(String keyname) {
            // return getRequest().getParameterValues(keyname);
            return null;
        }

        @Override
        public void handleError(String url, ServerJoinGroup server, long allcount, Exception e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void processResult(QueryRequestContext qrequest, QueryResponse result, ServerJoinGroup server) throws Exception {
        }

        @Override
        public void selectedCanidateServers(Collection<String> selectedCanidateServers) {
        }

        @Override
        public void setQuerySelectServerCandiate(Map<Short, List<ServerJoinGroup>> servers) {
        }
    }
}
