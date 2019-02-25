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
package com.qlangtech.tis.hdfs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FilterFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.util.Progressable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TISHdfsUtils {

	private static final Logger logger = LoggerFactory.getLogger(TISHdfsUtils.class);

	public static void main(String[] args) throws Exception {
	}

	/**
	 * @param commandLine
	 * @param dest
	 * @param runtime
	 * @return
	 */
	public static List<Path> getLibPaths(final String localJarDir,
			Path dest) /* hdfs 目标目录 */
	{
		try {
			// String localJarDir = null;
			// if (commandLine != null) {
			// localJarDir =
			// commandLine.getOptionValue(YarnConstant.PARAM_OPTION_LOCAL_JAR_DIR);
			// }
			List<Path> libs = null;
			if (StringUtils.isNotBlank(localJarDir)) {
				libs = copyLibs2Hdfs(localJarDir, dest);
			} else {
				libs = new ArrayList<Path>();
				if (!getFileSystem().exists(dest)) {
					throw new IllegalStateException("target dest:" + dest
							+ " is not exist ,please make sure having deploy the index build jar to hdfs.");
				}
				for (FileStatus s : getFileSystem().listStatus(dest)) {
					libs.add(s.getPath());
				}
			}
			if (libs.size() < 1) {
				throw new IllegalStateException("libs size can not small than 1");
			}
			return libs;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将本地jar包上传到hdfs上去
	 *
	 * @param localJarDir
	 * @param runtime
	 * @return
	 * @throws Exception
	 */
	public static List<Path> copyLibs2Hdfs(String localJarDir, Path dest)
			throws /* hdfs 目标目录 */
			Exception {
		List<Path> libs = new ArrayList<Path>();
		if (StringUtils.isBlank(localJarDir)) {
			throw new IllegalArgumentException("param localJarDir can not be null");
		}
		// 本地删除
		FileSystem fs = getFileSystem();
		// final Path path = new Path(YarnConstant.HDFS_GROUP_LIB_DIR + "/" +
		// runtime.getKeyName());
		fs.delete(dest, true);
		logger.info("path:" + dest + " have been delete");
		// 取得需要的lib包
		File dir = new File(localJarDir);
		String[] childs = null;
		if (!dir.isDirectory() || (childs = dir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return StringUtils.endsWith(name, ".jar");
			}
		})).length < 1) {
			throw new IllegalStateException("dir:" + dir.getAbsolutePath() + " has not find any jars");
		}
		URI source = null;
		Path d = null;
		for (String f : childs) {
			source = (new File(dir, f)).toURI();
			d = new Path(dest, f);
			fs.copyFromLocalFile(new Path(source), d);
			libs.add(d);
			logger.info("local:" + source + " have been copy to hdfs");
		}
		return libs;
	}

	private static final Map<String, FileSystem> fileSys = new HashMap<String, FileSystem>();

	public static FileSystem getFileSystem() {
		TSearcherConfigFetcher config = TSearcherConfigFetcher.get();
		return getFileSystem(config.getHdfsAddress());
	}

	public static FileSystem getFileSystem(String hdfsAddress) {
		try {
			FileSystem fileSystem = fileSys.get(hdfsAddress);
			if (fileSystem == null) {
				synchronized (TISHdfsUtils.class) {
					fileSystem = fileSys.get(hdfsAddress);
					if (fileSystem == null) {
						Configuration conf = new Configuration();
						conf.set(FsPermission.UMASK_LABEL, "000");
						TSearcherConfigFetcher config = TSearcherConfigFetcher.get();
						conf.set("fs.default.name", config.getHdfsAddress());
						conf.set("hadoop.job.ugi", "admin");

						RunEnvironment runtime = RunEnvironment.getSysRuntime();
						logger.info("run environment:" + runtime);

						URL url = TISHdfsUtils.class.getResource("/tis-web-config/hdfs-site.xml");
						conf.addResource(url);
						logger.info("add hdfs:" + config.getHdfsAddress() + " resource:" + url);
						
						
//						if (runtime == RunEnvironment.ONLINE) {
//							URL url = TISHdfsUtils.class.getResource("/online-hdfs-site.xml");
//							conf.addResource(url);
//							logger.info("add hdfs:" + config.getHdfsAddress() + " resource:" + url);
//						} else {
//							URL url = TISHdfsUtils.class.getResource("/daily-hdfs-site.xml");
//							conf.addResource(url);
//							logger.info("add hdfs:" + config.getHdfsAddress() + " resource:" + url);
//						}

						conf.setBoolean("fs.hdfs.impl.disable.cache", true);
						fileSystem = new FilterFileSystem(FileSystem.get(conf)) {

							@Override
							public boolean delete(Path f, boolean recursive) throws IOException {
								try {
									return super.delete(f, recursive);
								} catch (Exception e) {
									throw new RuntimeException("path:" + f, e);
								}
							}

							@Override
							public boolean mkdirs(Path f, FsPermission permission) throws IOException {
								return super.mkdirs(f, FsPermission.getDirDefault());
							}

							@Override
							public FSDataOutputStream create(Path f, FsPermission permission, boolean overwrite,
									int bufferSize, short replication, long blockSize, Progressable progress)
									throws IOException {
								return super.create(f, FsPermission.getDefault(), overwrite, bufferSize, replication,
										blockSize, progress);
							}

							@Override
							public FileStatus[] listStatus(Path f) throws IOException {
								try {
									return super.listStatus(f);
								} catch (Exception e) {
									throw new RuntimeException("path:" + f, e);
								}
							}

							@Override
							public void close() throws IOException {
								// super.close();
								// 设置不被关掉
							}
						};
						fileSystem.listStatus(new Path("/"));
						fileSys.put(hdfsAddress, fileSystem);
					}
				}
			}
			return fileSystem;
		} catch (IOException e) {
			throw new RuntimeException("hdfsAddress:" + TSearcherConfigFetcher.get().getHdfsAddress(), e);
		}
	}
}
