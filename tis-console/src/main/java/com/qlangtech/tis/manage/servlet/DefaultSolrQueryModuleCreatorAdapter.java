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
package com.qlangtech.tis.manage.servlet;

import java.io.InputStream;
import java.util.List;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.runtime.module.screen.IndexQuery.QueryRequestContext;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DefaultSolrQueryModuleCreatorAdapter extends SolrQueryModuleCreatorAdapter {

    // @Override
    // public void processResult(QueryRequest qrequest, QueryResponse result,
    // ServerJoinGroup server) throws Exception {
    // }
    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taobao.terminator.manage.servlet.QueryIndexServlet.SolrQueryModuleCreator
	 * #build()
	 */
    // @Override
    // public SolrParams build() {
    // throw new UnsupportedOperationException();
    // }
    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taobao.terminator.manage.servlet.QueryIndexServlet.SolrQueryModuleCreator
	 * #handleError(java.lang.String,
	 * com.taobao.terminator.manage.biz.dal.pojo.ServerJoinGroup, long,
	 * java.lang.Exception)
	 */
    @Override
    public void handleError(String url, ServerJoinGroup server, long allcount, Exception e) {
    }

    @Override
    public SolrParams build(String querystr, String sort, String fq, Integer shownumf, List<String> showField) {
        return null;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taobao.terminator.manage.servlet.QueryIndexServlet.SolrQueryModuleCreator
	 * #processResult(org.apache.solr.client.solrj.response.QueryResponse,
	 * com.taobao.terminator.manage.biz.dal.pojo.ServerJoinGroup, long)
	 */
    @Override
    public void processResult(QueryRequestContext request, QueryResponse result, ServerJoinGroup server) throws Exception {
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taobao.terminator.manage.servlet.QueryIndexServlet.SolrQueryModuleCreator
	 * #processSchema(java.io.InputStream)
	 */
    @Override
    public ParseResult processSchema(InputStream schemaStream) {
        return null;
    }
}
