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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.build.task.TaskReturn;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.hdfs.TISHdfsUtils;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.IndexBuildParam;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsIndexGetConfig implements TaskMapper {

	public static final Logger logger = LoggerFactory.getLogger(HdfsIndexGetConfig.class);

	FileSystem fs;

	long startTime;

	private String taskid = "";

	public HdfsIndexGetConfig() throws IOException {
		startTime = System.currentTimeMillis();
		fs = TISHdfsUtils.getFileSystem();
	}

	public static IndexConf getIndexConf(TaskContext context) {
		IndexConf indexConf;
		indexConf = new IndexConf(context);

		return indexConf;
	}

	@Override
	public TaskReturn map(TaskContext context) {
		final IndexConf indexConf = getIndexConf(context);
		try {

			Path tmpDir = new Path(Environment.PWD.$(), YarnConfiguration.DEFAULT_CONTAINER_TEMP_DIR);
			logger.info("tmp dir:" + tmpDir);
			String fsName = indexConf.getSourceFsName();

			taskid = context.getUserParam("indexing.taskid");

			String serviceName = context.getUserParam(// "indexing.servicename"
					IndexBuildParam.INDEXING_SERVICE_NAME);

			String schemaPath = context.getUserParam(IndexBuildParam.INDEXING_SCHEMA_PATH);
			if (schemaPath == null) {
				logger.error(IndexBuildParam.INDEXING_SCHEMA_PATH + " param have not been config");
				return new TaskReturn(TaskReturn.ReturnCode.FAILURE,
						IndexBuildParam.INDEXING_SCHEMA_PATH + "  param have not been config");
			}

			String solrConfigPath = context.getUserParam(IndexBuildParam.INDEXING_SOLRCONFIG_PATH);
			if (solrConfigPath == null) {
				logger.error(IndexBuildParam.INDEXING_SOLRCONFIG_PATH + " param have not been config");
				return new TaskReturn(TaskReturn.ReturnCode.FAILURE,
						IndexBuildParam.INDEXING_SOLRCONFIG_PATH + " param have not been config");
			}

			try {

				// File dstP =
				copyRemoteFile2Local(new PathStrategy() {
					@Override
					public String getRemotePath() {
						return schemaPath;
					}

					@Override
					public File getLocalDestFile() {
						return getLocalTmpSchemaFile();
					}
				});

				copyRemoteFile2Local(new PathStrategy() {
					@Override
					public String getRemotePath() {
						return solrConfigPath;
					}

					@Override
					public File getLocalDestFile() {
						return getLocalTmpSolrConfigFile();
					}
				});

			} catch (IOException e) {

				return new TaskReturn(TaskReturn.ReturnCode.FAILURE,
						"get schema error:" + ExceptionUtils.getStackTrace(e));
			}
			return new TaskReturn(TaskReturn.ReturnCode.SUCCESS, "success");
		} catch (Throwable e) {
			return new TaskReturn(TaskReturn.ReturnCode.FAILURE, "get schema fail:" + ExceptionUtils.getStackTrace(e));
		}
	}

	public static File getLocalTmpSchemaFile() {
		return new File(getTmpConifgDir(), ConfigFileReader.FILE_SCHEMA.getFileName());
	}

	public static File getLocalTmpSolrConfigFile() {
		return new File(getTmpConifgDir(), ConfigFileReader.FILE_SOLOR.getFileName());
	}

	private static final File getTmpConifgDir() {
		return new File(Environment.PWD.$() + File.separator + YarnConfiguration.DEFAULT_CONTAINER_TEMP_DIR);
	}

	protected File copyRemoteFile2Local(PathStrategy pStrategy) throws IOException {
		Path remotePath = new Path(pStrategy.getRemotePath());
		File dstP = pStrategy.getLocalDestFile();// getLocalTmpSchemaFile();
		FileUtils.forceMkdirParent(dstP);

		Path dstPath = new Path(dstP.getParent());

		fs.copyToLocalFile(remotePath, dstPath);
		logger.info("remote:" + remotePath + " copy to local:" + dstP + " succsessful");
		return dstP;
	}

	private static interface PathStrategy {
		public String getRemotePath();

		public File getLocalDestFile();
	}

}
