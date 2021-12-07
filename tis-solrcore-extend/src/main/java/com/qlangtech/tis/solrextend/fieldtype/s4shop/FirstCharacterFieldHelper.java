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
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FirstCharacterFieldHelper {

    public static boolean isCharOrNum(char a) {
        int v = a;
        boolean isNum = (v >= 48) && (v <= 57);
        boolean isHighCase = (v >= 65) && (v <= 90);
        boolean isLowCase = (v >= 97) && (v <= 122);
        return isNum || isHighCase || isLowCase;
    }

    public static boolean isChinese(char a) {
        int v = a;
        return (v >= 19968) && (v <= 171941);
    }

    public static int processInt(int old, char fc, int i, int BitsPerValue) {
        int position = (FirstCharacterField.NumOfValue - i - 1) * BitsPerValue;
        /**
         * a~z : 1~26  0~9:27~36 特殊字符:42
         */
        int num = (fc >= '0' && fc <= '9') ? (fc - '0' + 27) : (fc - 'a' + 1);
        old = (num << position) | old;
        return old;
    }

    public static void main(String[] args) {
        System.out.println(processInt(0, '9', 1, 5));
    }
}
