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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年8月6日
 */
public class TISCollectionUtils {

    public static String INDEX_BACKFLOW_STATUS = "indexflowback_status";

    public static String INDEX_BACKFLOW_READED = "readed";

    public static String INDEX_BACKFLOW_ALL = "all";

    private static String NAME_PREFIX = "search4";

    private static final Pattern coreNamePattern = Pattern.compile(NAME_PREFIX + "(.+?)_shard(\\d+?)_replica_n(\\d+?)");

    public static class TisCoreName {

        private String name;

        private int sharedNo;

        private int replicaNo;

        public String getName() {
            return NAME_PREFIX + this.name;
        }

        public int getSharedNo() {
            return this.sharedNo;
        }

        public int getReplicaNo() {
            return this.replicaNo;
        }
    }

    public static TisCoreName parse(String corename) {
        TisCoreName coreName = new TisCoreName();
        Matcher coreNameMatcher = coreNamePattern.matcher(corename);
        if (!coreNameMatcher.matches()) {
            throw new IllegalArgumentException("core name:" + corename + " does not match pattern:" + coreNamePattern);
        }
        coreName.name = coreNameMatcher.group(1);
        coreName.sharedNo = Integer.parseInt(coreNameMatcher.group(2));
        coreName.replicaNo = Integer.parseInt(coreNameMatcher.group(3));
        return coreName;
    }
}
