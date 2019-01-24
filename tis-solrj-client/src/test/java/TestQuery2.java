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
import com.qlangtech.tis.solrj.extend.TisCloudSolrClient;
import org.apache.solr.client.solrj.SolrQuery;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestQuery2 {

    // public void testQuery() throws Exception {
    // SolrQuery query = new SolrQuery();
    // query.setQuery("totalpay_id:0002280950fabbb30150fb82eaf80157 AND
    // curr_date:[20140627 TO *]");
    // 
    // SimpleQueryResult<Totalpay> result = this.client.query(
    // "search4totalpay", "1234", query, Totalpay.class);
    // 
    // for (Totalpay pay : result.getResult()) {
    // System.out.println(pay.getTotalpayId());
    // }
    // 
    // }
    public static void main(String[] args) throws Exception {
        TisCloudSolrClient client = new TisCloudSolrClient("zk1.2dfire-inc.com:2181,zk2.2dfire-inc.com:2181,zk3.2dfire-inc.com:2181/tis/cloud");
        SolrQuery query = new SolrQuery();
        query.setQuery("customerregister_id:193d43b1734245f5d3bf35092dbb3a40");
        query.addFilterQuery("{!postfilter f=menu_id exclude=true}000008424a4234f0014a5746c2cd1065,000008424a4234f0014a5746c2cd1065");
        query.setQuery("entity_id:00000241");
        query.setRows(0);
        TisCloudSolrClient.SimpleQueryResult<Object> result = client.query("search4totalpay", "00000241", query, Object.class);
        System.out.println("getNumberFound:" + result.getNumberFound());
    }
}
