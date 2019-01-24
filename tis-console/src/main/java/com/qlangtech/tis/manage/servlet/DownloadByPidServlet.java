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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.pubhook.common.ConfigConstant;

/*
 * 通过包和snapshot来下载资源包
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DownloadByPidServlet extends DownloadServlet {

    private static final long serialVersionUID = -8824528411218625478L;

    // private static final Pattern download_pattern = Pattern
    // .compile(".+?/download/bypid/(\\d+)/snid/(\\d+)/("
    // + replace(ConfigConstant.FILE_APPLICATION + "|"
    // + ConfigConstant.FILE_DATA_SOURCE + "|"
    // + ConfigConstant.FILE_SCHEMA + "|"
    // + ConfigConstant.FILE_SOLOR) + "|"
    // + DownloadResource.JAR_NAME + ")");
    private static final Pattern download_pattern = Pattern.compile(".+?/download/snid/(\\d+)/(" + replace(ConfigConstant.FILE_APPLICATION + "|" + ConfigConstant.FILE_DATA_SOURCE + "|" + ConfigConstant.FILE_SCHEMA + "|" + ConfigConstant.FILE_SOLOR) + "|" + DownloadResource.JAR_NAME + ")");

    // @Override
    // protected void doGet(HttpServletRequest reqeust,
    // HttpServletResponse response) throws ServletException, IOException {
    // 
    // }
    @Override
    protected DownloadResource getDownloadResource(Matcher matcher) {
        // AppPackage pack = this.getContext().getAppPackageDAO()
        // .selectByPrimaryKey(Integer.parseInt(matcher.group(1)));
        SnapshotDomain snapshot = this.getContext().getSnapshotViewDAO().getView(Integer.parseInt(matcher.group(1)));
        // Snapshot snapshot, String resourceName
        return new DownloadResource(this.getContext().getApplicationDAO().selectByPrimaryKey(snapshot.getAppId()), snapshot, matcher.group(2));
    // return super.getDownloadResource(matcher);
    // return resource;
    }

    @Override
    protected StringBuffer getAppendInfo(HttpServletRequest request, DownloadResource downloadRes) throws IOException {
        return new StringBuffer();
    }

    @Override
    protected Pattern getURLPattern() {
        return download_pattern;
    }
}
