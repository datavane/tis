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
package com.qlangtech.tis.util;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Util {

    /**
     * Convert null to "".
     */
    public static String fixNull(String s) {
        if (s == null)
            return "";
        else
            return s;
    }

    public static String join(Collection<?> strings, String separator) {
        return strings.stream().map((r) -> ((Object) r).toString()).collect(Collectors.joining(separator));
    }

    /**
     * Combines all the given collections into a single list.
     */
    public static <T> List<T> join(Collection<? extends T>... items) {
        int size = 0;
        for (Collection<? extends T> item : items) size += item.size();
        List<T> r = new ArrayList<T>(size);
        for (Collection<? extends T> item : items) r.addAll(item);
        return r;
    }

    public static void deleteRecursive(File destDir) {
        try {
            FileUtils.forceDelete(destDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
