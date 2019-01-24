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
package org.shai.xmodifier;

import org.junit.Assert;
import org.junit.Test;
import org.shai.xmodifier.util.Cons;
import org.shai.xmodifier.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Created by Shenghai on 14-11-28.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class StringUtilTest {

    @Test
    public void splitBySeparator() {
        String s = "ns:root//ns:element1/ns:element11";
        String[] strings = StringUtils.splitBySeparator(s, new String[] { "/", "//" }, new char[][] { { '\'', '\'' }, { '[', ']' }, { '(', ')' } }, true);
        Assert.assertEquals("[ns:root, //ns:element1, /ns:element11]", Arrays.toString(strings));
    }

    @Test
    public void splitBySeparator2() {
        String s = "@attr=1";
        String[] strings = StringUtils.splitBySeparator(s, new String[] { "/", "//" }, new char[][] { { '\'', '\'' }, { '[', ']' }, { '(', ')' } }, true);
        Assert.assertEquals("[@attr=1]", Arrays.toString(strings));
    }

    @Test
    public void findQuotingString() {
        String s = "aad(:xxx(yy[xxx)yy))eee";
        List<Cons<String, String>> escapeList = new ArrayList<Cons<String, String>>();
        escapeList.add(new Cons<String, String>("(", ")"));
        escapeList.add(new Cons<String, String>("[", ")"));
        Cons<String, String> result = StringUtils.findFirstQuotingString(s, new Cons<String, String>("(:", ")"), escapeList);
        System.out.println("result = " + result);
    }

    @Test
    public void removeQuotingString() {
        String s = "adfd(:lkjkl(kjlkj))lkjflkds(:lkfjlksdj(ldkj))lkjfdslj";
        List<Cons<String, String>> escapeList = new ArrayList<Cons<String, String>>();
        escapeList.add(new Cons<String, String>("(", ")"));
        escapeList.add(new Cons<String, String>("[", ")"));
        System.out.println(StringUtils.removeQuotingString(s, new Cons<String, String>("(:", ")"), escapeList));
    }
}
