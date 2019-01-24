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
package com.qlangtech.tis.config.module.action;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.screen.HdfsView;

/*
 * curl -F runtime=daily -F terminator-search.tar.gz=@terminator-search.tar.gz
 * http://10.68.210.9/upload/tgz 负责接收上传的tgz包
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TgzAction extends HdfsView {

    private static final long serialVersionUID = 1L;

    private static final ServletFileUpload fileUpload;

    private static final Logger logger = LoggerFactory.getLogger(TgzAction.class);

    static {
        DiskFileItemFactory itemFactory = new DiskFileItemFactory();
        itemFactory.setRepository(new File("/home/admin/uploadtmpDir"));
        fileUpload = new ServletFileUpload(itemFactory);
        fileUpload.setFileSizeMax(100 * 1024 * 1024);
        fileUpload.setSizeMax(100 * 1024 * 1024);
        fileUpload.setProgressListener(new ProgressListener() {

            @Override
            public void update(long pBytesRead, long pContentLength, int pItems) {
                logger.info("pBytesRead:{}, pContentLength:{},pItems:{}", pBytesRead, pContentLength, pItems);
            }
        });
    }

    public static final String DEFAULT_PATH = "/user/admin/terminatorUploadRes/";

    public void doUpload(Context context) throws Exception {
        FileSystem fileSys = this.getFilesystem(RunEnvironment.getEnum(StringUtils.defaultIfEmpty(this.getString("runtime"), "online")));
        try {
            List<FileItem> items = fileUpload.parseRequest(this.getRequest());
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            for (FileItem item : items) {
                Path path = new Path(DEFAULT_PATH + format.format(new Date()) + "/" + item.getFieldName());
                FSDataOutputStream outputStream = fileSys.create(path);
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
