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
package com.qlangtech.tis.solrj.extend;

import java.io.File;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
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
