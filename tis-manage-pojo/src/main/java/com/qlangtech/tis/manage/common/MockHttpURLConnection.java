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
package com.qlangtech.tis.manage.common;

import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MockHttpURLConnection extends HttpURLConnection {

    private final InputStream inputStream;

    private final Map<String, List<String>> headerFields;

    public MockHttpURLConnection(InputStream inputStream) {
        this(inputStream, Collections.emptyMap());
    }

    public MockHttpURLConnection(InputStream inputStream, Map<String, List<String>> headerFields) {
        super(null);
        if (inputStream == null) {
            throw new IllegalStateException("param inputStream can not be null");
        }
        this.inputStream = inputStream;
        this.headerFields = headerFields;
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return this.headerFields;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.inputStream;
    }

    @Override
    public void disconnect() {
        IOUtils.closeQuietly(this.inputStream);
    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    @Override
    public void connect() throws IOException {
    }
}
