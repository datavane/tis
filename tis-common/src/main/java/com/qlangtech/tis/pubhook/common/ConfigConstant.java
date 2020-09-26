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
package com.qlangtech.tis.pubhook.common;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-22
 */
public class ConfigConstant {

    public static final String FILE_APPLICATION = "applicationContext.xml";

    public static final String FILE_DATA_SOURCE = "ds.xml";

    public static final String FILE_SCHEMA = "schema.xml";

    public static final String FILE_SOLR = "solrconfig.xml";

    public static final String FILE_JAR = "dump.jar";

    public static final String FILE_CORE_PROPERTIES = "core.properties";
    // public static String getRunEnvir(int runEnvironment) {
    // switch (runEnvironment) {
    // case 0:
    // return "日常环境";
    // case 1:
    // return "DAILY";
    // default:
    // throw new IllegalStateException("runEnvironment:" + runEnvironment
    // + "  is not valid");
    // }
    // }
}
