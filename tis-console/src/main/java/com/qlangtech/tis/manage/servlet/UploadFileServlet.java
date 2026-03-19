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
package com.qlangtech.tis.manage.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * curl -F terminator-search.tar.gz=@terminator-search.tar.gz
 * http://10.68.210.9/upload/tgz
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年8月9日下午5:56:56
 */
public class UploadFileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> fileUpload;

    static {
        DiskFileItemFactory itemFactory = DiskFileItemFactory.builder()
                .setPath(new File("/tmp").toPath())
                .get();
        fileUpload = new JakartaServletFileUpload<>(itemFactory);
    }

    private static final String uploadDir = "/home/admin/tomcat/taobao-tomcat-7.0.52.2/deploy/up";

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        File file = new File(uploadDir);
        if (!file.isDirectory()) {
            throw new ServletException(file.getPath() + " is not a dir");
        }
        JSONArray array = new JSONArray();
        array.put(file.list());
        try {
            resp.getWriter().write(array.toString(1));
        } catch (JSONException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doGet(req, resp);
        File file = new File(uploadDir);
        if (!file.isDirectory()) {
            throw new ServletException(file.getPath() + " is not a dir");
        }
        StringBuffer content = new StringBuffer();
        File subDir = null;
        content.append("	<html>");
        content.append("		 <head>");
        content.append("		 </head>");
        content.append("		 <body>");
        content.append("		  <h1>repository</h1>");
        content.append("	  <ul>");
        for (String ff : file.list()) {
            subDir = new File(file, ff);
            if (!subDir.isDirectory()) {
                continue;
            }
            content.append("<li><strong>").append(ff).append("</strong>");
            for (String f : subDir.list()) {
                content.append("<a href='/up/").append(ff).append("/").append(f).append("'>").append(f).append("</a> &nbsp;");
            }
            content.append("</li>");
        }
        content.append("	  </ul>");
        content.append("	 </body>");
        content.append("	</html>");
        resp.getWriter().write(content.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<DiskFileItem> items = fileUpload.parseRequest(req);
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            for (DiskFileItem item : items) {
                FileOutputStream outputStream = FileUtils.openOutputStream(new File(uploadDir + "/" + format.format(new Date()) + "/" + item.getFieldName()));
                IOUtils.copy(item.getInputStream(), outputStream);
                outputStream.flush();
                outputStream.close();
                break;
            }
        } catch (FileUploadException e) {
            throw new ServletException(e);
        }
    }
}
