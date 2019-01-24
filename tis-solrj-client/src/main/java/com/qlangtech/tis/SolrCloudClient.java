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
package com.qlangtech.tis;

//import java.io.File;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
//import java.nio.charset.Charset;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang.StringUtils;
//import org.apache.solr.client.solrj.SolrClient;
//import org.apache.solr.client.solrj.SolrQuery;
//import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.solr.client.solrj.StreamingResponseCallback;
//import org.apache.solr.client.solrj.impl.HttpSolrClient;
//import org.apache.solr.client.solrj.response.QueryResponse;
//import org.apache.solr.common.SolrDocument;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SolrCloudClient {

//    private static final Pattern IP_PATTERN = Pattern.compile("(\\d+?)\\.(\\d+?)\\.(\\d+?)\\.(\\d+)");
//
//    /**
//     * 線上執行
//     * java -classpath tisclientlib/*:. com.dfire.tis.SolrCloudClient curr_date:20160325 http://10.46.77.103:8080/solr/search4totalpay_shard1_replica2  http://10.47.53.68:8080/solr/search4totalpay_shard1_replica1 totalpay_id,_version_
//     *
//     * @param args
//     * @throws Exception
//     */
//    public static void main(String[] args) throws Exception {
//        final String queryParam = args[0];
//        final String url = args[1];
//        final String url2 = args[2];
//        final String fields = args[3];
//        SolrQuery query = new SolrQuery(queryParam);
//        // final Set<String> fl = new HashSet<String>(
//        // Arrays.asList());
//        args = new String[] { parse(queryParam, url, fields, query).getAbsolutePath(), parse(queryParam, url2, fields, query).getAbsolutePath() };
//        CompareFile.main(args);
//    }
//
//    public static File parse(final String queryParam, final String url, final String fields, SolrQuery query) throws IOException, SolrServerException {
//        final String[] fl = StringUtils.split(fields, ",");
//        SolrClient client = new HttpSolrClient(url);
//        Matcher m = IP_PATTERN.matcher(url);
//        File outfile = null;
//        if (m.find()) {
//            outfile = new File(m.group(0));
//        } else {
//            client.close();
//            throw new IllegalStateException("can not find ip,url:" + url);
//        }
//        System.out.println("queryParam:" + queryParam);
//        System.out.println("url:" + url);
//        System.out.println("fields:" + fields);
//        System.out.println("outfile:" + outfile.getAbsolutePath());
//        query.setDistrib(false);
//        query.setFields(fields);
//        query.setRows(9999999);
//        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(FileUtils.openOutputStream(outfile), Charset.forName("utf8")));
//        for (String f : fl) {
//            writer.print(f);
//            writer.print(",");
//        }
//        final AtomicInteger count = new AtomicInteger(0);
//        QueryResponse result = client.queryAndStreamResponse(query, new StreamingResponseCallback() {
//
//            @Override
//            public void streamSolrDocument(SolrDocument doc) {
//                System.out.println(count.incrementAndGet());
//                for (String f : fl) {
//                    writer.print(doc.getFieldValue(f));
//                    writer.print(",");
//                }
//                writer.println();
//                writer.flush();
//            }
//
//            @Override
//            public void streamDocListInfo(long numFound, long start, Float maxScore) {
//            // writer.println("numFound:" + numFound);
//            }
//        });
//        writer.close();
//        System.out.println("numFound:" + result.getResults().getNumFound());
//        client.close();
//        return outfile;
//    }
}
