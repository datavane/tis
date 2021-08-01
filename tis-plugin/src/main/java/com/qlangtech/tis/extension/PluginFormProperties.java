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
package com.qlangtech.tis.extension;

import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.extension.impl.SuFormProperties;

import java.util.Map;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-12 11:01
 */
public abstract class PluginFormProperties {
    public abstract Set<Map.Entry<String, PropertyType>> getKVTuples();

    public abstract JSON getInstancePropsJson(Object instance);

    public abstract <T> T accept(IVisitor visitor);

    public interface IVisitor {
        default <T> T visit(RootFormProperties props) {
            return null;
        }

        default <T> T visit(SuFormProperties props) {
            return null;
        }
    }

}
