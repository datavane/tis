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
package com.qlangtech.tis.hdfs.client.process;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.exception.DataImportHDFSException;

/*
 * @description
 * @since  2011-10-13 04:26:57
 * @version  1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DateFormater implements DataProcessor<String, String> {

	protected static Log logger = LogFactory.getLog(DateFormater.class);

	public static final String FROM_TO_SEPERATOR = "->";

	private static final String DateFormater = null;

	public static ThreadLocal<DateFormat> fromLocal = new ThreadLocal<DateFormat>();

	public static ThreadLocal<DateFormat> toLocal = new ThreadLocal<DateFormat>();

	/**
	 * @uml.property name="formats"
	 */
	private Map<String, String> formats;

	/**
	 * @uml.property name="dateFormats"
	 */
	private Map<String, DateFormatPair> dateFormats;

	// private String defaultDate="yyyyMMddhhmmss";
	/**
	 * @uml.property name="defaultPair"
	 * @uml.associationEnd
	 */
	public DateFormatPair defaultPair;

	/**
	 * 使用前一定要调用该初始化方法，请配置为Spring的init-method属性
	 */
	public void init() {
		Set<String> columnNames = formats.keySet();
		if (dateFormats == null) {
			dateFormats = new HashMap<String, DateFormatPair>(formats.size());
		}
		for (String columnName : columnNames) {
			String formatStr = formats.get(columnName);
			String[] fromToStr = formatStr.split(FROM_TO_SEPERATOR);
			String from = fromToStr[0].trim();
			String to = fromToStr[1].trim();
			dateFormats.put(columnName, new DateFormatPair(from, to));
		}
		defaultPair = new DateFormatPair("yyyy-MM-dd hh:mm:ss", "yyyyMMddhhmmss");
	}

	private void initDefaultDateFormatPair() {
		defaultPair = new DateFormatPair("yyyy-MM-dd hh:mm:ss", "yyyyMMddhhmmss");
	}

	public static void main(String[] args) throws InterruptedException {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd hh:mm:ss.0");
		DateFormater formater = new DateFormater();
		formater.initDefaultDateFormatPair();
		try {
			Date srcDate = formater.defaultPair.getFromDateFormat().parse("2012-02-18 20:02:23");
			String destDate = formater.defaultPair.getToDateFormat().format(srcDate);
			System.out.println("destDate ==>" + destDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean process(Map<String, String> map) throws DataImportHDFSException {
		Set<String> columnNames = dateFormats.keySet();
		try {
			boolean flag = false;
			for (String columName : columnNames) {
				DateFormatPair pair = dateFormats.get(columName);
				try {
					String value = map.get(columName);
					if (StringUtils.isBlank(value)) {
						continue;
					}
					Date srcDate = pair.getFromDateFormat().parse(value);
					String destDate = pair.getToDateFormat().format(srcDate);
					map.put(columName, destDate);
				} catch (ParseException e) {
					flag = true;
				}
				if (flag) {
					// 格式转换错误，使用默认格式
					String value = map.get(columName);
					if (StringUtils.isBlank(value)) {
						continue;
					}
					Date srcDate = defaultPair.getFromDateFormat().parse(value);
					String destDate = defaultPair.getToDateFormat().format(srcDate);
					map.put(columName, destDate);
				}
			}
		} catch (Exception e) {
			throw new DataImportHDFSException("【错误】格式化时间失败=>", e);
		}
		return true;
	}

	public class DateFormatPair {

		private String fromDateStr;

		private String toDateStr;

		public DateFormatPair(String fromDateStr, String toDateStr) {
			this.fromDateStr = fromDateStr;
			this.toDateStr = toDateStr;
		}

		public DateFormat getFromDateFormat() {
			DateFormat format = (DateFormat) fromLocal.get();
			try {
				if (format == null || (!((SimpleDateFormat) format).toPattern().equals(this.getFromDateStr()))) {
					format = new SimpleDateFormat(this.getFromDateStr());
					fromLocal.set(format);
				}
			} catch (Exception ex) {
				throw new RuntimeException("获取fromDateFormat失败", ex);
			}
			return format;
		}

		public DateFormat getToDateFormat() {
			DateFormat format = (DateFormat) toLocal.get();
			try {
				if (format == null || (!((SimpleDateFormat) format).toPattern().equals(this.getToDateStr()))) {
					format = new SimpleDateFormat(this.getToDateStr());
					toLocal.set(format);
				}
			} catch (Exception ex) {
				throw new RuntimeException("获取toDateFormat失败", ex);
			}
			return format;
		}

		/**
		 * @return
		 * @uml.property name="fromDateStr"
		 */
		public String getFromDateStr() {
			return fromDateStr;
		}

		/**
		 * @return
		 * @uml.property name="toDateStr"
		 */
		public String getToDateStr() {
			return toDateStr;
		}
	}

	/**
	 * @return
	 * @uml.property name="formats"
	 */
	public Map<String, String> getFormats() {
		return formats;
	}

	/**
	 * @param formats
	 * @uml.property name="formats"
	 */
	public void setFormats(Map<String, String> formats) {
		this.formats = formats;
	}

	/**
	 * @return
	 * @uml.property name="dateFormats"
	 */
	public Map<String, DateFormatPair> getDateFormats() {
		return dateFormats;
	}

	/**
	 * @param dateFormats
	 * @uml.property name="dateFormats"
	 */
	public void setDateFormats(Map<String, DateFormatPair> dateFormats) {
		this.dateFormats = dateFormats;
	}
}
