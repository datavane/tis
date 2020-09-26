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
package com.qlangtech.tis.pubhook.common;

import com.qlangtech.tis.manage.common.TisUTF8;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-2-15
 */
public class FileUtils {

    public static String readLastLine(File file) {
        RandomAccessFile randomAccess = null;
        try {
            randomAccess = new RandomAccessFile(file, "r");
            boolean eol = false;
            int c = -1;
            long fileLength = randomAccess.length();
            long size = 1;
            ww: while (!eol) {
                long offset = fileLength - (size++);
                randomAccess.seek(offset);
                switch(c = randomAccess.read()) {
                    case -1:
                    case '\n':
                    case '\r':
                        randomAccess.seek(offset + 1);
                        break ww;
                }
            }
            return randomAccess.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                randomAccess.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 在文件最后追加一行记录
     *
     * @param file
     * @param line
     */
    public static void append(File file, String line) {
        try {
            org.apache.commons.io.FileUtils.write(file, ("\r\n" + line), TisUTF8.get(), true);
        } catch (IOException e) {
            throw new RuntimeException(file.getAbsolutePath(), e);
        }
    // OutputStream wirter = null;
    // try {
    // 
    // wirter = new FileOutputStream(file, true);
    // wirter.write(("\r\n" + line).getBytes());
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // try {
    // wirter.close();
    // } catch (IOException e) {
    // }
    // }
    }

    public static void main(String[] arg) {
        append(new File("D:\\tmp\\test.txt"), "11");
    }
}
