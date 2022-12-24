package com.qlangtech.tis.datax;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-12-24 10:48
 **/
public class DataXJobInfo {
    public final String jobFileName;
    private final Optional<String[]> targetTableNames;
    private static DataXJobInfo currJobInfo;
    private static final String FILENAME_SPLIT_CHAR = "/";
    private static final String TAB_SPLIT_CHAR = ",";

    public static DataXJobInfo parse(String jobInfo) {
        String[] split = StringUtils.split(jobInfo, FILENAME_SPLIT_CHAR);
        if (split.length > 1) {
            return currJobInfo = new DataXJobInfo(split[0], Optional.of(StringUtils.split(split[1], TAB_SPLIT_CHAR)));
        } else {
            return currJobInfo = new DataXJobInfo(split[0], Optional.empty());
        }
    }

    public static DataXJobInfo create(String jobFileName, List<String> targetTabs) {
        return currJobInfo = new DataXJobInfo(jobFileName, (targetTabs == null || targetTabs.isEmpty())
                ? Optional.empty() : Optional.of(targetTabs.toArray(new String[targetTabs.size()])));
    }

    public static DataXJobInfo getCurrent() {
        return Objects.requireNonNull(currJobInfo, "currJobInfo can not be null");
    }

    public String serialize() {
        StringBuffer buffer = new StringBuffer(jobFileName);
        if (targetTableNames.isPresent()) {
            String[] tabs = targetTableNames.get();
            if (tabs.length > 0) {
                buffer.append(FILENAME_SPLIT_CHAR).append(String.join(TAB_SPLIT_CHAR, tabs));
            }
        }
        return buffer.toString();
    }

    /**
     * 全部可执行的表列表枚举
     *
     * @param allTabs 全部可执行的表
     * @return 需要执行的表的列表
     */
    public static List<String> getExecTables(final List<String> allTabs, String escapeChar) {
        return allTabs;
//        Map<String, String> allAcceptedTabs =
//                allTabs.stream().collect(Collectors.toMap((tab) -> StringUtils.remove(tab, escapeChar), (tab) -> tab));
//        List<String> execTables = new ArrayList<>();
//        DataXJobInfo jobInfo = getCurrent();
//        Optional<String[]> targetTableNames = jobInfo.getTargetTableNames();
//        String[] filterTabs = null;
//        if (!targetTableNames.isPresent() || (filterTabs = targetTableNames.get()).length < 1) {
//            throw new IllegalStateException(jobInfo.jobFileName + " relevant targetTableNames can not be empty");
//        }
//        String t = null;
//        for (String tab : filterTabs) {
//            if ((t = allAcceptedTabs.get(tab)) != null) {
//                execTables.add(t);
//            }
//        }
//        return execTables;
    }

    private DataXJobInfo(String jobFileName, Optional<String[]> targetTableName) {
        if (StringUtils.isEmpty(jobFileName)) {
            throw new IllegalArgumentException("param jobFileName can not be empty");
        }
        this.jobFileName = jobFileName;
        this.targetTableNames = targetTableName;
    }

    public Optional<String[]> getTargetTableNames() {
        return this.targetTableNames;
    }

    public File getJobPath(File dataxCfgDir) {
        return new File(dataxCfgDir, jobFileName);
    }
}
