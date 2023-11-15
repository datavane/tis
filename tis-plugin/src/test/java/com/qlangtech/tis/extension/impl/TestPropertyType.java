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

package com.qlangtech.tis.extension.impl;

import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.extension.DefaultPlugin;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.RequiredPasswordPlugin;
import com.qlangtech.tis.plugin.annotation.Validator;
import junit.framework.TestCase;

import java.util.Map;
import java.util.Optional;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/10/19
 */
public class TestPropertyType extends TestCase {

    public void testGetValidator() {
        Map<String, /*** fieldname */IPropertyType> props
                = PropertyType.buildPropertyTypes(Optional.empty(), DefaultPlugin.class);

        PropertyType passwordPropery = (PropertyType) props.get("password");
        Assert.assertNotNull(passwordPropery);

        Validator[] validators = passwordPropery.getValidator();
        // contain 2 validator 'require' and 'none_blank'
        Assert.assertEquals(2, validators.length);
        boolean containRequire = false;
        for (Validator v : validators) {
            if (v == Validator.require) {
                containRequire = true;
            }
        }
        Assert.assertTrue("shall containRequire", containRequire);
    }

    public void testGetValidatorDisableRequireValidatorByJsonConfig() {
        Map<String, /*** fieldname */IPropertyType> props
                = PropertyType.buildPropertyTypes(Optional.empty(), RequiredPasswordPlugin.class);
        PropertyType passwordPropery = (PropertyType) props.get("password");
        Assert.assertNotNull(passwordPropery);
        Validator[] validators = passwordPropery.getValidator();
        // contain 2 validator 'require' and 'none_blank'
        Assert.assertEquals(1, validators.length);
        boolean containRequire = false;
        for (Validator v : validators) {
            if (v == Validator.require) {
                containRequire = true;
            }
        }
        Assert.assertFalse("shall not containRequire", containRequire);
    }
}
