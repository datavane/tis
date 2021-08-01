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

import com.qlangtech.tis.extension.Describable;

/**
 * The plugin global unique identity name
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IdentityName {

    String MSG_ERROR_NAME_DUPLICATE = "名称重复";

//    /**
//     * 相同类型的插件不能重名
//     *
//     * @return
//     */
//    String getName();

    /**
     * 取得唯一ID
     *
     * @return
     */
    default String identityValue() {
        Describable plugin = (Describable) this;
        return plugin.getDescriptor().getIdentityValue(plugin);
    }

}
