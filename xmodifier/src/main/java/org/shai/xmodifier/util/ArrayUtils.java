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
package org.shai.xmodifier.util;

/**
 * Created by Shenghai on 14-11-21.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ArrayUtils {

    public static boolean isNotEmpty(String[] array) {
        return (array != null && array.length != 0);
    }

    public static boolean isNotEmpty(char[] array) {
        return (array != null && array.length != 0);
    }

    public static boolean isEmpty(boolean[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(String[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(char[] array) {
        return array == null || array.length == 0;
    }
}
