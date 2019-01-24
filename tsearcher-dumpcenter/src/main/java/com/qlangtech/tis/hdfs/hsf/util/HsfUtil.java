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
package com.qlangtech.tis.hdfs.hsf.util;

import java.util.HashSet;
import java.util.Set;
import com.qlangtech.tis.common.TerminatorConstant;

/*
 * @description
 * @since 2011-9-14 04:06:20
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HsfUtil {

    public static Set<String> generateTerminatorVersions(Set<String> coreNameSet) {
        Set<String> versionSet = new HashSet<String>();
        for (String coreName : coreNameSet) {
            // coreName servername-groupname
            // servername-groupname-read
            versionSet.add(genearteVersion(ServiceType.reader, coreName));
            // servername-groupname-merger
            versionSet.add(genearteVersion(ServiceType.merger, coreName));
        }
        return versionSet;
    }

    public static String genearteVersion(ServiceType type, String coreName) {
        if (type.equals(ServiceType.merger)) {
            String[] s = coreName.split(TerminatorConstant.HSF_VERSION_SEPERATOR);
            String serviceName = s[0];
            return serviceName + TerminatorConstant.HSF_VERSION_SEPERATOR + type.getType();
        } else {
            return coreName + TerminatorConstant.HSF_VERSION_SEPERATOR + type.getType();
        }
    }
}
