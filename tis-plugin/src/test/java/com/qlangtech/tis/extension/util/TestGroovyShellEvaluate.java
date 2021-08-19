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

import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.ds.ReflectSchemaFieldType;
import junit.framework.TestCase;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-02-06 13:38
 */
public class TestGroovyShellEvaluate extends TestCase {

    public void testEval() {
        List<Option> fieldTyps = GroovyShellEvaluate.eval("com.qlangtech.tis.plugin.ds.ReflectSchemaFieldType.all()");
        assertNotNull(fieldTyps);
        List<Option> allOpts = ReflectSchemaFieldType.all();
        assertEquals(allOpts.size(), fieldTyps.size());
        int index = 0;
        Option actualOpt = null;
        for (Option o : allOpts) {
            actualOpt = fieldTyps.get(index++);
            assertEquals("index:" + index, o.getName() + "_" + o.getValue()
                    , actualOpt.getName() + "_" + actualOpt.getValue());
        }
    }
}
