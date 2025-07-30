/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin.rate;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.Duration;

/**
 * 指标采集配置
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-07-11 08:25
 **/
public class IndexCollectionConfig extends ParamsConfig {
    private static final String INDEX_COLLECTION_CONFIG = IndexCollectionConfig.class.getSimpleName();
    @FormField(ordinal = 0, identity = true, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.identity})
    public String name;

    @FormField(ordinal = 1, type = FormFieldType.DURATION_OF_SECOND, validate = {Validator.require})
    public Duration duration;

    public static int defaultDuration() {
        // 默认5秒采集一次,与flink.yaml 配置文件中的采样频率一致
        return 5;
    }

    @Override
    public boolean test(UploadPluginMeta uploadPluginMeta) {
        return StringUtils.equals(this.identityValue(), uploadPluginMeta.getPluginContext().getCollectionName().getPipelineName());
    }

    @Override
    public String getStoreGroup() {
        return super.getStoreGroup() + File.separator + this.identityValue();
    }

    public static IndexCollectionConfig getIndexCollectionConfig(DataXName dataXName) {
        return ParamsConfig.getItem(dataXName.getPipelineName(), INDEX_COLLECTION_CONFIG, false);
    }

    @Override
    public IndexCollectionConfig createConfigInstance() {
        return this;
    }

    @Override
    public String identityValue() {
        return name;
    }

    @TISExtension
    public static class DefaultDesc extends BasicParamsConfigDescriptor {
        static int minIntervalSec = 1;
        static int maxIntervalSec = 10;

        public DefaultDesc() {
            super(INDEX_COLLECTION_CONFIG);
        }

        @Override
        public String helpPath() {
            return "docs/guide/metric-collect-frequency";
        }

        public boolean validateDuration(IFieldErrorHandler msgHandler, Context context, String fieldName, String value) {
            int val = Integer.parseInt(value);

            if (val < minIntervalSec) {
                msgHandler.addFieldError(context, fieldName, "不能小于" + minIntervalSec + "秒");
                return false;
            }
            if (val > maxIntervalSec) {
                msgHandler.addFieldError(context, fieldName, "不能大于" + maxIntervalSec + "秒");
                return false;
            }
            return true;
        }

    }
}
