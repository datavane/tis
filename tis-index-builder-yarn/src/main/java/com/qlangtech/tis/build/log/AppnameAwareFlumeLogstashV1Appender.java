package com.qlangtech.tis.build.log;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.gilt.logback.flume.FlumeLogstashV1Appender;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.indexbuilder.HdfsIndexBuilder;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 发送日志的时候会将当前上下文MDC“app”参数发送
 * 
 * @author 百岁
 *
 * @date 2016年8月20日
 */
public class AppnameAwareFlumeLogstashV1Appender extends FlumeLogstashV1Appender {

	private static final Set<AppnameAwareFlumeLogstashV1Appender> flumeAppenderSet = new HashSet<>();

	private boolean closed = false;

	public static void closeAllFlume() {
		int closeCount = 0;
		for (FlumeLogstashV1Appender appender : flumeAppenderSet) {
			try {
				appender.stop();
				closeCount++;
			} catch (Throwable e) {
			}
		}
		System.out.println("closeFlumeClientCount:" + closeCount);
	}

	public AppnameAwareFlumeLogstashV1Appender() {
		super();
		super.setFlumeAgents(TSearcherConfigFetcher.get().getLogFlumeAddress());
		// super.setFlumeAgents("10.1.21.48:41414");
		flumeAppenderSet.add(this);
	}

	@Override
	public void stop() {
		this.closed = true;
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		super.stop();
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		if (closed) {
			return;
		}
		super.append(eventObject);
	}

	public void setFlumeAgents(String flumeAgents) {
		// super.setFlumeAgents(flumeAgents);
	}

	@Override
	protected Map<String, String> extractHeaders(ILoggingEvent eventObject) {
		Map<String, String> result = super.extractHeaders(eventObject);
		final Map<String, String> mdc = eventObject.getMDCPropertyMap();
		String collection = StringUtils.defaultIfEmpty(mdc.get(HdfsIndexBuilder.KEY_COLLECTION), "unknown");
		result.put(HdfsIndexBuilder.KEY_COLLECTION, collection);
		return result;
	}

}
