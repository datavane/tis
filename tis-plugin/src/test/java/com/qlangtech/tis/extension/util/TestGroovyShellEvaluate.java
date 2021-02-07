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
package com.qlangtech.tis.extension.util;

import junit.framework.TestCase;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-02-06 13:38
 */
public class TestGroovyShellEvaluate extends TestCase {
    public void testEval() {
        List<String> fieldTyps = GroovyShellEvaluate.eval("com.qlangtech.tis.plugin.ds.ReflectSchemaFieldType.all()");
        assertNotNull(fieldTyps);

        assertTrue(CollectionUtils.isEqualCollection(com.qlangtech.tis.plugin.ds.ReflectSchemaFieldType.all(), fieldTyps));
    }
}
