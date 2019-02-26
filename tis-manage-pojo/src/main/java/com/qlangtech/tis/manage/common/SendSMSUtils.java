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
package com.qlangtech.tis.manage.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SendSMSUtils {

	public static final Contact BAISUI_PHONE = new Contact(15999999999l, "baisui@xx.com");

	private static final String UTF8 = "utf8";

	private static final Log logger = LogFactory.getLog(SendSMSUtils.class);

	private static Map<Long, AtomicLong> /* moble */
	lastSendSMSTimestampMap = new HashMap<>();

	private static AtomicInteger errCount = new AtomicInteger(0);

	// private static final Joiner joinerWithCommon =
	// Joiner.on(",").skipNulls();
	public static void main(String[] args) {
		send("hello", BAISUI_PHONE);
	}

	public static void send(String content) {
		send(content, BAISUI_PHONE);
	}

	public static void send(String content, Contact... contact) {
		if (!RunEnvironment.isOnlineMode()) {
			return;
		}
		errCount.incrementAndGet();
		for (Contact c : contact) {
			AtomicLong lastSendSMSTimestamp = lastSendSMSTimestampMap.get(c.moblie);
			if (lastSendSMSTimestamp == null) {
				lastSendSMSTimestamp = new AtomicLong();
				lastSendSMSTimestampMap.put(c.moblie, lastSendSMSTimestamp);
			}
		}
		final List<Contact> contacts = new ArrayList<>();
		for (Contact c : contact) {
			AtomicLong lastSendSMSTimestamp = lastSendSMSTimestampMap.get(c.moblie);
			long last = lastSendSMSTimestamp.get();
			// 十分钟之内不能重复发送消息
			if ((last + (15 * 60 * 1000)) < System.currentTimeMillis()) {
				if (lastSendSMSTimestamp.compareAndSet(last, System.currentTimeMillis())) {
					contacts.add(c);
				}
			}
		}
		if (contacts.isEmpty()) {
			logger.info("send frequency is too high,ignore msg:" + content);
			return;
		}

	}

	protected static void applyRequest(final URL url, byte[] content) {
		ConfigFileContext.processContent(url, content, new PostFormStreamProcess<Object>() {

			@Override
			public Object p(int status, InputStream stream, String md5) {
				try {
					JSONObject result = new JSONObject(new JSONTokener(IOUtils.toString(stream, "utf8")));
					if (result.getInt("code") != 1) {
						logger.info("send_sms_msg faild url:" + url);
					}
					return null;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		// ConfigFileContext.processContent(url, new StreamProcess<Object>() {
		// @Override
		// public Object p(int status, InputStream stream, String md5) {
		// try {
		// JSONObject result = new JSONObject(new
		// JSONTokener(IOUtils.toString(stream)));
		// if (result.getInt("code") != 1) {
		// throw new IllegalStateException("send sms msg faild:" +
		// result.getInt("code"));
		// }
		// return null;
		// } catch (Exception e) {
		// throw new RuntimeException(e);
		// }
		// }
		// });
	}

	public static class Contact {

		private final long moblie;

		private final String email;

		/**
		 * @param moblie
		 * @param email
		 */
		public Contact(long moblie, String email) {
			super();
			this.moblie = moblie;
			this.email = email;
		}

		public long getMoblie() {
			return moblie;
		}

		public String getEmail() {
			return email;
		}
	}

}
