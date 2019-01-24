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
package com.qlangtech.tis.realtime;

import com.qlangtech.tis.realtime.yarn.rpc.JobType;
import junit.framework.TestCase;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestIncrControl extends TestCase {

    public void test() throws Exception {
        // String collection = req.getParameter("collection");
        // if (StringUtils.isBlank(collection)) {
        // throw new ServletException(
        // "param collection:" + collection + " can not be null");
        // }
        // JobType jobTpe = JobType.parseJobType(req.getParameter("action"));
        // boolean stop = Boolean.parseBoolean(req.getParameter("stop"));
        final String content = "collection=search4totalpay&action=" + JobType.IndexJobRunning.getName() + "&stop=false";
    // HttpUtils.post(new URL("http://hadoop5:38665/incr-control?" + content),
    // content, new StreamProcess<Object>() {
    // @Override
    // public Object p(int status, InputStream stream,
    // String md5) {
    // 
    // try {
    // System.out.println(IOUtils.toString(stream));
    // } catch (IOException e) {
    // 
    // throw new RuntimeException(e);
    // }
    // 
    // return null;
    // }
    // 
    // });
    }
}
