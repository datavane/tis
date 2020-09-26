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
package com.qlangtech.tis.realtime.transfer;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class UnderlineUtils {

    private UnderlineUtils() {
    }

    public static StringBuffer removeUnderline(String value) {
        StringBuffer parsedName = new StringBuffer();
        char[] nameAry = value.toCharArray();
        boolean findUnderChar = false;
        for (int i = 0; i < nameAry.length; i++) {
            if (nameAry[i] == '_') {
                findUnderChar = true;
            } else {
                if (findUnderChar) {
                    parsedName.append(Character.toUpperCase(nameAry[i]));
                    findUnderChar = false;
                } else {
                    parsedName.append(nameAry[i]);
                }
            }
        }
        return parsedName;
    }

    public static StringBuffer addUnderline(String value) {
        StringBuffer parsedName = new StringBuffer();
        char[] nameAry = value.toCharArray();
        for (int i = 0; i < nameAry.length; i++) {
            if (Character.isUpperCase(nameAry[i])) {
                parsedName.append('_').append(Character.toLowerCase(nameAry[i]));
            } else {
                parsedName.append(nameAry[i]);
            }
        }
        return parsedName;
    }
}
