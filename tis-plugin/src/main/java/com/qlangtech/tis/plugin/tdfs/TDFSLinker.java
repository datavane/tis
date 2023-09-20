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

package com.qlangtech.tis.plugin.tdfs;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtensible;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;

import java.util.List;

/**
 * 抽象各种资源类型，例如：FTP，OSS，HDFS 等
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-08-03 22:34
 **/
@TISExtensible
public abstract class TDFSLinker implements Describable<TDFSLinker> {
    public static final String KEY_FTP_SERVER_LINK = "linker";
    protected static final String KEY_FIELD_PATH = "path";
    @FormField(ordinal = 1, type = FormFieldType.SELECTABLE, validate = {Validator.require})
    public String linker;

    @FormField(ordinal = 6, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.absolute_path})
    public String path;

    public String getRootPath() {
        return path;
    }

    public abstract ITDFSSession createTdfsSession(Integer timeout);

    public abstract ITDFSSession createTdfsSession();

    public abstract <T> T useTdfsSession(TDFSSessionVisitor<T> tdfsSession);

    @Override
    public final Descriptor<TDFSLinker> getDescriptor() {
        Descriptor<TDFSLinker> descriptor = TIS.get().getDescriptor(this.getClass());
        if (!BasicDescriptor.class.isAssignableFrom(descriptor.getClass())) {
            throw new IllegalStateException("desc :" + descriptor.getClass() + " must inherited from " + BasicDescriptor.class.getName());
        }
        return descriptor;
    }

    protected static abstract class BasicDescriptor extends Descriptor<TDFSLinker> {
        public BasicDescriptor() {
            super();
            this.registerSelectOptions(KEY_FTP_SERVER_LINK, () -> createRefLinkers());
        }

        protected abstract List<? extends IdentityName> createRefLinkers();

        public abstract boolean validateLinker(
                IFieldErrorHandler msgHandler, Context context, String fieldName, String linker);
    }
}
