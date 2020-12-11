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
package com.qlangtech.tis.util;

import com.qlangtech.tis.extension.Descriptor;
import java.util.List;

/**
 * 某些输入控件是一个Selectable的，需要生成可选项。 但是属性设置为S
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ISelectOptionsGetter {

    List<Descriptor.SelectOption> getSelectOptions(String name);
}
