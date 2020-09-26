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
package com.qlangtech.tis;

import org.apache.commons.beanutils.BeanUtils;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestBeann extends TestCase {

    public void testBeanSet() throws Exception {
        TestBean bean = new TestBean();
        BeanUtils.setProperty(bean, "age", "1111d");
        System.out.println(bean.getAge());
    }

    public static void main(String[] args) {
    // System.out.println(
    // Test.class.getResource("org/objectweb/asm/ClassVisitor.class"));
    }
}
