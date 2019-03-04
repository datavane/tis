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

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.build.task.TaskReturn;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.hdfs.TISHdfsUtils;

/*
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsIndexDeliver implements TaskMapper {

	public static final Logger logger = LoggerFactory.getLogger(HdfsIndexDeliver.class);

	IndexConf indexConf;

	FileSystem fs;

	long startTime;

	public HdfsIndexDeliver() throws IOException {
		startTime = System.currentTimeMillis();
		indexConf = new IndexConf(false);
		indexConf.addResource("config.xml");
	}

	@Override
	public TaskReturn map(TaskContext context) {
		try {
			long start = System.currentTimeMillis();
			indexConf.loadFrom(context);

			fs = TISHdfsUtils.getFileSystem();
			String taskOutPath = context.getMapPath();
			String destOutPath = context.getUserParam("indexing.outputpath");
			if (destOutPath == null) {
				return new TaskReturn(TaskReturn.ReturnCode.FAILURE, "indexing.outputpath 参数没有配置");
			}
			Path destPath = new Path(destOutPath);
			logger.warn(indexConf.getCoreName() + " deliver done!take " + (System.currentTimeMillis() - start) / 1000
					+ " seconds");
			return new TaskReturn(TaskReturn.ReturnCode.SUCCESS, "success");
		} catch (Throwable e) {
			logger.error("deliver error:" + e);
			e.printStackTrace();
			return new TaskReturn(TaskReturn.ReturnCode.FAILURE, "deliver fail:" + e);
		}
	}

}
