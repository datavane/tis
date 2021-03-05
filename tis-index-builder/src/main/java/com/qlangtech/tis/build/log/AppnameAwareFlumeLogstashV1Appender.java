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
package com.qlangtech.tis.build.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.gilt.logback.flume.FlumeLogstashV1Appender;
import com.qlangtech.tis.indexbuilder.IndexBuilderTask;
import com.qlangtech.tis.manage.common.Config;
import org.apache.commons.lang.StringUtils;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 发送日志的时候会将当前上下文MDC“app”参数发送
 *
 * @author 百岁（baisui@qlangtech.com）
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
        super.setFlumeAgents(Config.getAssembleHost() + ":" + Config.LogFlumeAddressPORT);
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
        String collection = StringUtils.defaultIfEmpty(mdc.get(IndexBuilderTask.KEY_COLLECTION), "unknown");
        result.put(IndexBuilderTask.KEY_COLLECTION, collection);
        return result;
    }
}
