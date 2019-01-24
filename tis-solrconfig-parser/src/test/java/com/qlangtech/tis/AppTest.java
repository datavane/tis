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
package com.qlangtech.tis;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * Unit test for simple App.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AppTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName
     *            name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }

    private static final Pattern APPNAME_PATTERN = Pattern.compile("search4[a-zA-Z0-9]+");

    public static void main(String[] arg) throws Exception {
        Matcher m = APPNAME_PATTERN.matcher("search4realjhsGGGG234");
        System.out.println(m.matches());
    }
    // public static void main(String[] arg) throws Exception {
    // // 172.23.56.162:7001/terminator-search/search4realyunosic-0/select/?q=
    // String source =
    // "%28name%3A%E6%89%8B%E6%9C%BA%E7%AE%A1%E5%AE%B6%5E1000+OR+name_token%3A%E6%89%8B%E6%9C%BA%E7%AE%A1%E5%AE%B6%5E600+OR+name_pinyin%3A%E6%89%8B%E6%9C%BA%E7%AE%A1%E5%AE%B6%5E1000+OR+name_pinyin_token%3A%E6%89%8B%E6%9C%BA%E7%AE%A1%E5%AE%B6%5E600+OR+name_jianpin%3A%E6%89%8B%E6%9C%BA%E7%AE%A1%E5%AE%B6%5E1000+OR+name_jianpin_token%3A%E6%89%8B%E6%9C%BA%E7%AE%A1%E5%AE%B6%5E600+OR+keywords%3A%E6%89%8B%E6%9C%BA%E7%AE%A1%E5%AE%B6%5E30+OR+author_nick%3A%E6%89%8B%E6%9C%BA%E7%AE%A1%E5%AE%B6%5E10+OR+description%3A%E6%89%8B%E6%9C%BA%E7%AE%A1%E5%AE%B6%5E10%29+AND+%28_val_%3Aspu_week_download%29%5E200+AND+%28_val_%3Aspu_grade%29%5E200+AND+%28type%3A0%29+AND+%28status%3A1%29+AND+%28%28%28*%3A*+-%28device_model_support%3A1%29%29+AND+-%28device_model%3AK%5C-Touch%5C+E619%29%29+OR+%28device_model_support%3A1+AND+%28device_model%3AK%5C-Touch%5C+E619%29%29%29+AND+-%28yunos_ver%3A2.3.0%5C-E1%5C-20131118.0526%29+AND+-%28android_ver%3A2.3%29&group=true&group.ngroups=true&group.field=package_name&group.sort=version_code+desc&group.limit=1&start=0&rows=20&debugQuery=on";
    // 
    // System.out.println(URLEncoder.encode(
    // URLDecoder.decode(source, "utf-8"), "gbk"));
    // 
    // 
    // 
    // 
    // }
}
