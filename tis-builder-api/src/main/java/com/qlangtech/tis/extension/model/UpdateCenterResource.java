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

package com.qlangtech.tis.extension.model;

import com.qlangtech.tis.utils.TisMetaProps;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-06-20 10:37
 **/
public class UpdateCenterResource {

    public static final String KEY_UPDATE_SITE = "/update-site";
    public static final String PREDEFINED_UPDATE_SITE_ID = "default";
    public static final String KEY_DEFAULT_JSON = "default.json";
    private static final MessageFormat UPDATE_CENTER_URL_FORMAT
            = new MessageFormat("http://mirror.qlangtech.com/{0}{1}/");
    public static final String UPDATE_CENTER_URL
            = UPDATE_CENTER_URL_FORMAT.format(new Object[]{TisMetaProps.getInstance().getVersion(), KEY_UPDATE_SITE});

    public static URL getTISReleaseRes(String ver, String subPath, String res) {
        try {
            return new URL(UPDATE_CENTER_URL_FORMAT.format(new Object[]{ver, subPath}) + res);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 取得 TIS的预置包
     *
     * @param ver
     * @param pkgName
     * @return
     */
    public static URL getTISTarPkg(String ver, String pkgName) {
        return getTISReleaseRes(ver, "/tis", pkgName);
    }
}
