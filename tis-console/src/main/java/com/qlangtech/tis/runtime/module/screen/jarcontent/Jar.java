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
package com.qlangtech.tis.runtime.module.screen.jarcontent;

//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import org.apache.commons.io.IOUtils;
//import com.alibaba.citrus.turbine.Context;
//import com.qlangtech.tis.manage.common.ConfigFileReader;
//import com.qlangtech.tis.manage.common.PropteryGetter;
//import com.qlangtech.tis.manage.servlet.DownloadResource;
//import com.qlangtech.tis.manage.servlet.DownloadServlet;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
//public class Jar extends BasicContentScreen {
//
//    /**
//     */
//    private static final long serialVersionUID = 1L;
//
//    private final PropteryGetter getter = ConfigFileReader.FILE_JAR;
//
//    @Override
//    protected PropteryGetter getSolrDependency() {
//        return getter;
//    }
//
//    @Override
//    protected boolean isEditModel() {
//        return false;
//    }
//
//    @Override
//    protected void processContent(Context context) throws UnsupportedEncodingException {
//        getResponse().setContentType(DownloadResource.JAR_CONTENT_TYPE);
//        DownloadServlet.setDownloadName(getResponse(), getter.getFileName());
//        ByteArrayInputStream reader = null;
//        try {
//            reader = new ByteArrayInputStream(getSnapshot(context).getJarFile().getContent());
//            IOUtils.copy(reader, getResponse().getOutputStream());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            IOUtils.closeQuietly(reader);
//        }
//    }
//}
