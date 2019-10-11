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
package com.qlangtech.tis.logback;

import org.apache.solr.update.LoggingInfoStream;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LoggingInfoStreamLoggerFilter extends Filter<ILoggingEvent> {

    private static final String LoggingInfoStream = LoggingInfoStream.class.getName();

    private static final RequestFilter requestFilter;

    static {
        requestFilter = new RequestFilter();
        requestFilter.addAcceptPath("/get");
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
//        if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
//            return FilterReply.ACCEPT;
//        }
        if (requestFilter.decide(event) == FilterReply.ACCEPT && event.getLevel() != Level.ERROR) {
            return FilterReply.DENY;
        }
        if (event.getLevel() == Level.INFO && LoggingInfoStream.equals(event.getLoggerName())) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }
}
