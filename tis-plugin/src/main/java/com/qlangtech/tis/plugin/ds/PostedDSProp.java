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
package com.qlangtech.tis.plugin.ds;

import com.qlangtech.tis.offline.DbScope;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

import static com.qlangtech.tis.util.UploadPluginMeta.KEY_REQUIRE;

/**
 * DS update extra params posted form client
 *
 * @author: baisui 百岁
 * @create: 2020-11-24 16:24
 */
public class PostedDSProp {

    private Optional<DBIdentity> dbname;
    private final DbScope dbType;
    private final Boolean update;

    public static PostedDSProp parse(UploadPluginMeta pluginMeta) {
        return new PostedDSProp(DBIdentity.parse(pluginMeta.getExtraParam(DBIdentity.KEY_DB_NAME))
                , DbScope.parse(pluginMeta.getExtraParam(DBIdentity.KEY_TYPE))
                , pluginMeta.isUpdate());
    }

    public static UploadPluginMeta createPluginMeta(DBIdentity dbName, boolean update) {
        return UploadPluginMeta.parse(HeteroEnum.DATASOURCE.identityValue() + ":" + KEY_REQUIRE
                + "," + DBIdentity.KEY_DB_NAME + "_" + dbName.identityValue() + "," + DBIdentity.KEY_UPDATE + "_" + update);
    }

    public static PostedDSProp parse(String dbIdVal) {
        return new PostedDSProp(DBIdentity.parseId(dbIdVal));
    }

    public void setDbname(DBIdentity dbname) {
        this.dbname = Optional.ofNullable(dbname);
    }

    public PostedDSProp(DBIdentity dbname) {
        this(dbname, DbScope.DETAILED, null);
    }

    public PostedDSProp(DBIdentity dbname, DbScope dbType) {
        this(dbname, dbType, null);
        //ReflectionUtils
    }

    private PostedDSProp(DBIdentity dbname, DbScope dbType, Boolean update) {
        this(Optional.ofNullable(dbname), dbType, update);
    }


    public PostedDSProp(Optional<DBIdentity> dbname, DbScope dbType, Boolean update) {
        this.dbname = dbname; //Objects.requireNonNull(dbname, "param dbName can not be null");
        this.dbType = dbType;
        this.update = update;
    }

    public Optional<DBIdentity> getDbname() {
        return this.dbname;
    }

    public DbScope getDbType() {
        return this.dbType;
    }

    public boolean isUpdate() {
        return this.update;
    }

    @Override
    public String toString() {
        return "PostedDSProp{" +
                "dbname=" + (dbname.isPresent() ? dbname.get().identityValue() : StringUtils.EMPTY) +
                ", dbType=" + dbType +
                ", update=" + update +
                '}';
    }
}
