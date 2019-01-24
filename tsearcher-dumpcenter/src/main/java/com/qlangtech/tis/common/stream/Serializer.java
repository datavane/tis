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
package com.qlangtech.tis.common.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Serializer {

    public static Object bytesToObject(byte[] bytes) {
        if (bytes == null)
            return null;
        ByteArrayInputStream bis = null;
        ObjectInputStream os = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            os = new ObjectInputStream(bis);
            return os.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (os != null)
                    os.close();
            } catch (Exception e) {
            }
            try {
                if (bis != null)
                    bis.close();
            } catch (Exception e) {
            }
        }
    }

    public static void writeObject(DataOutputStream out, Serializable obj) throws IOException {
        byte[] b = objectToBytes(obj);
        out.writeInt(b.length);
        out.write(b);
    }

    public static Object readObject(DataInputStream in) throws IOException {
        int size = in.readInt();
        byte[] b = new byte[size];
        in.readFully(b);
        return bytesToObject(b);
    }

    public static byte[] objectToBytes(Object object) {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream os = null;
        try {
            bos = new ByteArrayOutputStream();
            os = new ObjectOutputStream(bos);
            os.writeObject(object);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (os != null)
                    os.close();
            } catch (Exception e) {
            }
            try {
                if (bos != null)
                    bos.close();
            } catch (Exception e) {
            }
        }
    }
}
