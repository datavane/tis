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
package com.qlangtech.tis.utils;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-18 15:39
 */
public class Utils {

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
            ww: while (true) {
                long offset = fileLength - (size++);
                if (offset < 0) {
                    randomAccess.seek(offset + 1);
                    break ww;
                }
                randomAccess.seek(offset);
                switch(// c =
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
                // listener.handle(line);
                lineProcess.print(line);
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
