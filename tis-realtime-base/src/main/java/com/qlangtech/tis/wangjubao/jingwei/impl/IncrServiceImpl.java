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
package com.qlangtech.tis.wangjubao.jingwei.impl;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class IncrServiceImpl {
    // private ReversedServer reversedServer;
    // 
    // private TableCluster tableCluster;
    // 
    // private Log log = LoggerFactory.getLogger(IncrServiceImpl.class);
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
