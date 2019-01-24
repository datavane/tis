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
package com.qlangtech.tis.realtime.utils;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.hdfs.TisIncrLauncher;
import com.qlangtech.tis.realtime.transfer.BasicONSListener;
import com.gilt.logback.flume.FlumeLogstashV1Appender;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import ch.qos.logback.classic.spi.ILoggingEvent;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisFlumeLogstashV1Appender extends FlumeLogstashV1Appender {

    private final String execGroupName;

    public TisFlumeLogstashV1Appender() {
        super();
        this.execGroupName = System.getProperty(TisIncrLauncher.ENVIRONMENT_INCR_EXEC_GROUP);
        if (StringUtils.isEmpty(execGroupName)) {
            throw new IllegalArgumentException("param:" + execGroupName + " can not be null");
        }
        super.setFlumeAgents(TSearcherConfigFetcher.get().getLogFlumeAddress());
    }

    public void setFlumeAgents(String flumeAgents) {
    // super.setFlumeAgents(flumeAgents);
    // System.out.println("flumeAgents:" + flumeAgents);
    }

    // @Override
    // public void start() {
    // long start = System.currentTimeMillis();
    // try {
    // System.out.println(
    // "start flumeLogstash connecting,agent:" + TSearcherConfigFetcher.get().getLogFlumeAddress());
    // super.start();
    // } finally {
    // System.out.println("flumeLogstash connect consume:" + (System.currentTimeMillis() - start) + "ms");
    // }
    // }
    @Override
    protected Map<String, String> extractHeaders(ILoggingEvent eventObject) {
        Map<String, String> result = super.extractHeaders(eventObject);
        final Map<String, String> mdc = eventObject.getMDCPropertyMap();
        String collection = StringUtils.defaultIfEmpty(mdc.get(BasicONSListener.KEY_COLLECTION), "unknown");
        result.put(BasicONSListener.KEY_COLLECTION, collection);
        return result;
    }

    @Override
    protected HashMap<String, String> createHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(TisIncrLauncher.ENVIRONMENT_INCR_EXEC_GROUP, execGroupName);
        return headers;
    }
}
