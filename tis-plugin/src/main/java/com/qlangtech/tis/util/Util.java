/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.util;

//import edu.umd.cs.findbugs.annotations.CheckForNull;
//import edu.umd.cs.findbugs.annotations.Nullable;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
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
     * Converts a {@link File} into a {@link Path} and checks runtime exceptions.
     *
     * @throws IOException if {@code f.toPath()} throws {@link InvalidPathException}.
     */
    public static Path fileToPath(File file) throws IOException {
        try {
            return file.toPath();
        } catch (InvalidPathException e) {
            throw new IOException(e);
        }
    }

    /**
     * Returns a file name by changing its extension.
     *
     * @param ext For example, ".zip"
     */
    public static File changeExtension(File dst, String ext) {
        String p = dst.getPath();
        int pos = p.lastIndexOf('.');
        if (pos < 0) return new File(p + ext);
        else return new File(p.substring(0, pos) + ext);
    }

    /**
     * Convert empty string to null, and trim whitespace.
     *
     * @since 1.154
     */
    public static String fixEmptyAndTrim(String s) {
        if (s == null) return null;
        return fixEmpty(s.trim());
    }

    /**
     * Convert empty string to null.
     */
    public static String fixEmpty(String s) {
        if (s == null || s.length() == 0) return null;
        return s;
    }


    /**
     * Convert null to "".
     */
    public static String fixNull(String s) {
        if (s == null)
            return "";
        else
            return s;
    }

    /**
     * Null-safe String intern method.
     *
     * @return A canonical representation for the string object. Null for null input strings
     */

    public static String intern(String s) {
        return s == null ? s : s.intern();
    }

    public static String join(Collection<?> strings, String separator) {
        return strings.stream().map((r) -> r.toString()).collect(Collectors.joining(separator));
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
