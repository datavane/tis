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
package com.qlangtech.tis.wangjubao.jingwei.impl;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IncrServiceImpl {
    // private ReversedServer reversedServer;
    // 
    // private TableCluster tableCluster;
    // 
    // private Log log = LogFactory.getLog(IncrServiceImpl.class);
    // 
    // private static final Map<String, Count> updateSummpay = new HashMap<String, Count>();
    // 
    // @Override
    // public void update(String indexName, Integer group,
    // UpdateDocumentRequest request) {
    // increaseAcc(indexName + "_update");
    // valiateTable(indexName);
    // log.info("update " + indexName + "-" + group);
    // reversedServer.updateDocument(indexName + "-" + group, request, 3,
    // TimeUnit.SECONDS);
    // }
    // 
    // @Override
    // public void delete(String table, Integer group, DeleteByIdRequest deleteId) {
    // increaseAcc(table + "_delete");
    // log.info("delete:" + table + "-" + group);
    // reversedServer.deleteDocument(table + "-" + group, deleteId, 10,
    // TimeUnit.SECONDS);
    // }
    // 
    // private static class Count {
    // private long value;
    // 
    // public void increase() {
    // this.value++;
    // }
    // }
    // 
    // private static long currentTimeStamp;
    // 
    // private void increaseAcc(String indexName) {
    // Count count = updateSummpay.get(indexName);
    // if (count == null) {
    // synchronized (updateSummpay) {
    // if (count == null) {
    // count = new Count();
    // updateSummpay.put(indexName, count);
    // }
    // }
    // }
    // count.increase();
    // // report execute count every 15 sec
    // long current = System.currentTimeMillis();
    // if (current > (currentTimeStamp + 1000 * 30)) {
    // synchronized (updateSummpay) {
    // if (current > (currentTimeStamp + 1000 * 30)) {
    // StringBuffer executesummary = new StringBuffer();
    // for (Map.Entry<String, Count> entry : updateSummpay
    // .entrySet()) {
    // executesummary.append(
    // "index:" + entry.getKey() + ",count:"
    // + entry.getValue().value).append("\n");
    // }
    // log.warn(executesummary.toString());
    // reversedServer.printAllSessionConnections();
    // currentTimeStamp = System.currentTimeMillis();
    // }
    // }
    // 
    // }
    // }
    // 
    // /**
    // * @param table
    // */
    // private void valiateTable(String table) {
    // // Table tab = tableCluster.getTable(table);
    // // if (tab == null) {
    // // throw new IllegalStateException("table " + table
    // // + " can not be null");
    // // }
    // }
    // 
    // @Override
    // public void add(String indexName, Integer group, AddDocumentRequest request) {
    // valiateTable(indexName);
    // increaseAcc(indexName + "_add");
    // log.info("add " + indexName + "-" + group);
    // reversedServer.addDocument(indexName + "-" + group, request, 3,
    // TimeUnit.SECONDS);
    // }
    // 
    // public TableCluster getTableCluster() {
    // return tableCluster;
    // }
    // 
    // public void setTableCluster(TableCluster tableCluster) {
    // this.tableCluster = tableCluster;
    // }
    // 
    // public ReversedServer getReversedServer() {
    // return reversedServer;
    // }
    // 
    // public void setReversedServer(ReversedServer reversedServer) {
    // this.reversedServer = reversedServer;
    // }
}
