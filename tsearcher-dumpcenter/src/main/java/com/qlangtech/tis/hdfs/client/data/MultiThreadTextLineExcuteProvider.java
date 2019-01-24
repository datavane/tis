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
package com.qlangtech.tis.hdfs.client.data;

/*
 * @description  本地文件数据源情况，
 * @since  2011-9-23 下午11:35:55
 * @version  1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MultiThreadTextLineExcuteProvider {
    // implements SourceDataProvider, ApplicationContextAware, Configurable
    // protected static Log logger = LogFactory.getLog(MultiThreadTextLineExcuteProvider.class);
    // private long minSplitSize = 1;
    // 
    // /**
    // * @return
    // * @uml.property  name="processFilePath"
    // */
    // public String getProcessFilePath() {
    // return processFilePath;
    // }
    // 
    // /**
    // * @param processFilePath
    // * @uml.property  name="processFilePath"
    // */
    // public void setProcessFilePath(String processFilePath) {
    // this.processFilePath = processFilePath;
    // }
    // 
    // private int numSplits = 1;
    // /**
    // * @uml.property  name="processFilePath"
    // */
    // private String processFilePath;
    // /**
    // * @uml.property  name="excuteFileName"
    // */
    // private List excuteFileName;
    // private static LocalFileSystem fileSystem;
    // /**
    // * @uml.property  name="context"
    // * @uml.associationEnd
    // */
    // private TSearcherContext context;
    // private String serviceName;
    // private String fileTitle;
    // //private boolean haveTitle = true;
    // 
    // /**
    // * @param fileTitle
    // * @uml.property  name="fileTitle"
    // */
    // public void setFileTitle(String fileTitle) {
    // this.fileTitle = fileTitle;
    // }
    // 
    // /**
    // * @uml.property  name="tab"
    // */
    // private String tab = "\t";
    // /**
    // * @uml.property  name="eof"
    // */
    // private String eof = "\n";
    // 
    // /**
    // * @return
    // * @uml.property  name="excuteFileName"
    // */
    // public List getExcuteFileName() {
    // return excuteFileName;
    // }
    // 
    // //	public boolean isHaveTitle() {
    // //		return haveTitle;
    // //	}
    // //
    // //	public void setHaveTitle(boolean haveTitle) {
    // //		this.haveTitle = haveTitle;
    // //	}
    // 
    // /**
    // * @return
    // * @uml.property  name="tab"
    // */
    // public String getTab() {
    // return tab;
    // }
    // 
    // /**
    // * @return
    // * @uml.property  name="eof"
    // */
    // public String getEof() {
    // return eof;
    // }
    // 
    // /**
    // * @param excuteFileName
    // * @uml.property  name="excuteFileName"
    // */
    // public void setExcuteFileName(List excuteFileName) {
    // this.excuteFileName = excuteFileName;
    // }
    // 
    // private static final double SPLIT_SLOP = 1.1; // 10% slop
    // 
    // List spliList = null;
    // 
    // /**
    // * @param applicationContext
    // * @throws BeansException
    // */
    // @Override
    // public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    // // TODO Auto-generated method stub
    // 
    // }
    // 
    // /**
    // * @param tab
    // * @uml.property  name="tab"
    // */
    // public void setTab(String tab) {
    // this.tab = tab;
    // }
    // 
    // /**
    // * @param eof
    // * @uml.property  name="eof"
    // */
    // public void setEof(String eof) {
    // this.eof = eof;
    // }
    // 
    // /**
    // * @throws SourceDataReadException
    // */
    // @Override
    // public void closeResource() throws SourceDataReadException {
    // 
    // }
    // 
    // /**
    // * @return
    // */
    // @Override
    // public List<SourceDataProvider> getSplit() {
    // 
    // return spliList;
    // }
    // 
    // /**
    // * @return
    // * @throws SourceDataReadException
    // */
    // @Override
    // public boolean hasNext() throws SourceDataReadException {
    // // TODO Auto-generated method stub
    // return false;
    // }
    // 
    // /**
    // * @return
    // * @throws SourceDataReadException
    // */
    // @Override
    // public Map next() throws SourceDataReadException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    // 
    // /**
    // * @throws SourceDataReadException
    // */
    // @Override
    // public void init() throws SourceDataReadException {
    // try {
    // fileSystem = FileSystem.getLocal(context.getConfiguration());
    // // fileSystem.getFileStatus(new Path(processFilePath));
    // PathFilter defaultFilter = new PathFilter() {
    // @Override
    // public boolean accept(Path path) {
    // String name = path.getName();
    // 
    // if (excuteFileName.contains(name))
    // return true;
    // else {
    // return false;
    // }
    // }
    // 
    // };
    // FileStatus[] 	filestatus = fileSystem.listStatus(new Path(processFilePath), defaultFilter);
    // 
    // ArrayList<FileStatus> rList = new ArrayList<FileStatus>();
    // for (FileStatus status : filestatus) {
    // if (status.isDir()) {
    // continue;
    // }
    // rList.add(status);
    // }
    // if(rList.size()==0){
    // throw new IOException("【注意】设置待消费的文件不存在，请严格检查配置文件！");
    // }
    // if(fileTitle==null){
    // throw new NullPointerException("【注意】请配置【<property name=\"fileTitle\" value=\"A,B,C,D,E,F\" />】的Title属性！！！");
    // }
    // } catch (IOException e) {
    // logger.error("【注意】初始化文本数据源Provider出现异常", e);
    // throw new SourceDataReadException(e);
    // }
    // 
    // }
    // 
    // /**
    // * @throws SourceDataReadException
    // */
    // @Override
    // public void openResource() throws SourceDataReadException {
    // FileStatus[] filestatus = null;
    // try {
    // //fileSystem.getFileStatus(new Path(processFilePath));
    // 
    // PathFilter defaultFilter = new PathFilter() {
    // @Override
    // public boolean accept(Path path) {
    // String name = path.getName();
    // 
    // if (excuteFileName.contains(name))
    // return true;
    // else {
    // return false;
    // }
    // }
    // 
    // };
    // filestatus = fileSystem.listStatus(new Path(processFilePath), defaultFilter);
    // 
    // ArrayList<FileStatus> rList = new ArrayList<FileStatus>();
    // for (FileStatus status : filestatus) {
    // if (status.isDir()) {
    // continue;
    // }
    // rList.add(status);
    // }
    // FileStatus[] fileSplit = new FileStatus[rList.size()];
    // InputSplit[] splits = this.getSplits(rList.toArray(fileSplit), context.getNumSplits(), context);
    // spliList = new ArrayList<SourceDataProvider>();
    // logger.warn("【注意】需要处理的文件被切分为[" + splits.length+"]份");
    // for (final InputSplit split : splits) {
    // SourceDataProvider provider = new SourceDataProvider() {
    // private TextLineRecordReader reader = null;
    // private Map map = null;
    // 
    // @Override
    // public void closeResource() throws SourceDataReadException {
    // if (reader != null)
    // try {
    // reader.close();
    // reader = null;
    // } catch (IOException e) {
    // logger.error("【警告】读入原始数据错误", e);
    // throw new SourceDataReadException("【警告】读入原始数据错误", e);
    // 
    // } finally {
    // reader = null;
    // }
    // 
    // }
    // 
    // @Override
    // public List getSplit() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    // 
    // @Override
    // public boolean hasNext() throws SourceDataReadException {
    // map = new LinkedHashMap();
    // try {
    // boolean flag = reader.next(map);
    // if(flag&&map.size()==0){
    // flag = hasNext();
    // }
    // return flag;
    // } catch (Exception e) {
    // logger.error("【警告】读入原始数据错误>>>>>>>", e);
    // logger.warn("数据错误很可能是格式有问题过滤掉此条数据，继续处理后续数据");
    // return true;
    // }
    // }
    // 
    // @Override
    // public void init() throws SourceDataReadException {
    // // TODO Auto-generated method stub
    // 
    // }
    // 
    // @Override
    // public Map next() throws SourceDataReadException {
    // // TODO Auto-generated method stub
    // return map;
    // }
    // 
    // @Override
    // public void openResource() throws SourceDataReadException {
    // 
    // try {
    // //							if (haveTitle) {
    // //								reader = new TextLineRecordReader(fileSystem, (FileSplit) split, tab.charAt(0), eof
    // //										.charAt(0));
    // //							} else
    // reader = new TextLineRecordReader(fileSystem, (FileSplit) split, fileTitle, tab
    // .charAt(0), eof.charAt(0));
    // } catch (IOException e) {
    // logger.error("【警告】读入原始数据错误", e);
    // throw new SourceDataReadException("【警告】读入原始数据错误", e);
    // }
    // 
    // }
    // };
    // spliList.add(provider);
    // }
    // 
    // } catch (IOException e) {
    // logger.error("【注意】打开本地文件系统出现IO异常", e);
    // throw new SourceDataReadException("【注意】打开本地文件系统出现IO异常");
    // } catch (Exception e) {
    // logger.error("【注意】打开本地文件系统出现未知异常", e);
    // throw new SourceDataReadException("【注意】打开本地文件系统出现未知异常");
    // }
    // 
    // }
    // 
    // public InputSplit[] getSplits(FileStatus[] files, int numSplits, TSearcherContext context) throws Exception {
    // long totalSize = 0; // compute total size
    // for (FileStatus file : files) { // check we have valid files
    // if (file.isDir()) {
    // continue;
    // }
    // totalSize += file.getLen();//
    // }
    // logger.warn("[totalSize]==>" + totalSize);
    // logger.warn("[numSplits]==>" + numSplits);
    // long goalSize = totalSize / (numSplits == 0 ? 1 : numSplits);
    // logger.warn("[goalSize]==>" + goalSize);
    // long minSize = Math.max(context.getNumSplits(), minSplitSize);
    // logger.warn("[minSize]==>" + minSize);
    // // generate splits
    // ArrayList<FileSplit> splits = new ArrayList<FileSplit>(numSplits);
    // //NetworkTopology clusterMap = new NetworkTopology();//
    // for (FileStatus file : files) {
    // Path path = file.getPath();
    // long length = file.getLen();
    // 
    // BlockLocation[] blkLocations = fileSystem.getFileBlockLocations(file, 0, length);
    // if ((length != 0) && isSplitable(fileSystem, path)) {
    // long blockSize = file.getBlockSize();//
    // logger.warn("[blockSize]==>" + blockSize);
    // 
    // long splitSize = computeSplitSize(goalSize, minSize, blockSize);
    // logger.warn("[splitSize]==>" + splitSize);
    // //
    // long bytesRemaining = length;
    // while (((double) bytesRemaining) / splitSize > SPLIT_SLOP) {
    // // String[] splitHosts = getSplitHosts(blkLocations, length
    // // - bytesRemaining, splitSize, clusterMap);
    // splits.add(new FileSplit(path, length - bytesRemaining, splitSize, new String[0]));
    // bytesRemaining -= splitSize;
    // }
    // 
    // if (bytesRemaining != 0) {
    // splits.add(new FileSplit(path, length - bytesRemaining, bytesRemaining,
    // blkLocations[blkLocations.length - 1].getHosts()));
    // }
    // } else if (length != 0) {
    // // String[] splitHosts = getSplitHosts(blkLocations, 0, length,
    // // clusterMap);
    // splits.add(new FileSplit(path, 0, length, new String[0]));
    // } else {
    // 
    // splits.add(new FileSplit(path, 0, length, new String[0]));
    // }
    // }
    // int size = splits.size();
    // logger.warn("[" + serviceName + "] 需要导入[" + totalSize / 1024 / 1024 + "]MB的源数据到HDFS，切分数据为: " + size + " 份");
    // 
    // FileSplit[] fileSplit = new FileSplit[size];
    // return splits.toArray(fileSplit);
    // }
    // 
    // protected long computeSplitSize(long goalSize, long minSize, long blockSize) {
    // return Math.max(minSize, Math.max(goalSize, blockSize));
    // }
    // 
    // /**
    // *
    // * @param fs
    // * @param filename
    // * @return
    // */
    // protected boolean isSplitable(FileSystem fs, Path filename) {
    // return true;
    // }
    // 
    // /**
    // * @return
    // */
    // @Override
    // public TSearcherContext getConfig() {
    // // TODO Auto-generated method stub
    // return context;
    // }
    // 
    // /**
    // * @param context
    // */
    // @Override
    // public void setConfig(TSearcherContext context) {
    // this.context = context;
    // this.serviceName = context.getServiceName();
    // }
}
