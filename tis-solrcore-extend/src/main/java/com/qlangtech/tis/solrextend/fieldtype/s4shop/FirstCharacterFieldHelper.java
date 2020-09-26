/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
