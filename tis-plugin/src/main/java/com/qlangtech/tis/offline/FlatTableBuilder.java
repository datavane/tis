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
package com.qlangtech.tis.offline;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.DescriptorExtensionList;
import com.qlangtech.tis.fs.IFs2Table;
import com.qlangtech.tis.fullbuild.taskflow.IFlatTableBuilder;
import com.qlangtech.tis.fullbuild.taskflow.ITaskFactory;
import com.qlangtech.tis.fullbuild.taskflow.ITemplateContext;
import com.qlangtech.tis.plugin.IdentityName;

/**
 * 宽表构建插件
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class FlatTableBuilder implements Describable<FlatTableBuilder>, IFlatTableBuilder, IdentityName {

    @Override
    public Descriptor<FlatTableBuilder> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }
    // public static DescriptorExtensionList<FlatTableBuilder, Descriptor<FlatTableBuilder>> all() {
    // return TIS.get().getDescriptorList(FlatTableBuilder.class);
    // }
}
