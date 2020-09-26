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
