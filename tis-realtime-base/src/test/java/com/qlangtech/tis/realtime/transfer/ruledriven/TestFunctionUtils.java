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
package com.qlangtech.tis.realtime.transfer.ruledriven;

import junit.framework.TestCase;
import java.util.List;
import static com.qlangtech.tis.realtime.transfer.ruledriven.FunctionUtils.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestFunctionUtils extends TestCase {

    public void testSplit() {
        List<String> split = split("20:32|已婚男士|15253653555|8f931168c2b44f7bb05e77e27286ce0c", "\\|");
        assertEquals("20:32", getArrayIndexProp(split, 0));
        assertEquals("已婚男士", getArrayIndexProp(split, 1));
        assertEquals("15253653555", getArrayIndexProp(split, 2));
        assertEquals("8f931168c2b44f7bb05e77e27286ce0c", getArrayIndexProp(split, 3));
    }

    public void testConcat_ws() {
        final String testKey = "testKey";
        // Set<Object> ids = Sets.newHashSet();
        // ids.add("8f931168c2b44f7bb05e77e27286ce0c");
        // ids.add("8f931168c2b44f7bb05e77e27286cccc");
        GroupValues gvals = new GroupValues();
        MediaData v1 = new MediaData();
        v1.put(testKey, "8f931168c2b44f7bb05e77e27286ce0c");
        MediaData v2 = new MediaData();
        v2.put(testKey, "8f931168c2b44f7bb05e77e27286cccc");
        gvals.vals.add(v1);
        gvals.vals.add(v2);
        String result = concat_ws(",", collect_set(gvals, (v) -> {
            return v.getColumn(testKey);
        }));
        assertEquals("8f931168c2b44f7bb05e77e27286ce0c,8f931168c2b44f7bb05e77e27286cccc", result);
        System.out.println(result);
    }
}
