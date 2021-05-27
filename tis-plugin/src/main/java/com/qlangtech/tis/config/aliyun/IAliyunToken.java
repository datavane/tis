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
import com.qlangtech.tis.plugin.IdentityName;

import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IAliyunToken extends IdentityName {

    String KEY_FIELD_ALIYUN_TOKEN = "aliyunToken";

    public static IAliyunToken getToken(String endpoint) {
        IAliyunToken aliyunToken = ParamsConfig.getItem(endpoint, IAliyunToken.class);
        Objects.requireNonNull(aliyunToken, "aliyunToekn can not be null");
        return aliyunToken;
    }

    // private static String endpoint = "*** Provide OSS endpoint ***";
    // private static String accessKeyId = "*** Provide your AccessKeyId ***";
    // private static String accessKeySecret = "*** Provide your AccessKeySecret ***";
    // public String getEndpoint();
    String getAccessKeyId();

    String getAccessKeySecret();

    String getEndpoint();
}
