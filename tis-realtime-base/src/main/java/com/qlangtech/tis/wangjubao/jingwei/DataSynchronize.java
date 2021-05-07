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
package com.qlangtech.tis.wangjubao.jingwei;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DataSynchronize {
    // public static BlockingQueue<String> queue = new
    // LinkedBlockingQueue<String>(
    // 100000);
    // 
    // private Log log = LoggerFactory.getLogger(DataSynchronize.class);
    // 
    // public static Map<Long, Long> timeConsume = new HashMap<Long, Long>();
    // public static Map<Long, Long> consume = new HashMap<Long, Long>();
    // 
    // public static long idid = 00L;
    // public static long addCount = 10000;
    // // key ���
    // private Map<String, MultiCoreServer> multiCoreServer;
    // 
    // // private final String serviceName;
    // private final TSearcherConfigFetcher configFetcher;
    // private final CommonZkClient zkClient;
    // 
    // private TableCluster tableCluster;
    // 
    // public DataSynchronize() throws Exception {
    // // this.serviceName = serviceName;
    // this.configFetcher = TSearcherConfigFetcher.getInstance("search4");
    // this.zkClient = new CommonZkClient(configFetcher.getZkAddress(), 30000);
    // 
    // TableClusterParser tabClusterParser = new TableClusterParser();
    // this.tableCluster = tabClusterParser
    // .parse(FileUtils
    // .readFileToString(new File(
    // "D:\\tmp\\terminator-jingwei\\src\\main\\resources\\wjb.xml")));
    // 
    // for (Table tab : tableCluster.getTables()) {
    // this.multiCoreServer.put(tab.getIndexName(), new MultiCoreServer(
    // tab.getIndexName(), this.zkClient));
    // }
    // 
    // log.warn("has initial the core server "
    // + this.multiCoreServer.keySet().size() + " has been construct");
    // }
    // 
    // public void send(final DBMSRowChange event) throws Exception {
    // 
    // DBMSAction action = event.getAction();
    // //
    // switch (action.value()) {
    // case 'I':
    // executeAddProcess(new ProcessStrategy() {
    // public Object process(Document doc) {
    // return AddDocumentRequest.newBuilder().setSolrDoc(doc)
    // .build();
    // }
    // }, event);
    // case 'U':
    // executeAddProcess(new ProcessStrategy() {
    // public Object process(Document doc) {
    // return UpdateDocumentRequest.newBuilder().setSolrDoc(doc)
    // .build();
    // }
    // }, event);
    // break;
    // case 'D':
    // // deleteDoc2TSearcher(documentList, "Delete");
    // break;
    // default:
    // break;
    // }
    // 
    // }
    // 
    // /**
    // * @param event
    // * @throws CommonZKException
    // */
    // private void executeAddProcess(ProcessStrategy processStrategy,
    // final DBMSRowChange event) throws CommonZKException {
    // Table table = tableCluster.getTable(event.getTable());
    // if (table == null) {
    // throw new IllegalStateException(event.getTable()
    // + " is not define in config file");
    // }
    // 
    // DBMSColumn sharedKey = event.findColumn(tableCluster.getSharedKey());
    // if (sharedKey == null) {
    // throw new IllegalStateException("shared key:"
    // + tableCluster.getSharedKey() + " is not define in table:"
    // + table.getName());
    // }
    // long group = 0;
    // CoreServer server = null;
    // for (int row = 1; row <= event.getRowSize(); row++) {
    // 
    // group = Long.parseLong(String.valueOf(event.getRowValue(row,
    // sharedKey)))
    // % (long) assertNotNull(
    // table.getIndexName()
    // + " core server can null be null",
    // this.multiCoreServer.get(table.getIndexName()))
    // .getGroupSize();
    // 
    // server = this.multiCoreServer.get(table.getIndexName())
    // .getServers().get((int) group);
    // 
    // if (server == null) {
    // throw new IllegalStateException("serviceName:"
    // + table.getIndexName() + " group:" + group
    // + " multiCoreServer is null");
    // }
    // final int rrow = row;
    // final Document.Builder doc = Document.newBuilder().setBoost(1.0f);
    // 
    // for (DBMSColumn column : event.getColumns()) {
    // 
    // doc.addFields(InputFields
    // .newBuilder()
    // .setKey(StringUtils.defaultIfBlank(
    // table.findAliasName(column.getName()),
    // column.getName()))
    // .addStrField(
    // this.getStringVal(
    // column,
    // event.getRowValue(rrow,
    // column.getColumnIndex())))
    // .setType(getType(column)).build());
    // }
    // 
    // // table.traverse(new FieldProcess() {
    // // public void process(TabField field) {
    // //
    // // }
    // // });
    // 
    // server.sendData(processStrategy.process(doc.build()));
    // 
    // // server.sendData(AddDocumentRequest.newBuilder()
    // // .setSolrDoc(doc.build()).build());
    // }
    // }
    // 
    // private static <T> T assertNotNull(String msg, T obj) {
    // if (obj == null) {
    // throw new NullPointerException(msg);
    // }
    // return obj;
    // }
    // 
    // private final ThreadLocal<SimpleDateFormat> localformat = new
    // ThreadLocal<SimpleDateFormat>() {
    // @Override
    // protected SimpleDateFormat initialValue() {
    // return new SimpleDateFormat("yyyyMMddHHmmss");
    // }
    // };
    // 
    // private Module.Type getType(DBMSColumn column) {
    // if (Types.DATE == column.getSqlType()
    // || Types.TIME == column.getSqlType()
    // || Types.TIMESTAMP == column.getSqlType()) {
    // return Module.Type.LONG;
    // }
    // 
    // if (Types.INTEGER == column.getSqlType()
    // || (Types.BIGINT == column.getSqlType())
    // || Types.NUMERIC == column.getSqlType()) {
    // return Module.Type.LONG;
    // }
    // 
    // if (Types.FLOAT == column.getSqlType()
    // || Types.DOUBLE == column.getSqlType()) {
    // return Module.Type.DOUBLE;
    // }
    // 
    // if (Types.TINYINT == column.getSqlType()
    // || Types.SMALLINT == column.getSqlType()) {
    // return Module.Type.INTEGER;
    // }
    // 
    // return Module.Type.STRING;
    // }
    // 
    // // private Module.Type parseFieldType(String colType) {
    // // if (StringUtils.isBlank(colType) || "str".equals(colType)) {
    // // return Module.Type.STRING;
    // // }
    // //
    // // if ("int".equals(colType)) {
    // // return Module.Type.INTEGER;
    // // }
    // //
    // // if ("double".equals(colType)) {
    // // return Module.Type.DOUBLE;
    // // }
    // //
    // // if ("float".equals(colType)) {
    // // return Module.Type.FLOAT;
    // // }
    // //
    // // if ("long".equals(colType)) {
    // // return Module.Type.LONG;
    // // }
    // //
    // // throw new IllegalArgumentException("colType:" + colType +
    // " is illegal");
    // // }
    // 
    // private String getStringVal(DBMSColumn column, Serializable val) {
    // 
    // if (Types.DATE == column.getSqlType()) {
    // return localformat.get().format(val);
    // }
    // 
    // return String.valueOf(val);
    // 
    // }
    // 
    // // static class AddData implements Runnable {
    // //
    // // @Override
    // // public void run() {
    // //
    // // MessageSender sender = createDataSender();
    // //
    // // List<String> lists = new ArrayList<String>();
    // // Random random = new Random();
    // // Long id = idid;
    // //
    // // int count = 0;
    // // int suc = 0;
    // // int error = 0;
    // //
    // // while (true) {
    // // Map<String, Object> map = new HashMap<String, Object>();
    // //
    // // map.put("id", (id) + "");
    // // map.put("province", RandomString(10 + random.nextInt(20)));
    // // map.put("city", RandomString(10 + random.nextInt(20)));
    // // map.put("district", RandomString(30 + random.nextInt(20)));
    // // map.put("town_name", RandomString(10 + random.nextInt(20)));
    // // map.put("town_code", RandomString(20 + random.nextInt(20)));
    // // map.put("poi_name", RandomString(10 + random.nextInt(20)));
    // // map.put("geohash", RandomString(10 + random.nextInt(20)));
    // //
    // // String res = "";
    // // Document.Builder doc = Document.newBuilder().setBoost(1.0f);
    // // for (Entry<String, Object> entry : map.entrySet()) {
    // // res += entry.getKey() + ":" + entry.getValue() + ",";
    // // if (entry.getValue() instanceof Long) {
    // 
    // // } else if (entry.getValue() instanceof Integer) {
    // // doc.addFields(InputFields.newBuilder()
    // // .setKey(entry.getKey())
    // // .addStrField(entry.getValue().toString())
    // // .setType(Type.INTEGER).build());
    // // } else if (entry.getValue() instanceof String) {
    // // doc.addFields(InputFields.newBuilder()
    // // .setKey(entry.getKey())
    // // .addStrField(entry.getValue().toString())
    // // .setType(Type.STRING).build());
    // // }
    // // }
    // //
    // // lists.add(res);
    // //
    // // lists = new ArrayList<String>();
    // //
    // // try {
    // // queue.put((String) map.get("id"));
    // //
    // // AddDocumentResponse object = (AddDocumentResponse) sender
    // // .sendAndWait(AddDocumentRequest.newBuilder()
    // // .setSolrDoc(doc.build()).build(), 10,
    // // TimeUnit.SECONDS);
    // // long startTime = System.currentTimeMillis();
    // // timeConsume.put(id, Long.valueOf(startTime));
    // //
    // // if (object.getErrorCode() == 200) {
    // // suc++;
    // // } else
    // // error++;
    // //
    // // count++;
    // //
    // // if (count >= addCount)
    // // break;
    // //
    // // } catch (Exception e) {
    // // e.printStackTrace();
    // // }
    // // id++;
    // // }
    // // System.out.println("Add�������");
    // // }
    // 
    // /**
    // * @return
    // */
    // // private MessageSender createDataSender() {
    // // ProtocolBufferEncoder encoder = new ProtocolBufferEncoder();
    // //
    // // ProtocolBufferDecoder decoder = new ProtocolBufferDecoder();
    // //
    // // List<String> handlers = new ArrayList<String>();
    // // handlers.add("com.taobao.common.transport.protoc");
    // // decoder.setBuilderMetaInfo(ProtocolBufferUtils
    // // .createBuilderMetainfo(handlers));
    // //
    // // DefaultMinaCodecFactory codecFactory = new DefaultMinaCodecFactory();
    // // codecFactory.setDecoder(decoder);
    // // codecFactory.setEncoder(encoder);
    // //
    // // BlockingTCPConnector conn = new BlockingTCPConnector(1);
    // // conn.setCodecFactory(codecFactory);
    // // conn.setDestIp("10.125.49.63");
    // // conn.setDestPort(7777);
    // // conn.setStopSessionOnTimeout(true);
    // //
    // // MessageSender sender = new MessageSender();
    // // sender.setConnetor(conn);
    // // sender.start();
    // // return sender;
    // // }
    // //
    // // }
    // 
    // // private static String printRowChange(StringBuilder builder,
    // // DBMSRowChange event, String lastSchema) {
    // // // Output schema name if needed.
    // // String schema = event.getSchema();
    // // lastSchema = printSchema(builder, schema, lastSchema);
    // // println(builder, "- TABLE = " + event.getTable());
    // // println(builder, "- ACTION = " + event.getAction());
    // //
    // // // Output row change details.
    // // final int columnCount = event.getColumnSize();
    // // for (int rownum = 1; rownum <= event.getRowSize(); rownum++) {
    // // println(builder, " - ROW# = " + rownum);
    // //
    // // // Print column values.
    // // DBMSRowData data = event.getRowData(rownum);
    // // for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
    // // DBMSColumn column = event.getColumn(columnIndex);
    // // Serializable value = data.getRowValue(columnIndex);
    // // printColumn(builder, column, value, "COL");
    // // builder.append('\n');
    // // }
    // //
    // // // Print change values.
    // // DBMSRowData dataChange = event.getChangeData(rownum);
    // // for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
    // // if (event.hasChangeColumn(columnIndex)) {
    // // DBMSColumn column = event.getColumn(columnIndex);
    // // Serializable value = dataChange.getRowValue(columnIndex);
    // // printColumn(builder, column, value, "UPD");
    // // builder.append('\n');
    // // }
    // // }
    // // }
    // //
    // // return lastSchema;
    // // }
    // 
    // // static class QueryThread implements Runnable {
    // //
    // // public SearchService searchService;
    // //
    // // public QueryThread(SearchService searchService) {
    // // this.searchService = searchService;
    // // }
    // //
    // // @Override
    // // public void run() {
    // // long id = idid;
    // // int count = 0;
    // // long totalTime = 0L;
    // // int requestTime = 0;
    // // while (true) {
    // // // Long startTime = System.currentTimeMillis();
    // // while (true) {
    // // TerminatorQueryRequest query = new TerminatorQueryRequest();
    // // query.setQuery("id:" + id);
    // //
    // // QueryResponse result = null;
    // // try {
    // // result = searchService.query(query);
    // // } catch (TerminatorServiceException e) {
    // // }
    // // if (result.getResults().getNumFound() >= 1) {
    // // count++;
    // // long time = System.currentTimeMillis();
    // // consume.put(id, time);
    // // // totalTime += (time - startTime);
    // // // requestTime++;
    // // break;
    // // }
    // // }
    // // id++;
    // // if (count >= addCount)
    // // break;
    // // }
    // //
    // // for (Entry<Long, Long> entry : consume.entrySet()) {
    // // Long key = entry.getKey();
    // // Long startTime = timeConsume.get(key);
    // // Long endTime = entry.getValue();
    // // totalTime += (endTime - startTime);
    // // requestTime++;
    // // }
    // //
    // // System.out.println("�ܻ���ʱ��:" + ((totalTime + 0.0)));
    // // System.out.println("�ܲ�ѯ����:" + requestTime);
    // // System.out.println("�������ܲ鵽��ƽ��ʱ����:"
    // // + ((totalTime + 0.0) / requestTime));
    // // }
    // //
    // // }
    // 
    // public static void main(String[] args) throws Exception {
    // //
    // // File file = new File(args[0]);
    // //
    // // PropertyConfigurator.configure(file.toURL());
    // //
    // // HSFEasyStarter.start("D:/Data/hsf", "1.4.9.6");
    // //
    // // try {
    // // Thread.sleep(1000 * 2);
    // // } catch (InterruptedException e) {
    // // throw new RuntimeException(e);
    // // }
    // //
    // // ApplicationContext context = new ClassPathXmlApplicationContext(
    // // "classpath:yxbean.xml");
    // //
    // // SearchService searchService = (SearchService) context
    // // .getBean("search4yxIC");
    // //
    // // try {
    // // Thread.sleep(3000 * 2);
    // // } catch (InterruptedException e) {
    // // throw new RuntimeException(e);
    // // }
    // //
    // // int count = 0;
    // // int suc = 0;
    // // int error = 0;
    // //
    // // // List<String> lists = new ArrayList<String>();
    // Random random = new Random();
    // // int requestCount = 0;
    // // long totalTime = 0;
    // // Scanner cin = new Scanner(System.in);
    // // while (true) {
    // // try {
    // // // String line = cin.nextLine();
    // // String line =
    // // "{!Join from=ar_target_id to=aa_auction_id fromV=\"ar_status:5\"}";
    // // if (line.length() < 0)
    // // break;
    // // TerminatorQueryRequest query1 = new TerminatorQueryRequest();
    // // // Map<String, String> map = new HashMap<String, String>();
    // // // map.put("user_id", "647585155");
    // // // query1.addRouteValue(map);
    // // query1.setQuery(line);
    // // query1.setRows(100);
    // // query1.setStart(0);
    // // QueryResponse result1 = searchService.query(query1);
    // //
    // // long num = result1.getResults().getNumFound();
    // // for (SolrDocument doc : result1.getResults()) {
    // // Object list = doc.get("auction_extensions");
    // // if (null == list || ((List<SolrDocument>) list).size() != 6)
    // // System.out.println(doc.get("aa_auction_id"));
    // // else
    // // System.out.println(((List<SolrDocument>) list).size());
    // // }
    // // System.out.println(num);
    // // System.out.println("time cost:" + result1.getQTime());
    // // } catch (Exception e) {
    // // e.printStackTrace();
    // // }
    // // }
    // 
    // // AddData addData = new AddData();
    // // QueryThread run = new QueryThread(searchService);
    // // Thread tt = new Thread(addData);
    // // Thread t1 = new Thread(run);
    // // tt.start();
    // // t1.start();
    // 
    // ProtocolBufferEncoder encoder = new ProtocolBufferEncoder();
    // 
    // ProtocolBufferDecoder decoder = new ProtocolBufferDecoder();
    // 
    // List<String> handlers = new ArrayList<String>();
    // handlers.add("com.taobao.common.transport.protoc");
    // decoder.setBuilderMetaInfo(ProtocolBufferUtils
    // .createBuilderMetainfo(handlers));
    // 
    // DefaultMinaCodecFactory codecFactory = new DefaultMinaCodecFactory();
    // codecFactory.setDecoder(decoder);
    // codecFactory.setEncoder(encoder);
    // 
    // BlockingTCPConnector conn = new BlockingTCPConnector(1);
    // conn.setCodecFactory(codecFactory);
    // conn.setDestIp("10.232.20.77");
    // conn.setDestPort(7777);
    // conn.setStopSessionOnTimeout(true);
    // 
    // MessageSender sender = new MessageSender();
    // sender.setConnetor(conn);
    // sender.start();
    // long max = Integer.MIN_VALUE;
    // long min = Integer.MAX_VALUE;
    // int[] dis = new int[10];
    // for (int i = 0; i < 10; i++)
    // dis[i] = 0;
    // while (true) {
    // 
    // Map<String, Object> map = new HashMap<String, Object>();
    // 
    // map.put("id", RandomString(20 + random.nextInt(20)));
    // map.put("province", RandomString(10 + random.nextInt(20)));
    // map.put("city", RandomString(10 + random.nextInt(20)));
    // map.put("district", RandomString(30 + random.nextInt(20)));
    // map.put("town_name", RandomString(10 + random.nextInt(20)));
    // map.put("town_code", RandomString(20 + random.nextInt(20)));
    // map.put("poi_name", RandomString(10 + random.nextInt(20)));
    // map.put("geohash", RandomString(10 + random.nextInt(20)));
    // 
    // Document.Builder doc = Document.newBuilder().setBoost(1.0f);
    // for (Entry<String, Object> entry : map.entrySet()) {
    // if (entry.getValue() instanceof Long) {
    // doc.addFields(InputFields.newBuilder()
    // .setKey(entry.getKey())
    // .addStrField(entry.getValue().toString())
    // .setType(Type.LONG).build());
    // } else if (entry.getValue() instanceof Integer) {
    // doc.addFields(InputFields.newBuilder()
    // .setKey(entry.getKey())
    // .addStrField(entry.getValue().toString())
    // .setType(Type.INTEGER).build());
    // } else if (entry.getValue() instanceof String) {
    // doc.addFields(InputFields.newBuilder()
    // .setKey(entry.getKey())
    // .addStrField(entry.getValue().toString())
    // .setType(Type.STRING).build());
    // }
    // }
    // 
    // try {
    // long startTime = System.currentTimeMillis();
    // AddDocumentResponse object = (AddDocumentResponse) sender
    // .sendAndWait(AddDocumentRequest.newBuilder()
    // .setSolrDoc(doc.build()).build(), 10,
    // TimeUnit.SECONDS);
    // 
    // // if (object.getErrorCode() == 200) {
    // // suc++;
    // // while (true) {
    // // TerminatorQueryRequest query = new TerminatorQueryRequest();
    // // query.setQuery("id:" + map.get("id"));
    // // query.setRows(20);
    // // query.setStart(0);
    // // QueryResponse result = searchService.query(query);
    // // if (result.getResults().getNumFound() >= 1) {
    // // long cost = (System.currentTimeMillis() - startTime);
    // // if (cost > max)
    // // max = cost;
    // // if (cost < min)
    // // min = cost;
    // // int index = Long.valueOf(cost / 100).intValue();
    // // if (index > 9)
    // // index = 9;
    // // dis[index]++;
    // // totalTime += cost;
    // // requestCount++;
    // // break;
    // // }
    // // }
    // // } else
    // // error++;
    // 
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // //
    // // count++;
    // //
    // // if (count % 100 == 0) {
    // // System.out.println("����һ�������" + count + "�����");
    // // System.out.println("�������ܲ鵽��ƽ��ʱ����:"
    // // + ((totalTime + 0.0) / requestCount) + ",�����Ҫ����ʱ����"
    // // + max + ",��С��Ҫ����ʱ��" + min);
    // //
    // // System.out.println("�����䣺");
    // // for (int i = 0; i < 10; i++) {
    // // System.out.println(i + "\t" + dis[i]);
    // // }
    // // }
    // //
    // // if (count >= addCount)
    // // break;
    // 
    // }
    // 
    // // System.out.println("�ܻ���ʱ��:" + ((totalTime + 0.0)));
    // // System.out.println("�ܲ�ѯ����:" + requestCount);
    // // System.out
    // // .println("�������ܲ鵽��ƽ��ʱ����:" + ((totalTime + 0.0) / requestCount));
    // }
    // 
    // // public static String randomDelimited(int index) {
    // // String str = "";
    // // boolean flag = false;
    // // for (int i = 0; i < index; i++) {
    // // if (flag)
    // // str += ",";
    // // str += RandomString(7);
    // // flag = true;
    // // }
    // //
    // // return str;
    // //
    // // }
    // //
    // public static String RandomString(int length) {
    // String str =
    // "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    // Random random = new Random();
    // StringBuffer buf = new StringBuffer();
    // for (int i = 0; i < length; i++) {
    // int num = random.nextInt(62);
    // buf.append(str.charAt(num));
    // }
    // return buf.toString();
    // }
}
