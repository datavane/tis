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
package com.qlangtech.tis.hdfs;

import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fs.ITISFileSystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年6月17日
 */
public class TISHdfsUtils {

    private static final Logger logger = LoggerFactory.getLogger(TISHdfsUtils.class);

    public static void main(String[] args) throws Exception {
    // FileSystem fs = getFileSystem();
    // FileStatus[] childs = fs.listStatus(new Path("/"));
    // for (FileStatus c : childs) {
    // System.out.println(c.getPath().toString());
    // }
    }
    // public static List<Path> getLibPaths(final String localJarDir, Path dest /* hdfs 目标目录 */) {
    // try {
    // // String localJarDir = null;
    // // if (commandLine != null) {
    // // localJarDir =
    // // commandLine.getOptionValue(YarnConstant.PARAM_OPTION_LOCAL_JAR_DIR);
    // // }
    // 
    // List<Path> libs = null;
    // if (StringUtils.isNotBlank(localJarDir)) {
    // libs = copyLibs2Hdfs(localJarDir, dest);
    // } else {
    // libs = new ArrayList<Path>();
    // // new Path(YarnConstant.HDFS_GROUP_LIB_DIR + "/" +
    // // runtime.getKeyName())
    // 
    // if (!getFileSystem().exists(dest)) {
    // throw new IllegalStateException("target dest:" + dest
    // + " is not exist ,please make sure having deploy the index build jar to hdfs.");
    // }
    // 
    // for (FileStatus s : getFileSystem().listStatus(dest)) {
    // libs.add(s.getPath());
    // }
    // }
    // if (libs.size() < 1) {
    // throw new IllegalStateException("libs size can not small than 1");
    // }
    // return libs;
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    // 
    // /**
    // * 将本地jar包上传到hdfs上去
    // *
    // * @param localJarDir
    // * @param runtime
    // * @return
    // * @throws Exception
    // */
    // public static List<Path> copyLibs2Hdfs(String localJarDir, Path dest /* hdfs 目标目录 */ ) throws Exception {
    // List<Path> libs = new ArrayList<Path>();
    // if (StringUtils.isBlank(localJarDir)) {
    // throw new IllegalArgumentException("param localJarDir can not be null");
    // }
    // 
    // // 本地删除
    // FileSystem fs = getFileSystem();
    // // final Path path = new Path(YarnConstant.HDFS_GROUP_LIB_DIR + "/" +
    // // runtime.getKeyName());
    // fs.delete(dest, true);
    // logger.info("path:" + dest + " have been delete");
    // 
    // // 取得需要的lib包
    // File dir = new File(localJarDir);
    // String[] childs = null;
    // if (!dir.isDirectory() || (childs = dir.list(new FilenameFilter() {
    // @Override
    // public boolean accept(File dir, String name) {
    // return StringUtils.endsWith(name, ".jar");
    // }
    // })).length < 1) {
    // throw new IllegalStateException("dir:" + dir.getAbsolutePath() + " has not find any jars");
    // }
    // 
    // URI source = null;
    // Path d = null;
    // for (String f : childs) {
    // source = (new File(dir, f)).toURI();
    // d = new Path(dest, f);
    // fs.copyFromLocalFile(new Path(source), d);
    // libs.add(d);
    // logger.info("local:" + source + " have been copy to hdfs");
    // }
    // 
    // return libs;
    // }
    // 
    // private static final Map<String, FileSystem> fileSys = new HashMap<String, FileSystem>();
    // public static ITISFileSystemFactory getFileSystem() {
    // // return hdfsFileSystemFactory.getFileSystem();
    // throw new UnsupportedOperationException();
    // }
    // public static FileSystem getFileSystem(String hdfsAddress) {
    // try {
    // 
    // FileSystem fileSystem = fileSys.get(hdfsAddress);
    // if (fileSystem == null) {
    // synchronized (TISHdfsUtils.class) {
    // fileSystem = fileSys.get(hdfsAddress);
    // if (fileSystem == null) {
    // Configuration conf = new Configuration();
    // conf.set(FsPermission.UMASK_LABEL, "000");
    // TSearcherConfigFetcher config = TSearcherConfigFetcher.get();
    // 
    // // fs.defaultFS
    // conf.set(FileSystem.FS_DEFAULT_NAME_KEY, config.getHdfsAddress());
    // conf.set("fs.default.name", config.getHdfsAddress());
    // 
    // conf.set("hadoop.job.ugi", "admin");
    // logger.info("run environment:" + config.getRunEnvironment());
    // // if
    // // (RunEnvironment.getEnum(config.getRunEnvironment())
    // // != RunEnvironment.DAILY) {
    // // URL url =
    // // TISHdfsUtils.class.getResource("/online-hdfs-site.xml");
    // // conf.addResource(url);
    // // logger.info("add hdfs:" + config.getHdfsAddress() + "
    // // resource:" + url);
    // // }
    // 
    // if (RunEnvironment.getEnum(config.getRunEnvironment()) == RunEnvironment.ONLINE) {
    // URL url = TISHdfsUtils.class.getResource("/online-hdfs-site.xml");
    // conf.addResource(url);
    // logger.info("add hdfs:" + config.getHdfsAddress() + " resource:" + url);
    // } else {
    // URL url = TISHdfsUtils.class.getResource("/daily-hdfs-site.xml");
    // conf.addResource(url);
    // logger.info("add hdfs:" + config.getHdfsAddress() + " resource:" + url);
    // }
    // 
    // // conf.set("dfs.nameservices", "cluster-cdh");
    // // conf.set("dfs.client.failover.proxy.provider.cluster-cdh",
    // // "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
    // // //////////////
    // // conf.set("dfs.ha.automatic-failover.enabled.cluster-cdh",
    // // "true");
    // // conf.set("dfs.ha.automatic-failover.enabled.cluster-cdh",
    // // "true");
    // conf.setBoolean("fs.hdfs.impl.disable.cache", true);
    // fileSystem = new FilterFileSystem(FileSystem.get(conf)) {
    // @Override
    // public boolean delete(Path f, boolean recursive) throws IOException {
    // try {
    // return super.delete(f, recursive);
    // } catch (Exception e) {
    // throw new RuntimeException("path:" + f, e);
    // }
    // }
    // 
    // @Override
    // public boolean mkdirs(Path f, FsPermission permission) throws IOException {
    // return super.mkdirs(f, FsPermission.getDirDefault());
    // }
    // 
    // @Override
    // public FSDataOutputStream create(Path f, FsPermission permission, boolean overwrite,
    // int bufferSize, short replication, long blockSize, Progressable progress)
    // throws IOException {
    // return super.create(f, FsPermission.getDefault(), overwrite, bufferSize, replication,
    // blockSize, progress);
    // }
    // 
    // @Override
    // public FileStatus[] listStatus(Path f) throws IOException {
    // try {
    // return super.listStatus(f);
    // } catch (Exception e) {
    // throw new RuntimeException("path:" + f, e);
    // }
    // }
    // 
    // @Override
    // public void close() throws IOException {
    // // super.close();
    // // 设置不被关掉
    // }
    // };
    // fileSystem.listStatus(new Path("/"));
    // fileSys.put(hdfsAddress, fileSystem);
    // }
    // }
    // }
    // 
    // return fileSystem;
    // } catch (IOException e) {
    // throw new RuntimeException("hdfsAddress:" + TSearcherConfigFetcher.get().getHdfsAddress(), e);
    // }
    // }
}
