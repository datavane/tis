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

package com.qlangtech.tis.plugin.datax.format.guesstype;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/13
 */
public class FocusWildcardTabName {
    /**
     * 整个字符串不能有空格，字符‘*’只能在字符串的最后出现（最多出现一次，也可以不出现），给一个例子：
     * t*est*        不匹配
     * test*           匹配
     * test           匹配
     * te st*           不匹配
     * te st           不匹配
     */
    public static final Pattern wildcardTabNamePattern = Pattern.compile("^([^\\s*]+)(\\*?)$");
    private final String logicalTableName;
    private final boolean wildcard;

    public FocusWildcardTabName(String wildcardTabName) {
        Matcher matcher = wildcardTabNamePattern.matcher(wildcardTabName);
        if (!matcher.matches()) {
            throw new IllegalStateException("illegal wildcardTabName:" + wildcardTabName + " must match pattern:" + wildcardTabNamePattern);
        }
        logicalTableName = StringUtils.lowerCase(matcher.group(1));
        wildcard = StringUtils.isNotEmpty(matcher.group(2));
    }

    public String getLogicalTableName() {
        return this.logicalTableName;
    }

    public boolean isMatch(String physicalTabName) {
        return wildcard
                ? FilenameUtils.wildcardMatch(StringUtils.lowerCase(physicalTabName), logicalTableName + "*")
                : logicalTableName.equalsIgnoreCase(physicalTabName);
    }

    @Override
    public String toString() {
        return "logicalTabName='" + logicalTableName + '\'' +
                ", wildcard=" + wildcard;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FocusWildcardTabName that = (FocusWildcardTabName) o;
        return Objects.equals(logicalTableName, that.logicalTableName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(logicalTableName);
    }

    public static void main(String[] args) {
        FocusWildcardTabName wildcardTabName = new FocusWildcardTabName("test*");
        System.out.println(wildcardTabName.logicalTableName);
        System.out.println(wildcardTabName.wildcard);

//        Matcher matcher = wildcardTabNamePattern.matcher();
//        System.out.println(matcher.matches());
    }
}
