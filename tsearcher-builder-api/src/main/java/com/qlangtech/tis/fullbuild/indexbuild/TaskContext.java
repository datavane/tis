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
package com.qlangtech.tis.fullbuild.indexbuild;

import java.util.HashMap;

import com.qlangtech.tis.build.metrics.Counters;
import com.qlangtech.tis.build.metrics.Messages;
import com.qlangtech.tis.manage.common.IndexBuildParam;
import org.apache.commons.cli.CommandLine;

/* 
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TaskContext extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;
	private final Counters counters = new Counters();
	private final Messages message = new Messages();

	private final CommandLine commandLine;

	private TaskContext(CommandLine commandLine) {
		super();
		this.commandLine = commandLine;
	}

	public static TaskContext create(CommandLine commandLine) {
		return new TaskContext(commandLine);
	}

	private Integer allRowCount;

	public int getAllRowCount() {
		if (allRowCount == null) {
			try {
				allRowCount = Integer.parseInt(getInnerParam(IndexBuildParam.INDEXING_ROW_COUNT));
			} catch (Throwable e) {
			}
			allRowCount = Integer.MAX_VALUE;
		}
		return allRowCount;
	}

	/**
	 * 构建索引已经完成的条数
	 * 
	 * @return
	 */
	public long getIndexMakerComplete() {
		return this.counters.getCounter(Counters.Counter.DOCMAKE_COMPLETE).get();
	}

	public Counters getCounters() {
		return this.counters;
	}

	public Messages getMessages() {
		return this.message;
	}

	public String getInnerParam(String key) {
		return commandLine.getOptionValue(key);
	}

	public String getUserParam(String key) {
		return commandLine.getOptionValue(key);
	}

	public String getMapPath() {
		return commandLine.getOptionValue("task.map.output.path");
	}

	public void setUserParam(String key, String val) {

	}

}
