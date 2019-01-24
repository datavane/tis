/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.pubhook.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
        OutputStream wirter = null;
        try {
            wirter = new FileOutputStream(file, true);
            wirter.write(("\r\n" + line).getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                wirter.close();
            } catch (IOException e) {
            }
        }
    }

    public static void main(String[] arg) {
        append(new File("D:\\tmp\\test.txt"), "11");
    }
}
