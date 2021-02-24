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
package com.qlangtech.tis.fullbuild.servlet.impl;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletRequest;

import com.qlangtech.tis.exec.ExecutePhaseRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.order.center.IParamContext;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月11日 下午12:11:27
 */
public class HttpExecContext implements IParamContext {

    private static final Logger logger = LoggerFactory.getLogger(HttpExecContext.class);

    private final Map<String, String> params;

    @Override
    public ExecutePhaseRange getExecutePhaseRange() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("all")
    public HttpExecContext(ServletRequest request, Map<String, String> params) {
        super();
        this.params = params;
        String key = null;
        Enumeration en = request.getParameterNames();
        while (en.hasMoreElements()) {
            key = String.valueOf(en.nextElement());
            if (!params.containsKey(key)) {
                params.put(key, request.getParameter(key));
            }
        }
    }

    @SuppressWarnings("all")
    public HttpExecContext(ServletRequest request) {
        this(request, new HashMap<String, String>());
    }

    @Override
    public String getString(String key) {
        String value = params.get(key);
        // logger.info("httprequest key:" + key + ",value:" + value);
        return value;
    }

    @Override
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(this.getString(key));
    }

    @Override
    public int getInt(String key) {
        return Integer.parseInt(this.getString(key));
    }

    @Override
    public long getLong(String key) {
        return Long.parseLong(this.getString(key));
    }

    @Override
    public String getPartitionTimestamp() {
        throw new UnsupportedOperationException();
    }
}
