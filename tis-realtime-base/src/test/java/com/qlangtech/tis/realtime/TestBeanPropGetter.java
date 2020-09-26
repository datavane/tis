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
package com.qlangtech.tis.realtime;

import com.google.common.collect.Sets;
import com.qlangtech.tis.common.utils.Assert;
import junit.framework.TestCase;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestBeanPropGetter extends TestCase {

    public void testGetProp() throws Exception {
        BeanInfo beaninfo = Introspector.getBeanInfo(User.class, Object.class);
        Set<String> names = Sets.newHashSet();
        for (PropertyDescriptor pdesc : beaninfo.getPropertyDescriptors()) {
            // System.out.println(pdesc.getName());
            names.add(pdesc.getName());
        }
        Assert.assertEquals(2, names.size());
        Assert.assertTrue(names.contains("name"));
        Assert.assertTrue(names.contains("age"));
    }

    private static class User {

        private String name;

        private String age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }
    }
}
