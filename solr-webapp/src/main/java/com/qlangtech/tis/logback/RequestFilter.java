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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.core.SolrCore;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年3月22日
 */
public class RequestFilter extends Filter<ILoggingEvent> {

    private boolean justIgnoreGet = false;

    private static final String SOLR_REQUEST_LOGGER = SolrCore.class.getName() + ".Request";

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (justIgnoreGet) {
            return ((StringUtils.indexOf(event.getMessage(), "/select") > -1)
                    || (StringUtils.indexOf(event.getMessage(), "/export") > -1))
                    ? FilterReply.ACCEPT : FilterReply.DENY;
        }
        if (SOLR_REQUEST_LOGGER.equals(event.getLoggerName()) && event.getLevel() == Level.INFO) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

    public boolean isJustIgnoreGet() {
        return justIgnoreGet;
    }

    public void setJustIgnoreGet(boolean justIgnoreGet) {
        this.justIgnoreGet = justIgnoreGet;
    }
}
