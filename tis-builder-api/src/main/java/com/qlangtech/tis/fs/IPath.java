/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.fs;

import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2018年11月23日
 */
public interface IPath {

    public static String pathConcat(String before, String... after) {
        if (before == null) {
            throw new IllegalArgumentException("param before can not be null");
        }
        StringBuffer result = new StringBuffer(before);
        for (int i = 0; i < after.length; i++) {
            // pathConcat(result, after[i]);

            boolean endWithSlash = (before.length() < 1)
                    || (before.charAt(before.length() - 1) == File.separatorChar);

            if (!endWithSlash) {
                result.append(File.separatorChar);
            }
            result.append(StringUtils.removeStart(after[i], String.valueOf(File.separator)));

        }
//        boolean endWithSlash = StringUtils.endsWith(before, (File.separator));
//        final String result = before + (endWithSlash ? StringUtils.EMPTY : File.separatorChar) + StringUtils.removeStart(after, String.valueOf(File.separator));
        return result.toString();
    }

//    static void pathConcat(StringBuffer before, String after) {
//
//        boolean endWithSlash = (before.charAt(before.length() - 1) == File.separatorChar);// StringUtils.endsWith(before., (File.separator));
//        if (!endWithSlash) {
//            before.append(File.separatorChar);
//        }
//        before.append(StringUtils.removeStart(after, String.valueOf(File.separator)));
//        //  return result;
//    }

    public String getName();

    public <T> T unwrap(Class<T> iface);
}
