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
package com.qlangtech.tis.web.start;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.DefaultContextSelector;

/**
 * 为web容器中实现多app 日志隔离功能，<br/>
 * 参考：http://logback.qos.ch/manual/loggingSeparation.html
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-12 09:24
 */
public class TISContextSelector extends DefaultContextSelector {

    public TISContextSelector(LoggerContext context) {
        super(context);
    }

    @Override
    public LoggerContext getLoggerContext() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return super.getLoggerContext();
    }
}
