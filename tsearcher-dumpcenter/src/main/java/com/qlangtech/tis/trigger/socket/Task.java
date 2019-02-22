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
package com.qlangtech.tis.trigger.socket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Task {

	private static final Log log = LogFactory.getLog(Task.class);

	// task 是否正在工作？
	private final AtomicBoolean fullDoing = new AtomicBoolean(false);

	private final AtomicBoolean incrDoing = new AtomicBoolean(false);

	public Task() {
		super();
	}

	// @Override
	// public void sendError(TaskContext context, String msg, Exception e) {
	// StringWriter writer = new StringWriter();
	// if (msg != null) {
	// writer.append(msg + "\n");
	// }
	// e.printStackTrace(new PrintWriter(writer));
	// // ExecuteState state = ExecuteState.create(InfoType.ERROR, writer
	// // .toString());
	// // state.setJobId(jobId);
	// // out.writeObject(state);
	// // } catch (Exception e1) {
	// // throw new RuntimeException(e1);
	// // }
	// // }
	//
	// sendMessage(InfoType.ERROR, context, writer.toString());
	// }
	// private void httpReportLogExcute(TaskContext context, ExecuteState state)
	// throws Exception {
	// ObjectOutputStream out = null;
	// ByteArrayOutputStream bOut = new ByteArrayOutputStream();
	// ByteArrayInputStream bInput = null;
	// try {
	// // URL url = new URL(context.getUrl());
	// // URLConnection connection = url.openConnection();
	// // RequestEntity request = new InputStreamRequestEntity(content)
	// // connection.setDoInput(true);
	// // connection.setDoOutput(true);
	// // connection.connect();
	// out = new ObjectOutputStream(bOut);
	// out.writeObject(state);
	// out.flush();
	// out.close();
	// out = null;
	//
	// bInput = new ByteArrayInputStream(bOut.toByteArray());
	// HttpClient client = new HttpClient();
	// PostMethod post = new PostMethod(context.getUrl());
	//
	// RequestEntity request = new InputStreamRequestEntity(bInput);
	// post.setRequestEntity(request);
	// int status = client.executeMethod(post);
	// // String response = post.getResponseBodyAsString();
	// post.releaseConnection();
	// } catch (Exception e) {
	//
	// log.warn("report executeState have problem", e);
	// } finally {
	// IOUtils.closeQuietly(out);
	// }
	// // HttpClient client = new HttpClient();
	// // String masterIp = mZkLock.getMasterIP(this.getServiceName(),
	// // groupName);
	// // int port = getMasterInfoPort(groupName, masterIp);
	// // client.getHostConfiguration().setHost(URL);;
	// // String url = "/terminator-search/http-dump-servlet?core="
	// // + this.getServiceName() + "-" + groupName + "&method=" + method;
	// // logger.warn("send url to trigger build index mastip:" + masterIp
	// // + ",port:" + port + " ," + url);
	//
	// }

	public void sendError(TaskContext context, String message) {
	}

	public void registerTriggerLogService() {
	}

	/**
	 * 任务执行结束
	 *
	 * @param context
	 * @throws Exception
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
