/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.datax.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.gilt.logback.flume.FlumeLogstashV1Appender;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.TISCollectionUtils;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.realtime.utils.NetUtils;
import org.apache.commons.lang.StringUtils;

import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年4月15日
 */
public class TisFlumeLogstashV1Appender extends FlumeLogstashV1Appender {

    public static TisFlumeLogstashV1Appender instance;

    public TisFlumeLogstashV1Appender() {
        super();
        if (instance != null) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " shall have not been initialize");
        }
        instance = this;
        super.setFlumeAgents(Config.getAssembleHost() + ":" + Config.LogFlumeAddressPORT);
    }

    public void setFlumeAgents(String flumeAgents) {
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (Config.isTestMock()) {
            return;
        }
        super.append(eventObject);
    }

    @Override
    protected Map<String, String> extractHeaders(ILoggingEvent eventObject) {
        Map<String, String> result = super.extractHeaders(eventObject);
        final Map<String, String> mdc = eventObject.getMDCPropertyMap();

        String taskId = mdc.get(IParamContext.KEY_TASK_ID);

        String collection = StringUtils.defaultIfEmpty(mdc.get(TISCollectionUtils.KEY_COLLECTION), "unknown");
        result.put(TISCollectionUtils.KEY_COLLECTION, collection);
        if (taskId != null) {
            result.put(IParamContext.KEY_TASK_ID, taskId);
        }
        return result;
    }

    @Override
    protected String resolveHostname() throws UnknownHostException {
        return NetUtils.getHostname();
    }

    //    @Override
//    protected HashMap<String, String> createHeaders() {
//        HashMap<String, String> headers = new HashMap<>();
//        //headers.put(ENVIRONMENT_INCR_EXEC_GROUP, "tis-datax");
//        return headers;
//    }
}
