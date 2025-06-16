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

package com.qlangtech.tis.plugin.timezone;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-06-16 16:14
 **/
public class CustomizeTISTimeZone extends TISTimeZone {

    private static final Logger logger = LoggerFactory.getLogger(CustomizeTISTimeZone.class);
    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String timeZone;

    @Override
    public ZoneId getTimeZone() {
        return ZoneId.of(this.timeZone);
    }

    @TISExtension
    public static class Desc extends Descriptor<TISTimeZone> {
        public Desc() {
            super();
        }

        public boolean validateTimeZone(IFieldErrorHandler msgHandler, Context context, String fieldName, String value) {

            try {
                ZoneId.of(value);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                msgHandler.addFieldError(context, fieldName, e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        public String getDisplayName() {
            return "customize";
        }
    }
}
