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
package com.qlangtech.tis.manage.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CheckYuntiSuccessFileIsReadyServlet extends BasicServlet {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CheckYuntiSuccessFileIsReadyServlet.class);

    private static final Pattern PATTERN_HDFS_RESULT = Pattern.compile("2[0-9]{13}");

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        InputStream reader = null;
        final String yuntipath = req.getParameter("yuntipath");
        Assert.assertNotNull("parameter yuntipath can not be null" + yuntipath);
        try {
            final String execCommand = "/home/admin/hadoop-0.19.1/bin/hadoop fs -cat " + yuntipath;
            Process process = Runtime.getRuntime().exec(execCommand);
            reader = process.getInputStream();
            LineIterator lineiterator = IOUtils.lineIterator(reader, "utf8");
            final String hdfsExecuteResult = lineiterator.nextLine();
            log.info("command " + execCommand + " result first line:" + hdfsExecuteResult);
            final PrintWriter writer = resp.getWriter();
            Matcher matcher = null;
            if ((matcher = PATTERN_HDFS_RESULT.matcher(StringUtils.trimToEmpty(hdfsExecuteResult))).matches()) {
                log.info("matched");
                // return true;
                writer.println(String.valueOf(true));
            } else {
                writer.println(String.valueOf(false));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
}
