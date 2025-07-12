///**
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.qlangtech.tis.realtime.servlet;
//
//import com.qlangtech.tis.realtime.transfer.IOnsListenerStatus;
//import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
//import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// *
// * @author: 百岁（baisui@qlangtech.com）
// * @create: 2025-07-10 15:28
// **/
//public class RealtimeReportWebSocketServlet extends WebSocketServlet {
//
//     private static final long serialVersionUID = 1L;
//
//     private Collection<IOnsListenerStatus> incrChannels;
//
//     private static final Logger logger = LoggerFactory.getLogger(RealtimeReportWebSocketServlet.class);
//
//     private static final ExecutorService exec = Executors.newCachedThreadPool();
//
//     /**
//     * @param incrChannels
//     */
//     public RealtimeReportWebSocketServlet() {
//     super();
//     }
//
//     @Override
//     public void configure(WebSocketServletFactory factory) {
//
//     }
//
//     @Override
//     public void init(ServletConfig config) throws ServletException {
//
//     super.init(config);
//     IndexSwapTaskflowLauncher launcherConext = IndexSwapTaskflowLauncher
//     .getIndexSwapTaskflowLauncher(config.getServletContext());
//     // Collection<IOnsListenerStatus> channels = getMockChannel();
//
//     this.incrChannels = launcherConext.getIncrChannels();
//     }
//
//     // public static Collection<IOnsListenerStatus> getMockChannel() {
//     // Collection<IOnsListenerStatus> channels = new ArrayList<>();
//     //
//     // for (int i = 0; i < 10; i++) {
//     // final String name = "search4a" + i;
//     // final Map<String, IIncreaseCounter> map = new HashMap<>();
//     // map.put("tab1", new IIncreaseCounter() {
//     //
//     // @Override
//     // public long getIncreasePastLast() {
//     //
//     // return (long) (Math.random() * 100);
//     // }
//     //
//     // @Override
//     // public long getAccumulation() {
//     //
//     // return (long) (Math.random() * 100);
//     // }
//     //
//     // });
//     //
//     // map.put("user", new IIncreaseCounter() {
//     //
//     // @Override
//     // public long getIncreasePastLast() {
//     //
//     // return (long) (Math.random() * 100);
//     // }
//     //
//     // @Override
//     // public long getAccumulation() {
//     //
//     // return (long) (Math.random() * 100);
//     // }
//     //
//     // });
//     // channels.add(new IOnsListenerStatus() {
//     // @Override
//     // public long getSolrConsumeIncrease() {
//     // return (long) (Math.random() * 100);
//     // }
//     //
//     // @Override
//     // public long getConsumeErrorCount() {
//     //
//     // return (long) (Math.random() * 100);
//     // }
//     //
//     // @Override
//     // public long getIgnoreRowsCount() {
//     //
//     // return (long) (Math.random() * 100);
//     // }
//     //
//     // @Override
//     // public void cleanLastAccumulator() {
//     // }
//     //
//     // @Override
//     // public String getCollectionName() {
//     //
//     // return name;
//     // }
//     //
//     // @Override
//     // public String getTableUpdateCount() {
//     //
//     // return null;
//     // }
//     //
//     // @Override
//     // public int getBufferQueueUsedSize() {
//     //
//     // return (int) (Math.random() * 100);
//     // }
//     //
//     // @Override
//     // public int getBufferQueueRemainingCapacity() {
//     //
//     // return (int) (Math.random() * 100);
//     // }
//     //
//     // @Override
//     // public long getConsumeIncreaseCount() {
//     //
//     // return (long) (Math.random() * 100);
//     // }
//     //
//     // @Override
//     // public void resumeConsume() {
//     // }
//     //
//     // @Override
//     // public void pauseConsume() {
//     // }
//     //
//     // @Override
//     // public Set<Entry<String, IIncreaseCounter>> getUpdateStatic() {
//     //
//     // return map.entrySet();
//     // }
//     //
//     // });
//     // }
//     // return channels;
//     // }
//
//     @Override
//     public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
//     RealtimeReportSocket realtimeReportSocket = new RealtimeReportSocket(incrChannels);
//     return realtimeReportSocket;
//     }
//
//     private static final Pattern CONSUME_KEY_PATTERN = Pattern.compile("consume\\d+");
//
//     private static class RealtimeReportSocket extends BasicIncrStatusReport implements WebSocket.OnTextMessage {
//
//     private WebSocket.Connection _connection;
//
//     @Override
//     protected boolean isClosed() {
//     return super.isClosed() && !_connection.isOpen();
//     }
//
//     public RealtimeReportSocket(Collection<IOnsListenerStatus> incrChannels) {
//     super(incrChannels);
//     }
//
//     private final Map<String, TimeWindow> timeWindowMap = new HashMap<String, TimeWindow>();
//
//     @Override
//     protected void processSnapshot() throws Exception {
//
//     JSONArray jarray = new JSONArray();
//     JSONObject o = null;
//     JSONObject statc = null;
//     long time = System.currentTimeMillis();
//     String collection = null;
//     TimeWindow timeWindow = null;
//     Matcher matcher = null;
//     for (IOnsListenerStatus l : this.incrChannels) {
//
//     // 某些增量状态现在是不用care的
//     if (TISCollectionUtils.ignoreIncrTransfer(l.getCollectionName())) {
//     continue;
//     }
//
//     long upateCount = l.getSolrConsumeIncrease();
//     o = new JSONObject();
//     collection = l.getCollectionName();
//     o.put("collection", collection);
//     int usedCapacity = l.getBufferQueueUsedSize();
//
//     if ((timeWindow = timeWindowMap.get(collection)) == null) {
//     synchronized (timeWindowMap) {
//     if ((timeWindow = timeWindowMap.get(collection)) == null) {
//     timeWindow = new TimeWindow();
//     timeWindowMap.put(collection, timeWindow);
//     }
//     }
//     }
//
//     timeWindow.setQPS((int) upateCount);
//
//     // (BasicONSListener.ALL_WAIT_QUEUE_LENGTH
//     // - l.getBufferQueueRemainingCapacity());
//     o.put("time", time);
//     o.put("usedqueuecapacity", usedCapacity);
//     o.put("updateqps", upateCount);
//     // 十秒平均值
//     o.put("updateqps10", timeWindow.average());
//     statc = new JSONObject();
//     for (Map.Entry<String, IIncreaseCounter> e : l.getUpdateStatic()) {
//     if (IIncreaseCounter.SOLR_CONSUME_COUNT.equals(e.getKey())) {
//     continue;
//     }
//
//     matcher = CONSUME_KEY_PATTERN.matcher(e.getKey());
//     if (matcher.matches()) {
//     continue;
//     }
//     statc.put(e.getKey(), e.getValue().getIncreasePastLast());
//     }
//     o.put("childtabtps", statc);
//     jarray.put(o);
//     }
//
//     String sendMsg = jarray.toString(1);
//
//     logger.info(sendMsg);
//
//     _connection.sendMessage(sendMsg);
//
//     }
//
//     private static class TimeWindow {
//     // 10秒之内的QPS ,為了求10秒平均QPS
//     private static final int ARRAY_LENGTH = 10;
//     private Integer[] qpsArray = new Integer[ARRAY_LENGTH];
//
//     public boolean setQPS(Integer val) {
//     if (index > (Integer.MAX_VALUE - 10000)) {
//     index = 0;
//     }
//     int offset = index++ % ARRAY_LENGTH;
//     qpsArray[offset] = val;
//     return offset == (ARRAY_LENGTH - 1);
//     }
//
//     public int index = 0;
//
//     public int average() {
//     int sum = 0;
//     int count = 0;
//     Integer qps = null;
//     for (int i = 0; i < qpsArray.length; i++) {
//     if ((qps = qpsArray[i]) != null) {
//     sum += qps;
//     count++;
//     }
//     }
//
//     return sum / ((count < 1) ? 1 : count);
//     }
//     }
//
//     @Override
//     public void onOpen(Connection connection) {
//     this._connection = connection;
//     // 开始发送消息
//     exec.execute(this);
//     }
//
//     @Override
//     public void onClose(int closeCode, String message) {
//     this.setClose();
//
//     }
//
//     @Override
//     public void onMessage(String data) {
//     }
//
//     }
//}
