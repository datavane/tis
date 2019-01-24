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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.citrus.turbine.Context;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.servlet.QueryIndexServlet;
import com.qlangtech.tis.manage.servlet.QueryIndexServlet.SolrQueryModuleCreator;
import com.qlangtech.tis.manage.servlet.QueryResutStrategy;
import com.qlangtech.tis.pubhook.common.Nullable;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.SolrFieldsParser.SchemaFields;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;

/*
 * 索引查询
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexQuery extends BasicScreen {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(IndexQuery.class);

    private static final Cache<String, SchemaFields> /* collection name */
    schemaFieldsCache;

    static {
        schemaFieldsCache = CacheBuilder.newBuilder().expireAfterWrite(6, TimeUnit.MINUTES).build();
    }

    public IndexQuery() {
        super();
    }

    private List<PSchemaField> getSfields(QueryResutStrategy queryStrategy, List<ServerJoinGroup> nodes) throws Exception {
        // return getRequest().getParameterValues("sfields");
        final String collection = this.getAppDomain().getAppName();
        List<PSchemaField> fieldList = null;
        fieldList = schemaFieldsCache.getIfPresent(collection);
        if (fieldList == null) {
            fieldList = schemaFieldsCache.get(collection, new Callable<SchemaFields>() {

                @Override
                public SchemaFields call() throws Exception {
                    QueryRequestContext queryContext = new QueryRequestContext(getRequest());
                    getSchemaFrom1Server(collection, queryContext, queryStrategy, nodes);
                    return queryContext.schema.dFields;
                }
            });
        }
        return fieldList;
    }

    @Override
    public void execute(Context context) throws Exception {
        this.enableChangeDomain(context);
        AppDomainInfo domain = this.getAppDomain();
        if (domain instanceof Nullable) {
            return;
        }
        QueryResutStrategy queryStrategy = QueryIndexServlet.createQueryResutStrategy(domain, new QueryRequestWrapper(getRequest(), context), getResponse(), getDaoContext());
        List<ServerJoinGroup> nodes = queryStrategy.queryProcess();
        context.put("sfields", this.getSfields(queryStrategy, nodes));
    }

    public static class QueryRequestWrapper extends HttpServletRequestWrapper {

        private final Context context;

        public QueryRequestWrapper(HttpServletRequest request, Context context) {
            super(request);
            this.context = context;
        }

        @Override
        public void setAttribute(String name, Object o) {
            context.put(name, o);
        }
    }

    private void getSchemaFrom1Server(String collection, QueryRequestContext requestContext, final QueryResutStrategy queryResutStrategy, final List<ServerJoinGroup> serverlist) throws ServletException {
        // boolean isSuccessGet = false;
        for (ServerJoinGroup server : serverlist) {
            try {
                requestContext.schema = processSchema(queryResutStrategy.getRequest(), // http://http://10.1.4.145:8080/solr/search4shop_shard1_replica1/:0/solr/search4shopadmin/file/?file=schema.xml
                "http://" + server.getIp() + ":8080/solr/" + collection);
                // isSuccessGet = true;
                return;
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        requestContext.schema = new ParseResult(false);
    // StringBuffer servers = new StringBuffer();
    // for (ServerJoinGroup server : serverlist) {
    // servers.append("[").append(server.getIpAddress()).append("]");
    // 
    // }
    // throw new ServletException("remote server faild,remote servers:" +
    // servers.toString());
    }

    public static class QueryRequestContext {

        // final ResultCount count = new ResultCount();
        public AtomicLong resultCount = new AtomicLong();

        public final HttpServletRequest request;

        public ParseResult schema;

        public QueryRequestContext(HttpServletRequest request) {
            super();
            this.request = request;
        }

        public void add(long value) {
            this.resultCount.addAndGet(value);
        }

        public final boolean queryDebug = false;
    }

    private static ParseResult processSchema(final SolrQueryModuleCreator creator, final String url) throws MalformedURLException {
        return ConfigFileContext.processContent(new URL(url + "/admin/file/?file=schema.xml"), new StreamProcess<ParseResult>() {

            @Override
            public ParseResult p(int status, InputStream stream, String md5) {
                return creator.processSchema(stream);
            }
        });
    }
}
