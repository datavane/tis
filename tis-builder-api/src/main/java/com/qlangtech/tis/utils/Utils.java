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
package com.qlangtech.tis.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-18 15:39
 */
public class Utils {

    private static final Charset ios8859_1 = Charset.forName("ISO-8859-1");

    private Utils() {
    }

    /**
     * 打印文件的最后n行内容
     *
     * @param monitorFile
     * @param n
     * @param lineProcess
     */
    public static void readLastNLine(File monitorFile, int n, IProcessLine lineProcess) {
        if (!monitorFile.exists()) {
            return;
        }
        RandomAccessFile randomAccess = null;
        try {
            randomAccess = new RandomAccessFile(monitorFile, "r");
            // boolean eol = false;
            // int c = -1;
            long fileLength = randomAccess.length();
            long size = 1;
            boolean hasEncountReturn = false;
            ww:
            while (true) {
                long offset = fileLength - (size++);
                if (offset < 0) {
                    randomAccess.seek(offset + 1);
                    break ww;
                }
                randomAccess.seek(offset);
                switch (// c =
                        randomAccess.read()) {
                    case '\n':
                    case '\r':
                        if (!hasEncountReturn && (n--) <= 0) {
                            randomAccess.seek(offset + 1);
                            break ww;
                        }
                        hasEncountReturn = true;
                        continue;
                    default:
                        hasEncountReturn = false;
                }
            }
            String line = null;
            while ((line = randomAccess.readLine()) != null) {
                // 转换成中文不会有乱码
                lineProcess.print(new String(line.getBytes(ios8859_1), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // IOUtils.closeQuietly(randomAccess);
            try {
                randomAccess.close();
            } catch (Throwable e) {
            }
        }
    }

    public interface IProcessLine {

        void print(String line);
    }
}
