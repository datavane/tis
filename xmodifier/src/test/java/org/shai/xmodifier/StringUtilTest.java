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
package org.shai.xmodifier;

import org.junit.Assert;
import org.junit.Test;
import org.shai.xmodifier.util.Cons;
import org.shai.xmodifier.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Shenghai on 14-11-28.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
