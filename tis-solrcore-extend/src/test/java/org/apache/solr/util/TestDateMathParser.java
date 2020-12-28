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
package org.apache.solr.util;

import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-12-28 11:22
 */
public class TestDateMathParser extends TestCase {

    public void test() {



        System.out.println(DateMathParser.parseMath(null, "2020-11-09T12:22:12.000Z"));
        System.out.println(DateMathParser.parseMath(null, "2020-11-09T00:00:00Z"));
    }
}
