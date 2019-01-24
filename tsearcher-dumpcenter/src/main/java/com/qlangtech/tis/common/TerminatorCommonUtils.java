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
package com.qlangtech.tis.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorCommonUtils {

    private static String localIp = null;

    /**
     * 获取本机IP
     *
     * @return
     */
    public static final String getLocalHostIP() {
        if (StringUtils.isBlank(localIp)) {
            try {
                localIp = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException("[local-ip] an exception occured when get local ip address", e);
            }
        }
        return localIp;
    }

    /**
     * 判断coreName是否是合法的形式 serviceName-groupName 如 search4album-0
     *
     * @param coreName
     * @return
     */
    public static final boolean isCorrectCoreName(String coreName) {
        if (!StringUtils.isBlank(coreName)) {
            String[] ss = coreName.split(TerminatorConstant.CORENAME_SEPERATOR);
            return ss.length == 2;
        }
        return false;
    }

    /**
     * 拆分coreName为serviceName  groupName两部分
     *
     * @param coreName
     * @return
     */
    public static String[] splitCoreName(String coreName) {
        if (isCorrectCoreName(coreName)) {
            return coreName.split(TerminatorConstant.CORENAME_SEPERATOR);
        } else {
            return null;
        }
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static Date parseDate(String str) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
    }
}
