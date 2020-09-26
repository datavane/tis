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
import com.qlangtech.tis.manage.common.ConfigFileContext.Header;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.PostFormStreamProcess.ContentType;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月17日 下午5:51:38
 */
public class SendSMSUtils {

    public static final Contact BAISUI_PHONE = new Contact(15868113480l, "baisui@2dfire.com");

    // private static final String appKey = "100007";
    // private static final String salt = "718c131f96d644e68976884b186f0ada";
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

    // wiki: http://k.2dfire.net/pages/viewpage.action?pageId=11468888
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
        try {
            content = StringUtils.substring("errs:" + errCount.get() + "|" + content, 0, 100);
            StringBuffer email = new StringBuffer();
            for (int i = 0; i < contacts.size(); i++) {
                email.append(contacts.get(i).getEmail());
                if (i < (contacts.size() - 1)) {
                    email.append(";");
                }
            }
            // 发送丁丁消息
            URL url = new URL("http://sm.2dfire-inc.com/sm-soa/sm/send_msg?msg=" + URLEncoder.encode("from:" + Inet4Address.getLocalHost().getHostAddress() + "|" + content, UTF8) + "&tos=" + URLEncoder.encode(email.toString(), UTF8));
            logger.info("dingding url:" + url);
            ConfigFileContext.processContent(url, new StreamProcess<Object>() {

                @Override
                public Object p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                    try {
                        System.out.println("receive dingding reply:" + IOUtils.toString(stream, "utf8"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }
            });
            if (contacts.size() < 1) {
                return;
            }
            StringBuffer phones = new StringBuffer();
            for (Contact c : contacts) {
                phones.append(c.getMoblie()).append(",");
            }
            // url = new
            // URL("http://msg.2dfire.com/sm-soa/sm/send_ms?source_name=tis&tos="
            // + URLEncoder.encode(sms.toString(), UTF8) + "&type=1&content=" +
            // URLEncoder.encode(content, UTF8));
            url = new URL("http://sm.2dfire-inc.com/sm-soa/sm/send_note");
            logger.info("sms url:" + url);
            StringBuffer buffer = new StringBuffer();
            Map<String, String> paramString = new HashMap<>();
            paramString.put("fromip", StringUtils.replace(Inet4Address.getLocalHost().getHostAddress(), ".", "-"));
            paramString.put("collection", "#");
            paramString.put("msg", content);
            Map<String, String> params = new HashMap<>();
            // params.put("outer_id","");
            params.put("code", "DS_21482144353");
            params.put("phones", phones.toString());
            params.put("param_string", URLEncoder.encode(JSON.toJSONString(paramString), "utf8"));
            params.put("ms_type", "1");
            params.put("send_time", "0");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                buffer.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            applyRequest(url, buffer.toString().getBytes(Charset.forName("utf8")));
        // 发送手机消息
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        errCount.set(0);
        logger.info("send sms");
    }

    protected static void applyRequest(URL url, byte[] content) {
        ConfigFileContext.processContent(url, content, new PostFormStreamProcess<Object>() {

            @Override
            public ContentType getContentType() {
                return PostFormStreamProcess.ContentType.TEXT_HTML;
            }

            @Override
            public Object p(int status, InputStream stream, Map<String, List<String>> headerFields) {
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
    // public static void send(String content, long phone) {
    // if (!RunEnvironment.isOnlineMode()) {
    // return;
    // }
    // errCount.incrementAndGet();
    // 
    // AtomicLong lastSendSMSTimestamp = lastSendSMSTimestampMap.get(phone);
    // if (lastSendSMSTimestamp == null) {
    // lastSendSMSTimestamp = new AtomicLong();
    // lastSendSMSTimestampMap.put(phone, lastSendSMSTimestamp);
    // }
    // 
    // long last = lastSendSMSTimestamp.get();
    // // 十分钟之内不能重复发送消息
    // if ((last + (15 * 60 * 1000)) < System.currentTimeMillis()) {
    // 
    // if (lastSendSMSTimestamp.compareAndSet(last, System.currentTimeMillis()))
    // {
    // 
    // try {
    // content =
    // StringUtils.substring(Inet4Address.getLocalHost().getHostAddress()
    // + "|errs:" + errCount.get() + "|" + content, 0, 100);
    // final String SIGN = "content" + content + "mobile" + phone + salt;
    // 
    // URL url = new URL("http://api.2dfire.com/sms/v2/sendSms1?mobile=" + phone
    // + "&content=" + URLEncoder.encode(content, UTF8) + "&appKey=" + appKey
    // + "&format=json&sign=" + DigestUtils.md5Hex(SIGN.getBytes(UTF8)));
    // logger.info("smsurl:" + url);
    // ConfigFileContext.processContent(url, new StreamProcess<Object>() {
    // @Override
    // public Object p(int status, InputStream stream, String md5) {
    // try {
    // JSONObject result = new JSONObject(
    // new JSONTokener(IOUtils.toString(stream)));
    // if (result.getInt("code") != 1) {
    // throw new IllegalStateException(
    // "send sms msg faild:" + result.getInt("code"));
    // }
    // return null;
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    // });
    // 
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // errCount.set(0);
    // logger.info("send sms");
    // }
    // } else {
    // logger.info("send frequency is too high,ignore msg:" + content);
    // }
    // 
    // }
}
