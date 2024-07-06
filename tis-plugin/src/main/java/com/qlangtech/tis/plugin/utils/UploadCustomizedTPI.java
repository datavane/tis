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

package com.qlangtech.tis.plugin.utils;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.PluginManager;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.impl.PluginManifest;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.maven.plugins.tpi.PluginClassifier;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.plugin.IPluginStore.AfterPluginSaved;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.ITmpFileStore;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.PluginMeta;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

/**
 * 用户上传自定义插件包
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-05 13:26
 **/
public class UploadCustomizedTPI implements Describable<UploadCustomizedTPI>, ITmpFileStore, AfterPluginSaved {
    private static final Logger logger = LoggerFactory.getLogger(UploadCustomizedTPI.class);
    private static final String KEY_FILE = "file";

    @FormField(ordinal = 1, type = FormFieldType.FILE, validate = {Validator.require})
    public String file;

    /**
     * 如果已经存在有相同的文件是否将其替换
     */
    @FormField(ordinal = 2, type = FormFieldType.ENUM, validate = {Validator.require})
    public Boolean forceReplace;

    private transient TmpFile tmp;

    @Override
    public void setTmpeFile(TmpFile tmp) {
        this.tmp = tmp;
    }

    @Override
    public TmpFile getTmpeFile() {
        return this.tmp;
    }

    @Override
    public String getStoreFileName() {
        return this.file;
    }

    private PluginMeta getPluginMeta() {
        TmpFile tmpFile = getTmpeFile();
        PluginManifest manifest = PluginManifest.create(tmpFile.tmp);
        PluginMeta pluginMeta = manifest.getPluginMeta();
        return pluginMeta;
    }

    @Override
    public void afterSaved(IPluginContext pluginContext, Optional<Context> context) {
        PluginManager pm = TIS.get().getPluginManager();
        TmpFile tmpFile = getTmpeFile();
//        PluginManifest manifest = PluginManifest.create(tmpFile.tmp);
        PluginMeta pluginMeta = this.getPluginMeta();
        if (StringUtils.isEmpty(pluginMeta.getPluginName())) {
            throw new IllegalStateException(this.getStoreFileName() + " is not valid plugin");
        }
        try {
            File targetTpi = new File(TIS.pluginDirRoot, this.getStoreFileName());
            if (targetTpi.exists()) {
                if (!forceReplace) {
                    throw TisException.create("插件包" + this.getStoreFileName() + "已经存在，如需强制替换插件包，请设置‘强制替换’为‘是’");
                }
                // 已经存在
                FileUtils.moveFile(targetTpi, new File(TIS.pluginDirRoot, this.getStoreFileName() + "." + IParamContext.getCurrentMillisecTimeStamp()));
                FileUtils.moveFile(tmpFile.tmp, targetTpi);
                pluginContext.addActionMessage(context.get(), "替换已有插件包" + this.getStoreFileName() + "，请重启TIS生效");
            } else {

                pm.dynamicLoad(tmpFile.tmp, false, null);
                pluginContext.addActionMessage(context.get(), "已成功加载" + this.getStoreFileName() + "，已生效");

            }
        } catch (Throwable e) {
            throw TisException.create(e.getMessage(), e);
        }
    }

    @TISExtension
    public static class DefaultDescriptor extends Descriptor<UploadCustomizedTPI> {
        public DefaultDescriptor() {
            super();
        }

        @Override
        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            UploadCustomizedTPI customizedTPI = postFormVals.newInstance();
            String errMsg = customizedTPI.getStoreFileName() + " 不是符合规范的" + PluginClassifier.PACAKGE_TPI_EXTENSION_NAME + "文件";
            try {
                PluginMeta pluginMeta = customizedTPI.getPluginMeta();
                if (StringUtils.isEmpty(pluginMeta.getPluginName())) {
                    msgHandler.addFieldError(context, KEY_FILE, errMsg);
                    return false;
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                msgHandler.addFieldError(context, KEY_FILE, errMsg);
                return false;
            }
            return true;
        }
    }

}
