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
package com.qlangtech.tis.dump.hive;

import org.apache.hadoop.hive.ql.exec.UDF;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LogoUrl extends UDF {

    public String evaluate(String entity_id, final String attachment_id, final String typee, final String server, String path) {
        int type = 0;
        try {
            type = Integer.parseInt(typee);
        } catch (Throwable e) {
        }
        if ("99928321".equals(entity_id)) {
            System.out.println("====================================================================================");
            return ("attid:" + attachment_id + ",type:" + type + ",server:" + server + ",path:" + path);
        // System.out.println("====================================================================================");
        }
        if (isBlank(attachment_id) && type == 3 && "zmfile.2dfire-daily.com".equals(server)) {
            return ("http://" + server + "/upload_files/" + path);
        } else if (isBlank(attachment_id) && type == 3 && "ifiletest.2dfire.com".equals(server)) {
            return ("http://" + server + "/" + path);
        } else if (isBlank(attachment_id) && type == 0 && "zmfile.2dfire-daily.com".equals(server)) {
            return ("http://" + server + "/upload_files/" + path);
        } else if (isBlank(attachment_id) && (type == 0 && "ifiletest.2dfire.com".equals(server))) {
            return ("http://" + server + '/' + path);
        } else {
            return ("http://" + server + "/upload_files/" + path);
        }
    }

    private static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
}
