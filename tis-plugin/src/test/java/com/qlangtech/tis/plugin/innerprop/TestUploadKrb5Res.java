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

import com.qlangtech.tis.config.kerberos.Krb5Res;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.ITmpFileStore;
import com.qlangtech.tis.plugin.annotation.Validator;

import java.io.File;
import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-07-04 13:59
 **/
public class TestUploadKrb5Res extends Krb5Res implements ITmpFileStore {
    private static final String KEY_FILE = "file";
    @FormField(ordinal = 1, type = FormFieldType.FILE, validate = {Validator.require})
    public String file;

    public IdentityName getParentPluginId() {
        return this.parentPluginId;
    }

    @Override
    public boolean isKrb5PathNotNull() {
        return true;
    }

    @Override
    public File getKrb5Path() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTmpeFile(TmpFile tmp) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStoreFileName() {
        return Objects.requireNonNull(
                this.parentPluginId, "prop parentPluginId can not be null")
                .identityValue() + "_" + this.file;
    }

    @Override
    public void save(File parentDir) {
    }

    @TISExtension
    public static final class DftDescriptor extends BaseDescriptor {

        @Override
        public String getDisplayName() {
            return "Upload";
        }

        @Override
        protected String getResPropFieldName() {
            return KEY_FILE;
        }

    }

}
