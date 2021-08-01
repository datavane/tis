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

package com.qlangtech.tis.plugin;

import junit.framework.TestCase;

import java.util.regex.Matcher;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-07-01 13:16
 **/
public class TestValidatorCommons extends TestCase implements ValidatorCommons {

    public void testPATTERN_ABSOLUTE_PATH() {
        Matcher matcher = PATTERN_ABSOLUTE_PATH.matcher("/home/hanfa.shf/ftpReaderTest/data");
        assertTrue("must match", matcher.matches());
    }
}
