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
package com.qlangtech.tis.solrdao;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-01-29 15:08
 */
public interface ISchemaPluginContext extends ISchemaFieldTypeContext {

    ISchemaPluginContext NULL = new ISchemaPluginContext() {
        @Override
        public <TYPE extends IFieldTypeFactory> List<TYPE> getFieldTypeFactories() {
            return null;
        }

        @Override
        public <TYPE extends IFieldTypeFactory> TYPE findFieldTypeFactory(String name) {
            return null;
        }

        @Override
        public boolean isTokenizer(String typeName) {
            return false;
        }
    };


    <TYPE extends IFieldTypeFactory> List<TYPE> getFieldTypeFactories();

    <TYPE extends IFieldTypeFactory> TYPE findFieldTypeFactory(String name);
}
