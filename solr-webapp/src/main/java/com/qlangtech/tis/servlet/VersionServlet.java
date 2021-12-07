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
package com.qlangtech.tis.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.qlangtech.tis.manage.common.TisUTF8;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.util.Version;
import com.qlangtech.tis.solrextend.utils.Assert;

/**
 * 打印该引擎的版本信息<br>
 * 5.3_${timestamp}
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class VersionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private String versionInfo;

    @Override
    public void init() throws ServletException {
        super.init();
        InputStream input = null;
        try {
            input = this.getClass().getResourceAsStream("/version/version.xml");
            List<String> lines = IOUtils.readLines(input, TisUTF8.get());
            for (String line : lines) {
                this.versionInfo = line;
                break;
            }
        } catch (IOException e) {
            throw new ServletException(e);
        } finally {
            IOUtils.closeQuietly(input);
        }
        Assert.assertNotNull("can not find version info", versionInfo);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().print(Version.LATEST.toString() + "_" + versionInfo);
    }
}
