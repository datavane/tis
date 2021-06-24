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

package com.qlangtech.tis.util.plugin;

import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-22 10:37
 **/
public class TestPluginImpl extends TestPlugin {

    @FormField(ordinal = 5, type = FormFieldType.INT_NUMBER, validate = {Validator.integer})
    public Integer connectionsPerHost;
    @FormField(ordinal = 6, type = FormFieldType.INT_NUMBER, validate = {Validator.integer})
    public Integer maxPendingPerConnection;

    @TISExtension
    public static final class DftDescriptor extends Descriptor<TestPlugin> {

    }
}
