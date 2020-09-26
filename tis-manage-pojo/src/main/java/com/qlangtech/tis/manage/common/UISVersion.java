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
package com.qlangtech.tis.manage.common;

import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年6月16日
 */
public class UISVersion {

    public static final String SOLR_VERSION_6 = "solr6";

    public static final String SOLR_VERSION_5 = "solr5";

    public static boolean isDataCenterCollection(String collection) {
        // 数据中心测试用需要过滤掉
        return StringUtils.startsWith(collection, "search4_fat") || StringUtils.startsWith(collection, "search4_thin");
    // || StringUtils.equalsIgnoreCase(collection, "search4TimeStatistic")
    // || StringUtils.equalsIgnoreCase(collection, "search4OperationStatistic");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    }
}
