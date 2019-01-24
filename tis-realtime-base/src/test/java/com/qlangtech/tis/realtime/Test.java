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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import junit.framework.TestCase;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Test extends TestCase {

    private static final Pattern ADDRESS_PATTERN = Pattern.compile("(.+?):(\\d+)$");

    public void test() {
        // System.out.println(new Date(1449676298000l));
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss.SSS");
        System.out.println(f.format(new Date()));
    // Matcher m = ADDRESS_PATTERN.matcher("127.0.0.1:8945");
    // 
    // if (m.matches()) {
    // System.out.println(m.group(1));
    // System.out.println(m.group(2));
    // }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
    // LineIterator it = FileUtils.lineIterator(new
    // File("D:\\tmp\\tab.txt"));
    // 
    // Set<String> tabs = new HashSet<String>();
    // while (it.hasNext()) {
    // tabs.add(it.nextLine());
    // }
    // 
    // for (String tab : tabs) {
    // System.out.println(tab);
    // }
    }
}
