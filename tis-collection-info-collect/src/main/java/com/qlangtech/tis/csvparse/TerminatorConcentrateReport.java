/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.csvparse;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TerminatorConcentrateReport {
    // // private final BufferedWriter reportWriter;
    //
    // private final Set<HistoryReport> historyReport = new HashSet<HistoryReport>();
    //
    // private static final Pattern ipFilePattern = Pattern
    // .compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\.txt");
    //
    // private static final Pattern serverPattern = Pattern
    // .compile("used:(\\d+),available:(\\d+),use:(\\d+)%");
    //
    // private static final Pattern corePattern = Pattern
    // .compile("IP:(.+?)corename:(.+?)\\-(\\d{cd}),indexsize:(.+?),indexNum:(\\d*),timesum:(\\d*),querycount:(\\d*)");
    //
    // protected TerminatorConcentrateReport(Date parseDate) {
    // super(parseDate);
    //
    // // this.parseDate = parseDate;
    // // this.historyReport = new HistoryReport(this.parseDate);
    //
    // Calendar calender = Calendar.getInstance();
    // calender.setTime(this.getParseDate());
    //
    // for (int i = 0; i < 7; i++) {
    // calender.add(Calendar.DAY_OF_YEAR, -(1));
    // System.out.println(dateFormat.format(calender.getTime()));
    // this.historyReport.add(new HistoryReport(calender.getTime()));
    // }
    //
    // }
    //
    // public int getQueryCountAverage(String coreName) {
    // return (int) getAverageValue(coreName, new PropGetter() {
    // @Override
    // public float get(Core core) {
    // return core.getHistoryQueryCount();
    // }
    // });
    // }
    //
    // public float getHistoryAverageCpuLoad(String coreName) {
    // return getAverageValue(coreName, new PropGetter() {
    // @Override
    // public float get(Core core) {
    // return core.getHistoryCupLoad();
    // }
    // });
    // }
    //
    // public float getHistoryAverageIndexCount(String coreName) {
    // return getAverageValue(coreName, new PropGetter() {
    // @Override
    // public float get(Core core) {
    // return core.getHistoryIndexCount();
    // }
    // });
    // }
    //
    // public float getQueryTimeAverage(String coreName) {
    // return getAverageValue(coreName, new PropGetter() {
    // @Override
    // public float get(Core core) {
    // return core.getHistoryQueryConsumeTime();
    // }
    // });
    // }
    //
    // private float getAverageValue(String coreName, PropGetter get) {
    // // float sum = 0;
    // // int count = 0;
    // // Core core = null;
    // // for (HistoryReport report : historyReport) {
    // // if ((core = report.getHistoryData().get(coreName)) == null) {
    // // // throw new IllegalStateException("coreName:" + coreName
    // // // + " can not find in "
    // // // + report.getHistoryFile().getAbsolutePath());
    // // continue;
    // //
    // // }
    // //
    // // sum += get.get(core);
    // // count++;
    // // }
    // //
    // // if (count < 1) {
    // // return 0;
    // // }
    // //
    // // return sum / count;
    // return 0;
    // }
    //
    // private interface PropGetter {
    // float get(Core core);
    // }
    //
    // private Map<String, Core> parseReportFromPhrase1() {
    //
    // final Map<String, Core> coreMap = new HashMap<String, Core>();
    //
    // File parent = new File(getWorkDir(), "data-" + this.getFormatDate());
    //
    // System.out.println("server data dir:" + parent.getAbsolutePath());
    //
    // String ipAddress = null;
    //
    // for (String fileName : parent.list()) {
    // ipAddress = getIpAddress(fileName);
    // if (ipAddress == null) {
    // continue;
    // }
    //
    // parseIpFile(coreMap, new File(parent, fileName), ipAddress);
    //
    // }
    //
    // return coreMap;
    // }
    //
    // // private Map<String, Core> parseReportFromCenterNode() {
    // //
    // // }
    //
    // public static String getWorkDir() {
    // return getDefaultValue("serverData",
    // "/home/baisui/terminator_index_report");
    // }
    //
    // private List<IPStatsInfo> parseServerPerspectView(Collection<Core> cores) {
    //
    // final List<IPStatsInfo> infoList = new LinkedList<IPStatsInfo>();
    // for (Core core : cores) {
    // infoList.addAll(core.getIpDimeStatsInfo());
    // }
    // Collections.sort(infoList, new Comparator<IPStatsInfo>() {
    // @Override
    // public int compare(IPStatsInfo info1, IPStatsInfo info2) {
    // return info1.getIpAddress().compareTo(info2.getIpAddress());
    // }
    //
    // });
    // return infoList;
    // }
    //
    // // public static final Pattern HostPattern =
    // // Pattern.compile("^\\w+\\.\\w+");
    //
    // private void parseIpFile(Map<String, Core> coreMap, File ipfile,
    // String ipAddress) {
    // BufferedReader reader = null;
    // Matcher m = null;
    // try {
    //
    // float maxload5Min = 0;
    //
    // InetAddress[] address = InetAddress.getAllByName(ipAddress);
    // try {
    // for (InetAddress ip : address) {
    //
    // // m = HostPattern.matcher(ip.getHostName());
    // maxload5Min = parseMaxLoad(ip);
    // // if (m.find()) {
    // //
    // // } else {
    // // throw new RuntimeException("ip.getHostName():"
    // // + ip.getHostName() + " is illegal");
    // // }
    //
    // }
    // } catch (Throwable e1) {
    // e1.printStackTrace();
    // }
    //
    // reader = new BufferedReader(new InputStreamReader(
    // new FileInputStream(ipfile)));
    //
    // // used:5527072,available:43943280,use:12%
    //
    // m = serverPattern
    // .matcher(StringUtils.trimToEmpty(reader.readLine()));
    // int used, available, use;
    // if (m.matches()) {
    // used = Integer.parseInt(m.group(1));
    // available = Integer.parseInt(m.group(2));
    // use = Integer.parseInt(m.group(3));
    // } else {
    // return;
    // }
    // String line = null;
    // Core core = null;
    // IPStatsInfo statsInfo = null;
    // // IP: 172.23.174.146
    // // corename:search4matrixtry-1,indexsize:906248,indexNum:1448264,querycount:387917
    // while ((line = reader.readLine()) != null) {
    // m = corePattern.matcher(line);
    // if (!m.find()) {
    // continue;
    // }
    //
    // String coreName = m.group(2);
    // core = coreMap.get(coreName);
    // if (core == null) {
    // core = new Core(coreName);
    // coreMap.put(coreName, core);
    // }
    //
    // final String ipaddress = StringUtils.trim(m.group(1));
    //
    // statsInfo = core.getIPStatsInfo(ipaddress);
    // if (statsInfo == null) {
    // statsInfo = new IPStatsInfo(ipaddress, coreName);
    //
    // statsInfo.setMaxLoad5Min(maxload5Min);
    // statsInfo.setAvailable(available);
    // statsInfo.setUsed(used);
    // statsInfo.setDate(this.getParseDate());
    // statsInfo.setUedPercent(String.valueOf(use) + "%");
    // core.add(statsInfo);
    // }
    //
    // CoreGroup group = new CoreGroup(Integer.parseInt(m.group(3)),
    // coreName);
    // try {
    // group.setIndexSize(Integer.parseInt(m.group(4)));
    // group.setIndexNum(Integer.parseInt(m.group(5)));
    // group.setQueryCount(Integer.parseInt(m.group(7)));
    // group.setQueryConsumeTime(Float.parseFloat(m.group(6)));
    // } catch (Throwable e) {
    // }
    //
    // statsInfo.addGroup(group);
    //
    // }
    //
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // try {
    // reader.close();
    // } catch (Throwable e) {
    // }
    // }
    // }
    //
    // private static final Pattern loadPairPattern = Pattern
    // .compile("\\{\"\\d\\d:\\d\\d:\\d\\d\":\"([\\d|\\.]{2,})\"\\}");
    //
    // private float parseMaxLoad(InetAddress ip) {
    //
    // if ("true".equalsIgnoreCase(System.getProperty("habojump"))) {
    // return 0l;
    // }
    //
    // final String hostName = ip.getHostName();
    //
    // try {
    //
    // URL url = new URL(
    // "http://172.24.142.103:8080/monitorapi/platformDetailData.do?host="
    // + hostName + "&key=load-min-5&date="
    // + this.getFormatDate() + "&module=device");
    //
    // System.out.println(url);
    //
    // return ConfigFileContext.processContent(url,
    //
    // new StreamProcess<Float>() {
    // @Override
    // public Float p(int status, InputStream stream, String md5) {
    //
    // // System.out
    // // .println();
    //
    // float max = 0;
    //
    // try {
    // String json = IOUtils.toString(stream);
    //
    // Matcher m = loadPairPattern.matcher(json);
    //
    // while (m.find()) {
    // if (max < Float.parseFloat(m.group(1))) {
    // max = Float.parseFloat(m.group(1));
    // }
    // }
    //
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    //
    // return max;
    //
    // }
    //
    // });
    // } catch (MalformedURLException e) {
    // throw new RuntimeException("host:" + hostName + " date:"
    // + this.getFormatDate(), e);
    // }
    //
    // }
    //
    // private String getIpAddress(String fileName) {
    //
    // Matcher matcher = ipFilePattern.matcher(fileName);
    //
    // if (matcher.matches()) {
    // return matcher.group(1);
    // }
    //
    // return null;
    // }
    //
    // public static String getDefaultValue(String key, String defaultValue) {
    // if ((System.getProperty(key) == null)) {
    // return defaultValue;
    // }
    //
    // return System.getProperty(key);
    // }
    //
    // public void startCreateReport() throws Exception {
    //
    // final Map<String, Core> phrase1result = this.parseReportFromPhrase1();
    //
    // for (String key : phrase1result.keySet()) {
    // System.out.print(key);
    // System.out.print(":");
    // System.out.print(phrase1result.get(key).getIpDesc());
    // System.out.println(phrase1result.get(key).getServerSum());
    // }
    //
    // // processExcel(
    // // Thread.currentThread().getClass()
    // // .getResourceAsStream("/core_report_template.xls"),
    // // new WokbookProcess() {
    // //
    // // @Override
    // // public void start(HSSFWorkbook workbook) throws Exception {
    // // HSSFRow row = null;
    // // HSSFSheet sheet = workbook.getSheetAt(0);
    // // // InputStreamReader coreNameReader = null;
    // // int rowIndex;
    // // try {
    // // // coreNameReader = new InputStreamReader(Thread
    // // // .currentThread().getClass().getResourceAsStream(
    // // // "/core_name.txt"));
    // // // LineIterator lineIterator = IOUtils
    // // // .lineIterator(coreNameReader);
    // // rowIndex = 2;
    // //
    // // for (Map.Entry<String, Core> entry : phrase1result
    // // .entrySet()) {
    // //
    // // String line = entry.getKey();
    // // Core core = entry.getValue();
    // //
    // // row = sheet.getRow(rowIndex++);
    // //
    // // if (row == null) {
    // // System.out.println("row is null row:"
    // // + rowIndex);
    // // }
    // // // if (core == null) {
    // // // continue;
    // // // }
    // //
    // // ExcelRow erow = new ExcelRow(row, core
    // // .getName());
    // //
    // // erow.setString(2, line);
    // //
    // // erow.setString(3, core.getIndexVolume() + "("
    // // + core.getServerSum() + "̨)");
    // //
    // // // cell = getCellValue(row, core.getName(), 5);
    // // // erow.getCell(5).setCellValue(core.getQueryCountFromPhrase2());
    // //
    // // erow.setDouble(5, core.getQueryCount());
    // //
    // // // cell = getCellValue(row, core.getName(),
    // // // 8);//
    // // // row.getCell(8);
    // //
    // // float atime = core.getAverageQueryTime();
    // //
    // // erow.setDouble(9, (atime > 0) ? atime : 0);
    // //
    // // erow.setDouble(13, core.getAverageLoad());
    // //
    // // erow.setDouble(4,
    // // getQueryCountAverage(core.getName()));
    // //
    // // erow.setDouble(8,
    // // getQueryTimeAverage(core.getName()));
    // //
    // // erow.setDouble(
    // // 12,
    // // getHistoryAverageCpuLoad(core.getName()));
    // //
    // // erow.setDouble(17, core.getIndexCount());
    // //
    // // erow.setDouble(16,
    // // getHistoryAverageIndexCount(core
    // // .getName()));
    // //
    // // }
    // //
    // // } finally {
    // // // try {
    // // // coreNameReader.close();
    // // // } catch (Throwable e) {
    // // //
    // // // }
    // // }
    // //
    // // createServerPerspectView(phrase1result, workbook);
    // //
    // // File newworkbook = createReportFileName(getParseDate());
    // //
    // // System.out.println("create file:"
    // // + newworkbook.getAbsolutePath());
    // //
    // // HSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
    // //
    // // FileOutputStream fileOut = new FileOutputStream(
    // // newworkbook);
    // //
    // // workbook.write(fileOut);
    // //
    // // fileOut.close();
    // //
    // // }
    // //
    // // });
    //
    // }
    //
    // private void createServerPerspectView(
    // final Map<String, Core> phrase1result, HSSFWorkbook workbook) {
    // HSSFRow row;
    // HSSFSheet sheet;
    // int rowIndex;
    // final int startRowIndex = 3;
    // List<IPStatsInfo> serverPerspectView = parseServerPerspectView(phrase1result
    // .values());
    // sheet = workbook.getSheetAt(1);
    //
    // // HSSFSheetConditionalFormatting formating = sheet
    // // .getSheetConditionalFormatting();
    // //
    // // HSSFConditionalFormattingRule rule1 = formating
    // // .createConditionalFormattingRule("VALUE($F4) > 0.3");
    // // // HSSFConditionalFormattingRule[] rule = { rule1 };
    // // HSSFPatternFormatting patternFmt1 = rule1.createPatternFormatting();
    // // patternFmt1.setFillBackgroundColor(HSSFColor.YELLOW.index);
    // // rule1.createFontFormatting().setFontColorIndex(HSSFColor.RED.index);
    // //
    // // CellRangeAddress[] address = new CellRangeAddress[] { new
    // // CellRangeAddress(
    // // startRowIndex, 100, 5, 5) };
    // // formating.addConditionalFormatting(address, rule1);
    //
    // sheet.createRow(0).createCell(0)
    // .setCellValue("һ��" + serverPerspectView.size() + "̨������");
    //
    // rowIndex = startRowIndex;
    // String ipAddress = null;
    // int startMergeRow = rowIndex;
    // for (IPStatsInfo sInfo : serverPerspectView) {
    // row = sheet.createRow(rowIndex++);
    // row.createCell(3).setCellValue(sInfo.getCoreName());
    // row.getCell(3).setCellStyle(createCellStyle(workbook));
    //
    // row.createCell(1).setCellValue(Core.formatVolume(sInfo.getUsed()));
    // row.getCell(1).setCellStyle(createCellStyle(workbook));
    //
    // row.createCell(2).setCellValue(sInfo.getUedPercent());
    // row.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
    // HSSFCellStyle style = createCellStyle(workbook);
    // style.setAlignment(CellStyle.ALIGN_RIGHT);
    // row.getCell(2).setCellStyle(style);
    //
    // row.createCell(0).setCellValue(sInfo.getIpAddress());
    // row.getCell(0).setCellStyle(createCellStyle(workbook));
    //
    // row.createCell(4).setCellValue(
    // Core.formatVolume(sInfo.getIndexSize()));
    // row.getCell(4).setCellStyle(createCellStyle(workbook));
    //
    // row.createCell(5).setCellValue(
    // (float) sInfo.getIndexSize() / sInfo.getUsed());
    // style = createCellStyle(workbook);
    // style.setDataFormat(HSSFDataFormat.getBuiltinFormat("0%"));
    // row.getCell(5).setCellStyle(style);
    // // row.getCell(3).setCellValue(sInfo.getCoreName());
    // // row.getCell(0).setCellValue(sInfo.getIpAddress());
    //
    // if (StringUtils.isNotEmpty(ipAddress)
    // && !StringUtils.equals(ipAddress, sInfo.getIpAddress())) {
    // sheet.addMergedRegion(new CellRangeAddress(startMergeRow, // first
    // // row
    // // (0-based)
    // row.getRowNum() - 1, // last row (0-based)
    // 0, // first column (0-based)
    // 0 // last column (0-based)
    // ));
    //
    // sheet.addMergedRegion(new CellRangeAddress(startMergeRow, // first
    // // row
    // // (0-based)
    // row.getRowNum() - 1, // last row (0-based)
    // 1, // first column (0-based)
    // 1 // last column (0-based)
    // ));
    // startMergeRow = row.getRowNum();
    // }
    //
    // ipAddress = sInfo.getIpAddress();
    // }
    //
    // }
    //
    // private HSSFCellStyle createCellStyle(HSSFWorkbook workbook) {
    //
    // // Style the cell with borders all around.
    // HSSFCellStyle style = workbook.createCellStyle();
    // style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
    // style.setBottomBorderColor(HSSFColor.BLACK.index);
    // style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
    // style.setLeftBorderColor(HSSFColor.BLACK.index);
    // style.setBorderRight(HSSFCellStyle.BORDER_THIN);
    // style.setRightBorderColor(HSSFColor.BLACK.index);
    // style.setBorderTop(HSSFCellStyle.BORDER_THIN);
    // style.setTopBorderColor(HSSFColor.BLACK.index);
    //
    // return style;
    // }
    //
    // /**
    // * @param args
    // */
    // public static void main(String[] args) throws Exception {
    //
    // if (args.length > 0 && "getips".equals(args[0])) {
    // GetAllServer2LocalFile.main(new String[0]);
    // return;
    // }
    //
    // Date parseDate = dateFormat.parse(getDefaultValue("date",
    // dateFormat.format(new Date())));
    //
    // TerminatorConcentrateReport terminatorConcentrateReport = new TerminatorConcentrateReport(
    // parseDate);
    // terminatorConcentrateReport.startCreateReport();
    //
    // }
}
