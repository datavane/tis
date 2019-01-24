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
package com.qlangtech.tis.fullbuild.servlet.impl;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.order.center.IParamContext;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HttpExecContext implements IParamContext {

    private static final Logger logger = LoggerFactory.getLogger(HttpExecContext.class);

    // private final ServletRequest request;
    private final Map<String, String> params;

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

    @Override
    public String getPartitionTimestamp() {
        throw new UnsupportedOperationException();
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
}
