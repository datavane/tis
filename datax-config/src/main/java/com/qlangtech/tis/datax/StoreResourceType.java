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

package com.qlangtech.tis.datax;


import org.apache.commons.lang3.StringUtils;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-02-08 11:08
 **/
public enum StoreResourceType {

    DataBase(StoreResourceTypeConstants.DB_GROUP_NAME, false, StringUtils.EMPTY) //
    , DataApp(StoreResourceTypeConstants.NAME_APP_DIR, false, StoreResourceTypeConstants.DEFAULT_DATAX_PROCESSOR_NAME) //
    , DataFlow(StoreResourceTypeConstants.NAME_DATAFLOW_DIR, true, StoreResourceTypeConstants.DEFAULT_WORKFLOW_PROCESSOR_NAME);

    public static final String KEY_STORE_RESOURCE_TYPE = "storeResType";
    public static final String KEY_PROCESS_MODEL = "processModel";
    public static final String DATAX_NAME = "dataxName";
    // 用于保存DB对应的 tables
    public static final String DATAX_DB_NAME = "dataxDB";
    private final String type;
    public final boolean useMetaCfgDir;
    public final String pluginDescName;

    public static StoreResourceType parse(boolean isDB) {
        return isDB ? DataBase : DataApp;
    }


    public static StoreResourceType parse(String type) {
        StoreResourceType[] types = StoreResourceType.values();
        for (StoreResourceType t : types) {
            if (t.type.equals(type)) {
                return t;
            }
        }
        throw new IllegalStateException("illegal type:" + type);
    }

    public static String getPipeParma(StoreResourceType resourceType, String appname) {
        switch (resourceType) {
            case DataApp:
            case DataFlow:
                return DATAX_NAME + "_" + appname;
            case DataBase:
                return DATAX_DB_NAME + "_" + appname;
            default:
                throw new IllegalStateException("illegal resType:" + resourceType);
        }
    }

    public String getType() {
        return this.type;
    }

    StoreResourceType(String type, boolean useMetaCfgDir, String pluginDescName) {
        this.type = type;
        this.useMetaCfgDir = useMetaCfgDir;
        this.pluginDescName = pluginDescName;
    }
}
