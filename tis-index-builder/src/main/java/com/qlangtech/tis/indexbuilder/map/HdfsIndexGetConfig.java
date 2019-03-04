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
package com.qlangtech.tis.indexbuilder.map;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.build.task.TaskReturn;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.hdfs.TISHdfsUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsIndexGetConfig implements TaskMapper {

	public static final Logger logger = LoggerFactory.getLogger(HdfsIndexGetConfig.class);

	FileSystem fs;

	long startTime;

	// 由consel传入的taskid
	private String taskid = "";

	public HdfsIndexGetConfig() throws IOException {
		startTime = System.currentTimeMillis();
		// getAllFileName();
		// indexSchema = new IndexSchema(new
		// SolrResourceLoader("",IndexConf.class.getClassLoader() ,null),
		// indexConf.getSchemaName(), null);
	}

	@Override
	public TaskReturn map(TaskContext context) {
		IndexConf indexConf;
		indexConf = new IndexConf(false);
		indexConf.addResource("config.xml");
		try {
			// System.out.println("config.xml url:"
			// + this.getClass().getClassLoader()
			// .getResource("config.xml"));
			indexConf.loadFrom(context);
			// Configuration conf = new Configuration();
			// String fsName = indexConf.getFsName();
			String fsName = indexConf.getSourceFsName();
			logger.warn("remote hdfs host:" + fsName);
			fs = TISHdfsUtils.getFileSystem();
			taskid = context.getUserParam("indexing.taskid");
			String serviceName = context.getUserParam("indexing.servicename");
			final String taskOutPath = context.getMapPath();

			String schemaPath = context.getUserParam("indexing.schemapath");
			if (schemaPath == null) {
				logger.error("[taskid:" + taskid + "]" + "indexing.schemapath 参数没有配置");
				return new TaskReturn(TaskReturn.ReturnCode.FAILURE, "indexing.schemapath 参数没有配置");
			}
			String configPath = context.getUserParam("indexing.configpath");
			try {
				Path srcPath = new Path(schemaPath);
				File dstP = new File(taskOutPath, "schema");
				if (dstP.exists()) {
					dstP.delete();
				}
				dstP.mkdirs();
				Path dstPath = new Path(dstP.getAbsolutePath());
				fs.copyToLocalFile(srcPath, dstPath);
				logger.warn("[taskid:" + taskid + "]" + indexConf.getCoreName() + " get schema done!");
				if (configPath != null) {
					srcPath = new Path(configPath);
					dstP = new File(taskOutPath, "config");
					if (dstP.exists()) {
						dstP.delete();
					}
					dstP.mkdirs();
					dstPath = new Path(dstP.getAbsolutePath());
					fs.copyToLocalFile(srcPath, dstPath);
					logger.warn("[taskid:" + taskid + "]" + indexConf.getCoreName() + " get config done!");
					String normalizePath = configPath.replaceAll("\\\\", "/");
					String configFile = dstP.getAbsolutePath() + File.separator
							+ normalizePath.substring(normalizePath.lastIndexOf("/") + 1);
					context.setUserParam("configFile", configFile);
				}
			} catch (IOException e) {
				// + e);
				return new TaskReturn(TaskReturn.ReturnCode.FAILURE,
						"get schema error:" + ExceptionUtils.getStackTrace(e));
			}
			return new TaskReturn(TaskReturn.ReturnCode.SUCCESS, "success");
		} catch (Throwable e) {
			// logger.error("[taskid:" + taskid + "]" + "get schema fail:", e);
			return new TaskReturn(TaskReturn.ReturnCode.FAILURE, "get schema fail:" + ExceptionUtils.getStackTrace(e));
		}
	}

}
