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
package com.qlangtech.tis.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.util.Version;
import com.qlangtech.tis.solrextend.utils.Assert;

/*
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
            input = this.getClass().getResourceAsStream("/version/version.jsp");
            List<String> lines = IOUtils.readLines(input, Charset.forName("utf8"));
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
