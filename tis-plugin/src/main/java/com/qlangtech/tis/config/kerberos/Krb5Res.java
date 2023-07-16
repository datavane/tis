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

package com.qlangtech.tis.config.kerberos;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.InnerPropOfIdentityName;
import com.qlangtech.tis.realtime.utils.NetUtils;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.function.Consumer;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-07-03 15:53
 **/
public abstract class Krb5Res implements Describable<Krb5Res>, InnerPropOfIdentityName {
    public abstract File getKrb5Path();

    public abstract boolean isKrb5PathNotNull();

    protected transient IdentityName parentPluginId;

    @Override
    public void setIdentity(IdentityName id) {
        this.parentPluginId = id;
    }

    @Override
    public final Descriptor<Krb5Res> getDescriptor() {
        Descriptor descriptor = TIS.get().getDescriptor(this.getClass());
        if (!(descriptor instanceof BaseDescriptor)) {
            throw new IllegalStateException("descriptor class:"
                    + descriptor.getClass().getName() + " must be type of " + BaseDescriptor.class.getName());
        }
        return descriptor;
    }

    public static abstract class BaseDescriptor extends Descriptor<Krb5Res> {
        static final String KEY_KRB5_CONFIG = "java.security.krb5.conf";
        private static final Logger logger = LoggerFactory.getLogger(BaseDescriptor.class);

        protected abstract String getResPropFieldName();

        @Override
        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            Krb5Res krb5Res = postFormVals.newInstance(this, msgHandler);
            if (krb5Res.isKrb5PathNotNull()) {
                File krb5Path = (krb5Res.getKrb5Path());
                if (!krb5Path.exists()) {
                    msgHandler.addFieldError(context, getResPropFieldName()
                            , NetUtils.getHostname() + "节点中，文件文件：" + krb5Path.getAbsolutePath() + "不存在该路径");
                    return false;
                }

                if (!krb5ConfigTmpSession(krb5Res, () -> {
                    sun.security.krb5.Config.refresh();
                    return true;
                }, (e) -> {
                    msgHandler.addFieldError(context, getResPropFieldName(), e.getMessage());
                })) {
                    return false;
                }
            }

            return true;
        }

        public static <T> T krb5ConfigTmpSession(Krb5Res krb5Res, Krb5Provider<T> process, Consumer<Exception>... errProcs) {
            String krb5Config = System.getProperty(KEY_KRB5_CONFIG);
            File krb5Path = krb5Res.getKrb5Path();
            if (!krb5Path.exists()) {
                throw new IllegalStateException("krb5Path is not exist:" + krb5Path.getAbsolutePath());
            }
            try {
                System.setProperty(KEY_KRB5_CONFIG, krb5Path.getAbsolutePath());
                return process.run();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                try {
                    for (Consumer<Exception> errProcess : errProcs) {
                        errProcess.accept(e);
                        break;
                    }
                } catch (Exception ex) {

                }
                return null;
            } finally {
                if (StringUtils.isNotBlank(krb5Config)) {
                    System.setProperty(KEY_KRB5_CONFIG, krb5Config);
                } else {
                    System.clearProperty(KEY_KRB5_CONFIG);
                }
            }
        }


        @FunctionalInterface
        public interface Krb5Provider<T> {
            /**
             * When an object implementing interface <code>Runnable</code> is used
             * to create a thread, starting the thread causes the object's
             * <code>run</code> method to be called in that separately executing
             * thread.
             * <p>
             * The general contract of the method <code>run</code> is that it may
             * take any action whatsoever.
             *
             * @see java.lang.Thread#run()
             */
            public abstract T run() throws Exception;
        }

    }
}
