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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-11-1
 */
public class CheckYuntiSuccessFileIsReadyServlet extends BasicServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(CheckYuntiSuccessFileIsReadyServlet.class);

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
