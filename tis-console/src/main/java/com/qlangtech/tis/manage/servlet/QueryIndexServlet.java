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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.CheckAppDomainExistValve;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.runtime.module.screen.IndexQuery.QueryRequestContext;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.SolrFieldsParser.SchemaFields;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;
import junit.framework.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class QueryIndexServlet extends BasicServlet {

    private static final long serialVersionUID = 1L;

    private static final ExecutorService threadPool = java.util.concurrent.Executors.newCachedThreadPool();

    private static final Log log = LogFactory.getLog(QueryIndexServlet.class);

    // private ZkStateReader zkStateReader;
    // private SolrZkClient solrZkClient;
    private Cache<String, SchemaFields> /* collection name */
    schemaFieldsCache;

    public QueryIndexServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        this.schemaFieldsCache = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES).build();
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<String> emptylist = Collections.emptyList();
        doQuery(req, resp, CheckAppDomainExistValve.getAppDomain(req, this.getContext()), emptylist);
    }

    // private static final GeneralXMLResponseParser RESPONSE_PARSE = new
    // GeneralXMLResponseParser();
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse resp) throws ServletException, IOException {
        String[] sfields = request.getParameterValues("sfields");
        doQuery(request, resp, CheckAppDomainExistValve.getAppDomain(request, this.getContext()), Arrays.asList(sfields));
    }

    public void doQuery(final HttpServletRequest request, final HttpServletResponse resp, AppDomainInfo domain, List<String> sfields) throws ServletException, IOException {
        request.setAttribute("selectedFields", sfields);
        request.setCharacterEncoding("utf8");
        final String query = StringUtils.defaultIfBlank((request.getParameter("q")).replaceAll("\r|\n", StringUtils.EMPTY), "*:*");
        Integer shownum = null;
        try {
            shownum = Integer.parseInt(request.getParameter("shownum"));
        } catch (Throwable e2) {
        }
        QueryRequestContext requestContext = new QueryRequestContext(request);
        // 是否需要打印scoreexplain信息
        // requestContext.queryDebug =
        // "true".equalsIgnoreCase(request.getParameter("debugQuery"));
        final String sort = request.getParameter("sort");
        final String fq = request.getParameter("fq");
        if (domain instanceof com.qlangtech.tis.pubhook.common.Nullable) {
            throw new IllegalStateException("domain can not be nullable ");
        }
        final QueryResutStrategy queryResutStrategy = createQueryResutStrategy(domain, request, resp, this.getContext());
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
        execuetQuery(domain, requestContext, this.getContext(), queryResutStrategy, serverlist, query, sort, fq, shownum, sfields);
    // }
    }

    private void setSfields(final HttpServletRequest request, AppDomainInfo domain, QueryRequestContext requestContext, final QueryResutStrategy queryResutStrategy, final List<ServerJoinGroup> serverlist) throws ServletException {
        List<PSchemaField> fieldList = null;
        fieldList = this.schemaFieldsCache.getIfPresent(domain.getAppName());
        try {
            if (fieldList == null) {
                fieldList = this.schemaFieldsCache.get(domain.getAppName(), () -> {
                    getSchemaFrom1Server(domain, requestContext, queryResutStrategy, serverlist);
                    return requestContext.schema.dFields;
                });
            }
        } catch (ExecutionException e1) {
            throw new ServletException(e1);
        }
        request.setAttribute("sfields", fieldList);
    }

    /**
     * @param domain
     * @param requestContext
     * @param queryResutStrategy
     * @param serverlist
     * @throws ServletException
     */
    private void getSchemaFrom1Server(AppDomainInfo domain, QueryRequestContext requestContext, final QueryResutStrategy queryResutStrategy, final List<ServerJoinGroup> serverlist) throws ServletException {
        // boolean isSuccessGet = false;
        for (ServerJoinGroup server : serverlist) {
            try {
                requestContext.schema = processSchema(queryResutStrategy.getRequest(), // http://http://10.1.4.145:8080/solr/search4shop_shard1_replica1/:0/solr/search4shopadmin/file/?file=schema.xml
                "http://" + server.getIp() + ":8080/solr/" + domain.getAppName());
                // isSuccessGet = true;
                return;
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
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

    public static QueryResutStrategy createQueryResutStrategy(AppDomainInfo domain, final HttpServletRequest request, // , boolean queryResultAware
    final HttpServletResponse resp, // , boolean queryResultAware
    RunContext runContext) {
        final SolrQueryModuleCreatorAdapter creatorAdapter = new SolrQueryModuleCreatorAdapter() {

            @Override
            public boolean schemaAware() {
                return false;
            }

            @Override
            public SolrParams build(final String querystr, final String sort, final String fq, final Integer shownumf, final List<String> showFields) {
                SolrQuery query = new SolrQuery();
                // 增加排序字段
                if (StringUtils.isNotBlank(sort)) {
                    query.add(CommonParams.SORT, sort);
                }
                // query.add(CommonParams.Q, querystr);
                query.setQuery(querystr);
                if (StringUtils.isNotBlank(fq)) {
                    query.add(CommonParams.FQ, fq);
                }
                query.add(CommonParams.START, "0");
                // 默认显示前三行
                query.setRows(shownumf);
                // query.add(CommonParams.ROWS, String.valueOf(shownumf));
                query.add(CommonParams.VERSION, "2.2");
                query.add(CommonParams.WT, "xml");
                query.add(CommonParams.DISTRIB, "false");
                for (String field : showFields) {
                    query.addField(field);
                }
                query.setShowDebugInfo(true);
                return query;
            }

            @Override
            public ParseResult processSchema(InputStream schemaStream) {
                try {
                    SolrFieldsParser schemaParser = new SolrFieldsParser();
                    ParseResult parseResult = schemaParser.parseSchema(schemaStream);
                    return parseResult;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String[] getParameterValues(String keyname) {
                return request.getParameterValues(keyname);
            }

            @Override
            public void handleError(String url, ServerJoinGroup server, long allcount, Exception e) {
                synchronized (resp) {
                    try {
                        flushResult(resp, request, Arrays.asList(convert2Html("url:" + url + "<br/>" + getErrorContent(e), server)), allcount);
                    } catch (IOException e1) {
                    }
                }
            }

            @Override
            public void processResult(QueryRequestContext qrequest, QueryResponse result, ServerJoinGroup server) throws Exception {
                flushResult(resp, qrequest.request, convert2Html(qrequest, result, server), qrequest.resultCount.get());
            }

            @Override
            public void selectedCanidateServers(Collection<String> selectedCanidateServers) {
                request.setAttribute("selectedCanidateServers", selectedCanidateServers);
            }

            @Override
            public void setQuerySelectServerCandiate(Map<Short, List<ServerJoinGroup>> servers) {
                request.setAttribute("querySelectServerCandiate", servers);
            }
        };
        final QueryResutStrategy queryResutStrategy = SolrCloudQueryResutStrategy.create(domain, creatorAdapter, runContext);
        return queryResutStrategy;
    }

    public static String getErrorContent(Throwable e) {
        StringWriter reader = new StringWriter();
        PrintWriter errprint = null;
        try {
            errprint = new PrintWriter(reader);
            e.printStackTrace(errprint);
            return processContent2Json(reader.toString());
        // StringUtils.trimToEmpty(
        // StringUtils.replace(reader.toString(), "\"", "'"))
        // .replaceAll("(\r|\n|\t)+", "<br/>");
        } finally {
            IOUtils.closeQuietly(errprint);
        }
    }

    // public static void execuetQuery(final AppDomainInfo domain, final
    // SolrQueryModuleCreator creator,
    // RunContext getContext) {
    // final QueryResutStrategy queryResutStrategy =
    // SolrCloudQueryResutStrategy.create(domain, creator, getContext);
    // final List<ServerJoinGroup> serverlist = queryResutStrategy.queryProcess();
    // QueryRequestContext requestContext = new QueryRequestContext(null);
    // execuetQuery(domain, requestContext, getContext, queryResutStrategy,
    // serverlist);
    // }
    /**
     * 执行查询逻辑和使用的场景无关
     *
     * @param domain
     * @param creator
     * @param getContext
     */
    public static void execuetQuery(final AppDomainInfo domain, final QueryRequestContext requestContext, RunContext getContext, final QueryResutStrategy queryResutStrategy, final List<ServerJoinGroup> serverlist, final String querystr, final String sort, final String fq, final Integer shownumf, final List<String> showFields) {
        Assert.assertNotNull("param SolrQueryModuleCreator can not be null", queryResutStrategy.getRequest());
        Assert.assertNotNull(queryResutStrategy);
        Assert.assertNotNull(serverlist);
        final CountDownLatch lock = new CountDownLatch(serverlist.size());
        final AtomicBoolean hasGetSchema = new AtomicBoolean(false);
        synchronized (lock) {
            for (final ServerJoinGroup server : serverlist) {
                threadPool.execute(new Runnable() {

                    @Override
                    public void run() {
                        // 组装url
                        // createApplyUrl(domain, queryResutStrategy, server);
                        final String url = server.getIpAddress();
                        try {
                            // 取得schema信息
                            // if (queryResutStrategy.getRequest().schemaAware()
                            // && hasGetSchema.compareAndSet(false, true)) {
                            // processSchema(queryResutStrategy.getRequest(), url);
                            // }
                            // if (!queryResutStrategy.getRequest().queryResultAware()) {
                            // return;
                            // }
                            QueryCloudSolrClient solrClient = new QueryCloudSolrClient(url);
                            QueryResponse result = solrClient.query(domain.getAppName(), queryResutStrategy.getRequest().build(querystr, sort, fq, shownumf, showFields), METHOD.POST);
                            solrClient.close();
                            // CommonsHttpSolrServer solr = new
                            // CommonsHttpSolrServer(
                            // new URL(url), null, RESPONSE_PARSE, false);
                            // QueryResponse result = solr.query(
                            // queryResutStrategy.getRequest().build(),
                            // METHOD.POST);
                            long c = result.getResults().getNumFound();
                            if (c < 1) {
                                return;
                            }
                            // count.add(c);
                            requestContext.add(c);
                            queryResutStrategy.getRequest().processResult(requestContext, result, server);
                        } catch (Exception e) {
                            e.printStackTrace();
                            queryResutStrategy.getRequest().handleError(url, server, requestContext.resultCount.get(), e);
                        } finally {
                            lock.countDown();
                        }
                    }
                });
            }
        }
        try {
            lock.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // private static String createApplyUrl(final AppDomainInfo domain, final
    // QueryResutStrategy queryResutStrategy,
    // final ServerJoinGroup server) {
    // return "http://" + StringUtils.trim(server.getIpAddress()) //
    // (queryResutStrategy.getServicePort())
    // + "/solr/" + domain.getAppName();// + server.getGroupIndex();
    // // return server.getIpAddress();
    // }
    private static ParseResult processSchema(final SolrQueryModuleCreator creator, final String url) throws MalformedURLException {
        return ConfigFileContext.processContent(new URL(url + "/admin/file/?file=schema.xml"), new StreamProcess<ParseResult>() {

            @Override
            public ParseResult p(int status, InputStream stream, String md5) {
                return creator.processSchema(stream);
            }
        });
    }

    public static interface SolrQueryModuleCreator {

        public SolrParams build(final String querystr, final String sort, final String fq, final Integer shownumf, final List<String> showField);

        public void processResult(QueryRequestContext qrequest, QueryResponse result, ServerJoinGroup server) throws Exception;

        public void handleError(String url, ServerJoinGroup server, long allcount, Exception e);

        public void selectedCanidateServers(final Collection<String> selectedCanidateServers);

        public void setQuerySelectServerCandiate(Map<Short, List<ServerJoinGroup>> servers);

        /**
         * 处理schema流
         *
         * @param schemaStream
         */
        public ParseResult processSchema(InputStream schemaStream);

        public boolean schemaAware();

        // public boolean queryResultAware();
        /**
         * 取得查询参数
         *
         * @param keyname
         * @return
         */
        public String[] getParameterValues(String keyname);
    }

    public static void flushResult(final HttpServletResponse resp, final HttpServletRequest req, List<Row> rowlist, long allcount) throws IOException {
        Assert.assertNotNull(req);
        String callback = req.getParameter("callback");
        Assert.assertNotNull("param callback can not be null", callback);
        // rowlist = new ArrayList<Row>(rowlist);
        synchronized (resp) {
            PrintWriter writer = resp.getWriter();
            writer.write(callback + "(");
            JSONObject j = new JSONObject();
            JSONArray rows = new JSONArray();
            JSONObject rr = null;
            for (Row r : rowlist) {
                rr = new JSONObject();
                rr.put("server", r.getServer());
                rr.put("rowContent", r.getRowContent());
                rows.put(rr);
            }
            j.put("result", rows);
            j.put("rownum", allcount);
            writer.write(j.toString(1));
            writer.write(");\n");
        }
    }

    private static class ResultCount {

        private AtomicLong value = new AtomicLong();

        public void add(long v) {
            // this.value += v;
            value.addAndGet(v);
        }
    }

    @SuppressWarnings("all")
    private static List<Row> convert2Html(QueryRequestContext requestContext, QueryResponse response, ServerJoinGroup server) {
        // StringBuffer result = new StringBuffer();
        // 
        SolrDocumentList solrDocumentList = response.getResults();
        String uniqueKey = null;
        List<Row> result = new ArrayList<Row>();
        Row record = null;
        SimpleOrderedMap explain = null;
        if (requestContext.queryDebug) {
            explain = (SimpleOrderedMap) response.getDebugMap().get("explain");
        }
        for (SolrDocument document : solrDocumentList) {
            StringBuffer temp = new StringBuffer();
            for (String key : document.getFieldNames()) {
                temp.append("<strong>").append(key).append("</strong>");
                temp.append(":").append(processContent2Json(String.valueOf(document.get(key)))).append(" ");
            }
            record = convert2Html(temp.toString(), server);
            if (requestContext.queryDebug) {
                uniqueKey = getUniqueKey(requestContext, document);
                record.setPk(uniqueKey);
                record.explain = processContent2Json(String.valueOf(explain.get(uniqueKey)));
            }
            result.add(record);
        }
        return result;
    }

    public static String processContent2Json(String content) {
        return StringUtils.trimToEmpty(StringEscapeUtils.escapeHtml4(content)).replaceAll("(\r|\n)+", "<br/>").replaceAll("\\s|\t", "&nbsp;");
    }

    private static String getUniqueKey(QueryRequestContext request, SolrDocument document) {
        Assert.assertNotNull("request.schema can not be null", request.schema);
        return String.valueOf(document.get(request.schema.getUniqueKey()));
    }

    // public static class QueryRequestContext {
    // // final ResultCount count = new ResultCount();
    // private AtomicLong resultCount = new AtomicLong();
    // private final HttpServletRequest request;
    // private ParseResult schema;
    // 
    // public QueryRequestContext(HttpServletRequest request) {
    // super();
    // this.request = request;
    // }
    // 
    // void add(long value) {
    // this.resultCount.addAndGet(value);
    // }
    // 
    // private boolean queryDebug;
    // }
    // private static final Pattern IP_PATTERN =
    // Pattern.compile("//([\\d|\\.]+)");
    public static Row convert2Html(String rightcell, ServerJoinGroup server) {
        Row result = new Row();
        result.setRowContent(rightcell);
        result.setServer("[" + server.getGroupIndex() + "]" + server.getIp());
        return result;
    }

    private static class Row {

        private String server;

        private String rowContent;

        private String explain;

        private String pk;

        String getExplain() {
            return explain;
        }

        void setExplain(String explain) {
            this.explain = explain;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getRowContent() {
            return rowContent;
        }

        private String getPk() {
            return pk;
        }

        private void setPk(String pk) {
            this.pk = pk;
        }

        public void setRowContent(String rowContent) {
            this.rowContent = rowContent;
        }
    }

    public static void main(String[] arg) throws Exception {
    // CommonsHttpSolrServer solr = new CommonsHttpSolrServer(
    // new URL(
    // "http://10.235.160.77:8080/terminator-search/search4barbarian-0"),
    // null, RESPONSE_PARSE, false);
    // Matcher p = IP_PATTERN
    // .matcher("http://20.1.7.41:8983/solr/search4totalpay_shard1_replica1");
    // if (p.find()) {
    // System.out.println(p.group(1));
    // } else {
    // 
    // }
    }
}
