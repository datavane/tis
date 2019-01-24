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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

/*
 * curl -F terminator-search.tar.gz=@terminator-search.tar.gz
 * http://10.68.210.9/upload/tgz
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class UploadFileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final ServletFileUpload fileUpload;

    static {
        DiskFileItemFactory itemFactory = new DiskFileItemFactory();
        itemFactory.setRepository(new File("/home/admin/uploadtmpDir"));
        fileUpload = new ServletFileUpload(itemFactory);
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
            List<FileItem> items = fileUpload.parseRequest(req);
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            for (FileItem item : items) {
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
