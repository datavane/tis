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
package com.qlangtech.tis.solrj.extend;

import java.io.File;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestParser extends TestCase {

    // public void test() throws Exception {
    // InputStream input = FileUtils
    // .openInputStream(new File(
    // "D:\\j2ee_solution\\eclipse-standard-kepler-SR2-win32-x86_64\\workspace\\terminator-trigger-center\\tis-solrj-client\\r010003.query"));
    // 
    // String content = IOUtils.toString(input);
    // 
    // String[] params = StringUtils.split(content, "&");
    // String[] p = null;
    // for (String pair : params) {
    // p = StringUtils.split(pair, "=");
    // System.out.print(p[0]);
    // System.out.print("=");
    // System.out.println(URLDecoder.decode(p[1]));
    // }
    // 
    // input.close();
    // 
    // }
    public void testInstanceCount() throws Exception {
        InputStream input = FileUtils.openInputStream(new File("D:\\j2ee_solution\\eclipse-standard-kepler-SR2-win32-x86_64\\workspace\\terminator-trigger-center\\tis-solrj-client\\json.txt"));
        LineIterator it = IOUtils.lineIterator(input, "utf8");
        Pattern pattern = Pattern.compile("\"all_menu\":\"(.+?)\"");
        int instanceCount = 0;
        Matcher matcher = null;
        String menu = null;
        while (it.hasNext()) {
            matcher = pattern.matcher(it.nextLine());
            if (matcher.find()) {
                System.out.println(matcher.group(1));
                menu = matcher.group(1);
                instanceCount += menu.split(";").length;
            }
        }
        System.out.println("instanceCount:" + instanceCount);
    // // JSONTokener tokener = new JSONTokener(input);
    // //
    // // JSONArray array = new JSONArray(tokener);
    // // JSONObject o = null;
    // String menu = null;
    // int instanceCount = 0;
    // for (int i = 0; i < array.length(); i++) {
    // o = (JSONObject) array.get(i);
    // 
    // }
    // 
    // 
    }
}
