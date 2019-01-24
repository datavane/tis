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
package com.qlangtech.tis.manage.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Secret {

    private static final int KEY_LENGTH = 24;

    private static final String ENCODING = "UTF-8";

    private static final String Algorithm = "DESede";

    private static final Base64 base64 = new Base64();

    public static String base64Encode(String plaintext) throws UnsupportedEncodingException {
        if (plaintext == null) {
            return null;
        }
        byte[] plaintextBytes = plaintext.getBytes(ENCODING);
        return new String(base64.encode(plaintextBytes));
    }

    public static String base64Decode(String cipherText) throws IOException {
        return new String(base64.decode(cipherText));
    }

    public static String encrypt(String src, String cryptKey) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        // cryptKey = base64Decode(cryptKey);
        SecretKey desKey = new SecretKeySpec(build3DesKey(base64Decode(cryptKey)), Algorithm);
        Cipher c1 = Cipher.getInstance(Algorithm);
        c1.init(Cipher.ENCRYPT_MODE, desKey);
        byte[] cryptBytes = c1.doFinal(src.getBytes(ENCODING));
        return new String(base64.encode(cryptBytes));
    }

    public static String decrypt(String src, String cryptKey) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cryptKey = base64Decode(cryptKey);
        SecretKey desKey = new SecretKeySpec(build3DesKey(cryptKey), Algorithm);
        Cipher c1 = Cipher.getInstance(Algorithm);
        c1.init(Cipher.DECRYPT_MODE, desKey);
        byte[] cryptBytes = base64.decode(src);
        return new String(c1.doFinal(cryptBytes));
    }

    public static byte[] build3DesKey(String keyStr) throws UnsupportedEncodingException {
        byte[] key = new byte[KEY_LENGTH];
        byte[] temp = keyStr.getBytes(ENCODING);
        if (key.length > temp.length) {
        } else {
        }
        return key;
    }
}
