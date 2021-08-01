/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.plugin.ds;

import com.qlangtech.tis.offline.DbScope;
import com.qlangtech.tis.util.UploadPluginMeta;

/**
 * DS update extra params posted form client
 *
 * @author: baisui 百岁
 * @create: 2020-11-24 16:24
 */
public class PostedDSProp {

    public static final String KEY_DB_NAME = "dsname";
    public static final String KEY_TYPE = "type";
    public static final String KEY_UPDATE = "update";

    private String dbname;
    private final DbScope dbType;
    private final Boolean update;

    public static PostedDSProp parse(UploadPluginMeta pluginMeta) {
        return new PostedDSProp(pluginMeta.getExtraParam(KEY_DB_NAME)
                , DbScope.parse(pluginMeta.getExtraParam(KEY_TYPE))
                , pluginMeta.isUpdate());
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public PostedDSProp(String dbname) {
        this(dbname, DbScope.DETAILED, null);
    }

    public PostedDSProp(String dbname, DbScope dbType) {
        this(dbname, dbType, null);
        //ReflectionUtils
    }

    private PostedDSProp(String dbname, DbScope dbType, Boolean update) {
        this.dbname = dbname;
        this.dbType = dbType;
        this.update = update;
    }

    public String getDbname() {
        return dbname;
    }

    public DbScope getDbType() {
        return dbType;
    }

    public boolean isUpdate() {
        return this.update;
    }
}
