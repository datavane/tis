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
package com.qlangtech.tis.hdfs.client.bean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.common.utils.MockHDFSProvider;
import com.qlangtech.tis.exception.TerminatorInitException;
import com.qlangtech.tis.hdfs.client.service.ImportGroupServiceSupport;

/*
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsRealTimeTerminatorBean extends BasicTerminatorClient implements ImportGroupServiceSupport {

	private static Log logger = LogFactory.getLog(HdfsRealTimeTerminatorBean.class);

	public static final String DEFAULT_SERVLET_CONTEXT = "terminator-search";

	public static final int DEFAULT_ZK_TIMEOUT = 300000;

	@Override
	public final boolean isShallConnectTriggerServer() {
		return !(this.getFullHdfsProvider() instanceof MockHDFSProvider);
	}

	@Override
	public void init() throws TerminatorInitException {
		super.init();
	}

	public static void main(String[] args) {

	}
}
