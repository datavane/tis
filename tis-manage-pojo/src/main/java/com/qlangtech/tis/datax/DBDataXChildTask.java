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

import com.alibaba.fastjson.annotation.JSONField;

import java.io.File;

public class DBDataXChildTask {
    // 需要执行数据抽取的数据库编号，如果支持JDBC的DB 一般为DB的jdbcUrl
    private final String dbIdenetity;
    public final String dbFactoryId;
    private final String dataXCfgFileName;

    public DBDataXChildTask(String dbIdenetity, String dbFactoryId, String dataXCfgFileName) {
        this.dbIdenetity = dbIdenetity;
        this.dataXCfgFileName = dataXCfgFileName;
        this.dbFactoryId = dbFactoryId;
    }

    public String getDbFactoryId() {
        return this.dbFactoryId;
    }

    public String getDbIdenetity() {
        return this.dbIdenetity;
    }

    public String getDataXCfgFileName() {
        return this.dataXCfgFileName;
    }

    @JSONField(serialize = false)
    public String getDataXCfgFileNameWithSuffix() {
        return this.getDataXCfgFileName() + DataXCfgFile.DATAX_CREATE_DATAX_CFG_FILE_NAME_SUFFIX;
    }

    public File getJobPath(File dataxCfgDir) {
        //return new File(dataxCfgDir, jobFileName);
        //            File dataXCfg = new File(dataxCfgDir
        //                    , this.getDbFactoryId() + File.separator + this.getDataXCfgFileNameWithSuffix());
        //            return dataXCfg;
        return DataXJobInfo.getJobPath(dataxCfgDir, this.getDbFactoryId(), this.getDataXCfgFileNameWithSuffix());
    }

    @Override
    public String toString() {
        return "{" + "dbIdenetity='" + dbIdenetity + '\'' + ", dbFactoryId='" + dbFactoryId + '\'' + ", " +
                "dataXCfgFileName='" + dataXCfgFileName + '\'' + '}';
    }
}
