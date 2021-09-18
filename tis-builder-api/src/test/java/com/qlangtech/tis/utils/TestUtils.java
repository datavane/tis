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

package com.qlangtech.tis.utils;

import junit.framework.TestCase;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-09-18 17:04
 **/
public class TestUtils extends TestCase {
    public void testReadLastUtf8Lines() {
        File monitorFile = new File("./src/test/resources/com/qlangtech/tis/utils/monitorFile.txt");
        assertTrue(monitorFile.exists());
        Set<String> chars = new HashSet<>();
        chars.add("吃");
        chars.add("喝");
        chars.add("玩");
        chars.add("乐");
        Utils.readLastNLine(monitorFile, 10, (line) -> {
            chars.remove(line);
        });
        assertTrue(chars.isEmpty());
    }
}
