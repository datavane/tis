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
package com.qlangtech.tis.exec.impl;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.dump.hive.HiveRemoveHistoryDataTask;
import com.qlangtech.tis.exec.ActionInvocation;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.servlet.BuildTriggerServlet;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo.HdfsSourcePathCreator;

/*
 * 当直接使用数据中心构建好的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public final class IndexBuildWithHdfsPathInterceptor extends IndexBuildInterceptor {

	private static final String COMPONENT_NAME = "directbuild";

	private static final String HDFS_PATH = "hdfspath";

	private static final Logger logger = LoggerFactory.getLogger(IndexBuildWithHdfsPathInterceptor.class);

	// 小全量触发相关参数
	public static String KEY_PARTIAL = "dump.partial";
	public static String KEY_PARTIAL_SWITCH = KEY_PARTIAL + ".build";
	public static String KEY_PARTIAL_PT_PATTERN = KEY_PARTIAL + ".pt.pattern";
	public static String KEY_PARTIAL_PMOD_SIZE = KEY_PARTIAL + ".pmod.size";

	@Override
	public ExecuteResult intercept(ActionInvocation invocation) throws Exception {
		IExecChainContext execContext = invocation.getContext();
		// 删除历史build索引文件
		HiveRemoveHistoryDataTask removeHistoryDataTask = new HiveRemoveHistoryDataTask(execContext.getIndexName(),
				execContext.getContextUserName(), execContext.getDistributeFileSystem());
		removeHistoryDataTask.removeHistoryBuildFile();
		return super.intercept(invocation);
	}

	@Override
	protected HdfsSourcePathCreator createIndexBuildSourceCreator(final IExecChainContext execContext) {

		boolean buildWithDumpPartial = execContext.getBoolean(KEY_PARTIAL_SWITCH);
		final String hdfspath = execContext.getString(HDFS_PATH);
		final FileSystem fs = execContext.getDistributeFileSystem();
		if (buildWithDumpPartial) {

			// 执行小全量
			String partialPattern = execContext.getString(KEY_PARTIAL_PT_PATTERN);
			if (StringUtils.isBlank(partialPattern)) {
				throw new IllegalStateException("partialPattern can not be blank");
			}
			if (StringUtils.indexOf(partialPattern, "/") > -1) {
				throw new IllegalStateException("partialPattern:" + partialPattern + " can not contain split char '/'");
			}

			if (StringUtils.indexOf(partialPattern, "(") < 0 || StringUtils.indexOf(partialPattern, ")") < 0) {
				throw new IllegalStateException(
						"partialPattern:" + partialPattern + " can not contain parentheses '(' and ')'");
			}

			try {
				Pattern.compile(partialPattern);
			} catch (PatternSyntaxException e) {
				throw new RuntimeException("partialPattern:" + partialPattern, e);
			}

			return new HdfsSourcePathCreator("buildWithDumpPartial") {
				@Override
				public String build(String groupOrd) {
					final String path = hdfspath + "/" + partialPattern + "/pmod=" + groupOrd;
					logger.info("source hdfs path:" + path);
					return path;
				}

				@Override
				public int getGroupSize(FileSystem hdfs) {
					try {
						int partialPmodSize = execContext.getInt(KEY_PARTIAL_PMOD_SIZE);
						if (partialPmodSize < 1) {
							throw new IllegalStateException(
									KEY_PARTIAL_PMOD_SIZE + ":" + partialPmodSize + " can not small than 1");
						}
						return partialPmodSize;
					} catch (IllegalStateException e) {
						throw e;
					} catch (Exception e) {
						throw new IllegalArgumentException(
								"buildWithDumpPartial shall set param '" + KEY_PARTIAL_PMOD_SIZE + "'", e);
					}
				}

			};

		} else {

			return new HdfsSourcePathCreator("buildAll") {
				@Override
				public String build(String group) {
					String path = hdfspath + "/pmod=" + group;
					try {
						if (fs.exists(new Path(path))) {
							return path;
						}
						logger.info("sourcepath not exist:" + path);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					final String targetPath = "0".equals(group) ? hdfspath : path;
					logger.info("source hdfs path:" + targetPath);
					return targetPath;
				}
			};
		}
	}

	@Override
	protected void setBuildTableTitleItems(String indexName, ImportDataProcessInfo processinfo,
			IExecChainContext execContext) {
		processinfo.setBuildTableTitleItems(execContext.getString(BuildTriggerServlet.KEY_COLS));
		processinfo.setHdfsdelimiter(//
				StringUtils.defaultIfEmpty(execContext.getString(ImportDataProcessInfo.KEY_DELIMITER),
						ImportDataProcessInfo.DELIMITER_001));
	}

	@Override
	public String getName() {
		return COMPONENT_NAME;
	}
}
