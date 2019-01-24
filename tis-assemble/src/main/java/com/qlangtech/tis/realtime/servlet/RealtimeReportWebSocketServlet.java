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
package com.qlangtech.tis.realtime.servlet;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RealtimeReportWebSocketServlet {
    // private static final long serialVersionUID = 1L;
    // 
    // private Collection<IOnsListenerStatus> incrChannels;
    // 
    // private static final Logger logger = LoggerFactory.getLogger(RealtimeReportWebSocketServlet.class);
    // 
    // private static final ExecutorService exec = Executors.newCachedThreadPool();
    // 
    // /**
    // * @param incrChannels
    // */
    // public RealtimeReportWebSocketServlet() {
    // super();
    // }
    // 
    // @Override
    // public void init(ServletConfig config) throws ServletException {
    // 
    // super.init(config);
    // IndexSwapTaskflowLauncher launcherConext = IndexSwapTaskflowLauncher
    // .getIndexSwapTaskflowLauncher(config.getServletContext());
    // // Collection<IOnsListenerStatus> channels = getMockChannel();
    // 
    // this.incrChannels = launcherConext.getIncrChannels();
    // }
    // 
    // // public static Collection<IOnsListenerStatus> getMockChannel() {
    // // Collection<IOnsListenerStatus> channels = new ArrayList<>();
    // //
    // // for (int i = 0; i < 10; i++) {
    // // final String name = "search4a" + i;
    // // final Map<String, IIncreaseCounter> map = new HashMap<>();
    // // map.put("tab1", new IIncreaseCounter() {
    // //
    // // @Override
    // // public long getIncreasePastLast() {
    // //
    // // return (long) (Math.random() * 100);
    // // }
    // //
    // // @Override
    // // public long getAccumulation() {
    // //
    // // return (long) (Math.random() * 100);
    // // }
    // //
    // // });
    // //
    // // map.put("user", new IIncreaseCounter() {
    // //
    // // @Override
    // // public long getIncreasePastLast() {
    // //
    // // return (long) (Math.random() * 100);
    // // }
    // //
    // // @Override
    // // public long getAccumulation() {
    // //
    // // return (long) (Math.random() * 100);
    // // }
    // //
    // // });
    // // channels.add(new IOnsListenerStatus() {
    // // @Override
    // // public long getSolrConsumeIncrease() {
    // // return (long) (Math.random() * 100);
    // // }
    // //
    // // @Override
    // // public long getConsumeErrorCount() {
    // //
    // // return (long) (Math.random() * 100);
    // // }
    // //
    // // @Override
    // // public long getIgnoreRowsCount() {
    // //
    // // return (long) (Math.random() * 100);
    // // }
    // //
    // // @Override
    // // public void cleanLastAccumulator() {
    // // }
    // //
    // // @Override
    // // public String getCollectionName() {
    // //
    // // return name;
    // // }
    // //
    // // @Override
    // // public String getTableUpdateCount() {
    // //
    // // return null;
    // // }
    // //
    // // @Override
    // // public int getBufferQueueUsedSize() {
    // //
    // // return (int) (Math.random() * 100);
    // // }
    // //
    // // @Override
    // // public int getBufferQueueRemainingCapacity() {
    // //
    // // return (int) (Math.random() * 100);
    // // }
    // //
    // // @Override
    // // public long getConsumeIncreaseCount() {
    // //
    // // return (long) (Math.random() * 100);
    // // }
    // //
    // // @Override
    // // public void resumeConsume() {
    // // }
    // //
    // // @Override
    // // public void pauseConsume() {
    // // }
    // //
    // // @Override
    // // public Set<Entry<String, IIncreaseCounter>> getUpdateStatic() {
    // //
    // // return map.entrySet();
    // // }
    // //
    // // });
    // // }
    // // return channels;
    // // }
    // 
    // @Override
    // public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
    // RealtimeReportSocket realtimeReportSocket = new RealtimeReportSocket(incrChannels);
    // return realtimeReportSocket;
    // }
    // 
    // private static final Pattern CONSUME_KEY_PATTERN = Pattern.compile("consume\\d+");
    // 
    // private static class RealtimeReportSocket extends BasicIncrStatusReport implements WebSocket.OnTextMessage {
    // 
    // private WebSocket.Connection _connection;
    // 
    // @Override
    // protected boolean isClosed() {
    // return super.isClosed() && !_connection.isOpen();
    // }
    // 
    // public RealtimeReportSocket(Collection<IOnsListenerStatus> incrChannels) {
    // super(incrChannels);
    // }
    // 
    // private final Map<String, TimeWindow> timeWindowMap = new HashMap<String, TimeWindow>();
    // 
    // @Override
    // protected void processSnapshot() throws Exception {
    // 
    // JSONArray jarray = new JSONArray();
    // JSONObject o = null;
    // JSONObject statc = null;
    // long time = System.currentTimeMillis();
    // String collection = null;
    // TimeWindow timeWindow = null;
    // Matcher matcher = null;
    // for (IOnsListenerStatus l : this.incrChannels) {
    // 
    // // 某些增量状态现在是不用care的
    // if (TISCollectionUtils.ignoreIncrTransfer(l.getCollectionName())) {
    // continue;
    // }
    // 
    // long upateCount = l.getSolrConsumeIncrease();
    // o = new JSONObject();
    // collection = l.getCollectionName();
    // o.put("collection", collection);
    // int usedCapacity = l.getBufferQueueUsedSize();
    // 
    // if ((timeWindow = timeWindowMap.get(collection)) == null) {
    // synchronized (timeWindowMap) {
    // if ((timeWindow = timeWindowMap.get(collection)) == null) {
    // timeWindow = new TimeWindow();
    // timeWindowMap.put(collection, timeWindow);
    // }
    // }
    // }
    // 
    // timeWindow.setQPS((int) upateCount);
    // 
    // // (BasicONSListener.ALL_WAIT_QUEUE_LENGTH
    // // - l.getBufferQueueRemainingCapacity());
    // o.put("time", time);
    // o.put("usedqueuecapacity", usedCapacity);
    // o.put("updateqps", upateCount);
    // // 十秒平均值
    // o.put("updateqps10", timeWindow.average());
    // statc = new JSONObject();
    // for (Map.Entry<String, IIncreaseCounter> e : l.getUpdateStatic()) {
    // if (IIncreaseCounter.SOLR_CONSUME_COUNT.equals(e.getKey())) {
    // continue;
    // }
    // 
    // matcher = CONSUME_KEY_PATTERN.matcher(e.getKey());
    // if (matcher.matches()) {
    // continue;
    // }
    // statc.put(e.getKey(), e.getValue().getIncreasePastLast());
    // }
    // o.put("childtabtps", statc);
    // jarray.put(o);
    // }
    // 
    // String sendMsg = jarray.toString(1);
    // 
    // logger.info(sendMsg);
    // 
    // _connection.sendMessage(sendMsg);
    // 
    // }
    // 
    // private static class TimeWindow {
    // // 10秒之内的QPS ,為了求10秒平均QPS
    // private static final int ARRAY_LENGTH = 10;
    // private Integer[] qpsArray = new Integer[ARRAY_LENGTH];
    // 
    // public boolean setQPS(Integer val) {
    // if (index > (Integer.MAX_VALUE - 10000)) {
    // index = 0;
    // }
    // int offset = index++ % ARRAY_LENGTH;
    // qpsArray[offset] = val;
    // return offset == (ARRAY_LENGTH - 1);
    // }
    // 
    // public int index = 0;
    // 
    // public int average() {
    // int sum = 0;
    // int count = 0;
    // Integer qps = null;
    // for (int i = 0; i < qpsArray.length; i++) {
    // if ((qps = qpsArray[i]) != null) {
    // sum += qps;
    // count++;
    // }
    // }
    // 
    // return sum / ((count < 1) ? 1 : count);
    // }
    // }
    // 
    // @Override
    // public void onOpen(Connection connection) {
    // this._connection = connection;
    // // 开始发送消息
    // exec.execute(this);
    // }
    // 
    // @Override
    // public void onClose(int closeCode, String message) {
    // this.setClose();
    // 
    // }
    // 
    // @Override
    // public void onMessage(String data) {
    // }
    // 
    // }
}
