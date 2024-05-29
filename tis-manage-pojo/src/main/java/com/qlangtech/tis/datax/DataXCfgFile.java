/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.datax;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.File;

public class DataXCfgFile {
    public static final String DATAX_CREATE_DATAX_CFG_FILE_NAME_SUFFIX = ".json";
    public static final String DATAX_CREATE_DDL_FILE_NAME_SUFFIX = ".sql";
    private File file;
    private String fileName;
    private String dbFactoryId;

    public DataXCfgFile() {
    }

    public DataXCfgFile setFile(File file) {
        this.file = file;
        this.fileName = file.getName();
        return this;
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public DataXCfgFile setDbFactoryId(String dbFactoryId) {
        this.dbFactoryId = dbFactoryId;
        return this;
    }

    public String getDbFactoryId() {
        return dbFactoryId;
    }

    public String getFileName() {
        return this.fileName;
    }

    @JSONField(serialize = false)
    public File getFile() {
        return this.file;
    }
}
