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
package com.qlangtech.tis.email;

import java.util.Date;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SendMail {

	private static final Logger logger = LoggerFactory.getLogger(SendMail.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

	}

	// 邮箱服务器
	private String host = "smtp.mxhichina.com";

	// // 这个是你的邮箱用户名
	private String username = "tis.service@xxxx.com";

	// 你的邮箱密码:
	private String password = "xxxx";

	private String mail_from = "tis.service@xxxx.com";
	private String personalName = "TIS日报表";

	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	/**
	 * 此段代码用来发送普通电子邮件
	 */
	/**
	 * @param subject
	 * @param content
	 * @throws Exception
	 */
	public void send(String subject, String content, String mail_to2) throws Exception {
		try {
			// 获取系统环境
			Properties props = new Properties();
			// 进行邮件服务器用户认证
			Authenticator auth = new Email_Autherticator();
			props.put("mail.smtp.host", host);
			props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
			props.setProperty("mail.smtp.socketFactory.fallback", "false");
			props.setProperty("mail.smtp.port", "465");
			props.setProperty("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.auth", "true");
			Session session = Session.getDefaultInstance(props, auth);
			// session.setDebug(true);
			MimeMessage message = new MimeMessage(session);
			// message.setHeader("Content-Type", "text/html;charset=utf8");
			// message.setHeader("Content-Transfer-Encoding", "utf8");
			// 设置邮件主题
			message.setSubject(MimeUtility.encodeText(subject, "gb2312", "B"));
			// message.setHeader(name, value); // 设置邮件正文
			// message.setHeader(mail_head_name, mail_head_value); // 设置邮件标题
			// 设置邮件发送日期
			message.setSentDate(new Date());
			Address address = new InternetAddress(mail_from, personalName);
			// 设置邮件发送者的地址
			message.setFrom(address);
			message.setContent(content, "text/html;charset=gb2312");
			logger.info("sendto email:" + mail_to2);
			String[] destAddress = StringUtils.split(mail_to2, ",");
			for (String to : destAddress) {
				Address toAddress2 = new InternetAddress(to);
				message.addRecipient(Message.RecipientType.TO, toAddress2);
			}
			System.out.println("from:" + username);
			// 发送邮件
			Transport.send(message);
			System.out.println("send ok!");
		} catch (Exception ex) {
			throw new Exception(ex.getMessage(), ex);
		}
	}

	/**
	 * 用来进行服务器对用户的认证
	 */
	private class Email_Autherticator extends Authenticator {

		public Email_Autherticator() {
			super();
		}

		// Email_Autherticator(String user, String pwd) {
		// super();
		// username = user;
		// password = pwd;
		// }
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password);
		}
	}
}
