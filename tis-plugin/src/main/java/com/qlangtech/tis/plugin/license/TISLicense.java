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

package com.qlangtech.tis.plugin.license;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.datax.TimeFormat;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.lang.ErrorValue;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.lang.TisException.ErrorCode;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore.AfterPluginSaved;
import com.qlangtech.tis.plugin.IPluginStore.BeforePluginSaved;
import com.qlangtech.tis.plugin.IdentityDesc;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.license.TISLicense.HasExpire;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-11-14 18:39
 **/
public class TISLicense extends ParamsConfig implements BeforePluginSaved, AfterPluginSaved, IdentityDesc<HasExpire> {

    public static final String key_tis_license = "tis_license";
    private transient Pair<PublicKey, String> publicKey;
    private transient HasExpire hasExpire;
    public static final String KEY_EXPIRE_DATE = "expireDatetime";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_MOBILE = "mobile";
    public static final String KEY_IDENTITY = "licenseId";
    public static final String KEY_TIS_IDENTITY = "tis-license-id" ;
    public static final String KEY_FIELD_ACTIVATION_CODE = "activationCode";
    public static final String KEY_DISPLAY_NAME = "License";

    public static TISLicense load() {
        return load(true);
    }

    /**
     * 加载实例
     *
     * @return
     */
    public static TISLicense load(boolean validateNull) {
        return ParamsConfig.getItem(key_tis_license, KEY_DISPLAY_NAME, Optional.empty(), validateNull);
    }


    @FormField(identity = true, type = FormFieldType.INPUTTEXT, ordinal = 0, validate = {})
    public String expireDate;


    /**
     * 激活码
     */
    @FormField(type = FormFieldType.TEXTAREA, ordinal = 4, validate = {Validator.require})
    public String activationCode;

    @FormField(type = FormFieldType.INPUTTEXT, advance = true, ordinal = 1, validate = {})
    public String licenseId;

    @FormField(type = FormFieldType.INPUTTEXT, advance = true, ordinal = 2, validate = {})
    public String email;

    @FormField(type = FormFieldType.INPUTTEXT, advance = true, ordinal = 3, validate = {})
    public String mobile;

    @Override
    public void beforeSaved(IPluginContext pluginContext, Optional<Context> context) {
        HasExpire hasExpire = null;
        try {
            hasExpire = hasExpire();
            this.expireDate = hasExpire.expireDate;
            this.email = hasExpire.getEmail();
            this.mobile = hasExpire.getMobile();
            this.licenseId = hasExpire.getLicenseId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (hasExpire != null && !hasExpire.hasNotExpire) {
            throw TisException.create(ErrorValue.create(ErrorCode.LICENSE_INVALID, new HashMap<>()), "License period "
                    + "of validity till:" + hasExpire.expireDate);
        }
    }

    @Override
    public void afterSaved(IPluginContext pluginContext, Optional<Context> context) {
        this.publicKey = null;
        this.hasExpire = null;
    }

    public final Pair<PublicKey, String> loadPublicKey() {
        try {
            if (this.publicKey == null) {
                String publicKey = IOUtils.loadResourceFromClasspath(TISLicense.class, "public.key");
                byte[] decodedPublicKey = Base64.getDecoder().decode(publicKey);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedPublicKey);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.publicKey = Pair.of(keyFactory.generatePublic(keySpec), publicKey);
            }
            return this.publicKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HasExpire describePlugin() {
        try {
            return hasExpire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 是否实效过期？
     *
     * @return
     */
    public HasExpire hasExpire() throws Exception {

        if (hasExpire == null) {
            final byte[] content = decryptData(Base64.getDecoder().decode(this.activationCode),
                    this.loadPublicKey().getKey());
            HashMap<String, String> deserialize = JsonUtil.deserialize(new String(content, TisUTF8.get()),
                    new HashMap<String, String>());
            long epochMilli = Long.parseLong(deserialize.get(KEY_EXPIRE_DATE));
            final TimeFormat format = TimeFormat.yyyy_MM_dd;
            hasExpire = new HasExpire(LocalDate.now().isBefore(format.ofInstant(epochMilli).toLocalDate()),
                    format.format(epochMilli));
            hasExpire.setEmail(deserialize.get(KEY_EMAIL));
            hasExpire.setMobile(deserialize.get(KEY_MOBILE));
            hasExpire.setLicenseId(deserialize.get(KEY_IDENTITY));
        }
        return hasExpire;
    }


    public static class HasExpire {
        public final boolean hasNotExpire;
        public final String expireDate;
        private String licenseId;
        /**
         * 手机
         */
        private String mobile;
        /**
         * 邮箱
         */
        private String email;

        public HasExpire(boolean hasNotExpire, String expireDate) {
            this.hasNotExpire = hasNotExpire;
            this.expireDate = expireDate;
        }

        public void setLicenseId(String licenseId) {
            this.licenseId = licenseId;
        }

        public String getMobile() {
            return this.mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getEmail() {
            return this.email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isHasNotExpire() {
            return this.hasNotExpire;
        }

        public String getExpireDate() {
            return expireDate;
        }

        public String getLicenseId() {
            return this.licenseId;
        }
    }

    //    private PublicKey getPublicKey() throws Exception {
    //        byte[] decodedPublicKey = this.loadPublicKey();
    //        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedPublicKey);
    //        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    //        return keyFactory.generatePublic(keySpec);
    //    }

    private static byte[] decryptData(byte[] encryptedData, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(encryptedData);
    }

    @Override
    public TISLicense createConfigInstance() {
        return this;
    }

    @Override
    public String identityValue() {
        return key_tis_license;
    }

    @TISExtension
    public static final class DftDescriptor extends BasicParamsConfigDescriptor implements IEndTypeGetter {
        public DftDescriptor() {
            super(KEY_DISPLAY_NAME);
        }

        @Override
        public EndType getEndType() {
            return EndType.License;
        }

        @Override
        public String getDisplayName() {
            return paramsConfigType();
        }

        @Override
        public String helpPath() {
            return "community-collaboration";
        }

        @Override
        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            TISLicense license = postFormVals.newInstance();
            try {
                HasExpire hasExpire = license.hasExpire();
                if (!hasExpire.hasNotExpire) {
                    // 实效了
                    msgHandler.addFieldError(context, KEY_FIELD_ACTIVATION_CODE, "证书已失效，有效期到：" + hasExpire.expireDate);
                    return false;
                } else {
                    msgHandler.addActionMessage(context, "证书有效，有效期到：" + hasExpire.expireDate);
                }
            } catch (Exception e) {
                throw TisException.create("请检查是否是合法证书，" + e.getMessage(), e);
            }
            return true;
        }

        @Override
        protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            return this.validateAll(msgHandler, context, postFormVals);
        }
    }

}
