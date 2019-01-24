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
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FirstCharacterFieldHelper {

    public static boolean isCharOrNum(char a) {
        int v = a;
        boolean isNum = (v >= 48) && (v <= 57);
        boolean isHighCase = (v >= 65) && (v <= 90);
        boolean isLowCase = (v >= 97) && (v <= 122);
        return isNum || isHighCase || isLowCase;
    }

    public static boolean isChinese(char a) {
        int v = a;
        return (v >= 19968) && (v <= 171941);
    }

    public static int processInt(int old, char fc, int i, int BitsPerValue) {
        int position = (FirstCharacterField.NumOfValue - i - 1) * BitsPerValue;
        /**
         * a~z : 1~26  0~9:27~36 特殊字符:42
         */
        int num = (fc >= '0' && fc <= '9') ? (fc - '0' + 27) : (fc - 'a' + 1);
        old = (num << position) | old;
        return old;
    }

    public static void main(String[] args) {
        System.out.println(processInt(0, '9', 1, 5));
    }
}
