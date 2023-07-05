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

package com.qlangtech.tis.plugin.innerprop;

import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.config.kerberos.IKerberos;
import com.qlangtech.tis.config.kerberos.Krb5Res;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.Validator;

import java.io.File;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-07-04 13:56
 **/
public class KerberosCfg extends ParamsConfig implements IKerberos {

    @FormField(identity = true, ordinal = 0, validate = {Validator.require, Validator.identity})
    public String name;


    @FormField(ordinal = 3, validate = {Validator.require})
    public Krb5Res krb5Res;


    public Krb5Res getKrb5Res() {
        return this.krb5Res;
    }

    @Override
    public String getPrincipal() {
       throw new UnsupportedOperationException();
    }

    @Override
    public String getKeytabPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getKeyTabPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object createConfigInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String identityValue() {
        return this.name;
    }


    @TISExtension
    public static class DefaultDescriptor extends Descriptor<ParamsConfig> {
        @Override
        public String getDisplayName() {
            return IKerberos.IDENTITY;
        }
    }

}
