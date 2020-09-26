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
package com.qlangtech.tis.plugin;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-03 11:30
 */
public class TestPlugin implements Describable<TestPlugin> {

    public String prop1;

    public String prop2;

    @Override
    public Descriptor<TestPlugin> getDescriptor() {
        return TIS.get().getDescriptor(TestPlugin.class);
    }

    @TISExtension
    public static class DefaultImpl extends Descriptor<TestPlugin> {

        public DefaultImpl() {
        }
    }
}
