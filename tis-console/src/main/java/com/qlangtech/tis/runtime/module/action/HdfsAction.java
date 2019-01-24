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
package com.qlangtech.tis.runtime.module.action;

import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.common.DefaultFilter;
import com.qlangtech.tis.runtime.module.screen.HdfsView;

/*
 * hdfs 服务管理
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HdfsAction extends HdfsView {

    private static final long serialVersionUID = 1L;

    public void doDownload(Context context) {
        final Path path = new Path(this.getString("path"));
        // BufferedReader reader = new BufferedReader(new InputStreamReader());
        InputStream inputStream = null;
        try {
            inputStream = this.getFilesystem().open(path);
            HttpServletResponse response = (HttpServletResponse) DefaultFilter.getRespone();
            response.setContentType("application/text");
            response.addHeader("Content-Disposition", "attachment; filename=\"" + path.getName() + ".txt\"");
            IOUtils.copyLarge(inputStream, response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
