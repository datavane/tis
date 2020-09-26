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
