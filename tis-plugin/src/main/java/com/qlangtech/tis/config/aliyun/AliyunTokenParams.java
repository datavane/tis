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
package com.qlangtech.tis.config.aliyun;

import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.Validator;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class AliyunTokenParams extends ParamsConfig implements IAliyunToken {

    @FormField(ordinal = 0, validate = { Validator.require, Validator.identity })
    public String name;

    // @FormField(ordinal = 1, validate = {Validator.require})
    // public String endpoint;
    @FormField(ordinal = 2, validate = { Validator.require })
    public String accessKeyId;

    @FormField(ordinal = 3, validate = { Validator.require })
    public String accessKeySecret;

    @Override
    public Object createConfigInstance() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    // @Override
    // public String getEndpoint() {
    // return this.endpoint;
    // }
    @Override
    public String getAccessKeyId() {
        return this.accessKeyId;
    }

    @Override
    public String getAccessKeySecret() {
        return this.accessKeySecret;
    }

    @TISExtension()
    public static class DefaultDescriptor extends Descriptor<ParamsConfig> {

        @Override
        public String getDisplayName() {
            return "aliyun-token";
        }
    }
}
