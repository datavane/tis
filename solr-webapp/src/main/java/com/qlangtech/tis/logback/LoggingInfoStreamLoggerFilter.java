/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.logback;

import org.apache.solr.update.LoggingInfoStream;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年3月22日
 */
public class LoggingInfoStreamLoggerFilter extends Filter<ILoggingEvent> {

    private static final String LoggingInfoStream = LoggingInfoStream.class.getName();

    private static final RequestFilter requestFilter;

    static {
        requestFilter = new RequestFilter();
        requestFilter.setJustIgnoreGet(false);
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
            return FilterReply.ACCEPT;
        }
        if (LoggingInfoStream.equals(event.getLoggerName()) && event.getLevel() == Level.INFO) {
            return FilterReply.DENY;
        }
        if (requestFilter.decide(event) == FilterReply.DENY) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }
}
