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

import com.qlangtech.tis.plugin.ds.DBIdentity;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-12-24 10:48
 **/
public class DataXJobInfo {

    public static final String KEY_ALL_ROWS_APPROXIMATELY = "allRowsApproximately";
    public static final String KEY_DATAX_JOB_INFO_SERIALIZE_INFO = "dataXJobInfo";
    //    public static final AtomicReference<File> dataXExecutorDir
    //            = new AtomicReference<>(TisAppLaunch.isTestMock()
    //            ? new File(".")
    //            : new File("/opt/tis/" + IDataXTaskRelevant.KEY_TIS_DATAX_EXECUTOR));

    public static final AtomicReference<File> dataXExecutorDir = new AtomicReference<>(new File("."));

    public static int DATAX_THREAD_PROCESSING_CANCAL_EXITCODE = 943;
    private static final String FILENAME_SPLIT_CHAR = "/";
    private static final String TAB_SPLIT_CHAR = ",";
    private static DataXJobInfo currJobInfo;


    private final String jobFileName;
    /**
     * 对应的分表集合
     */
    private final Optional<String[]> targetTableNames;

    private final DBIdentity dbFactoryId;

    public static DataXJobInfo parse(String jobInfo) {
        if (StringUtils.isEmpty(jobInfo)) {
            throw new IllegalArgumentException("param jobInfo can not be empty");
        }
        String[] split = StringUtils.split(jobInfo, FILENAME_SPLIT_CHAR);
        if (split.length > 2) {
            return currJobInfo = new DataXJobInfo(split[0], DBIdentity.parseId(split[1]),
                    Optional.of(StringUtils.split(split[2], TAB_SPLIT_CHAR)));
        } else if (split.length == 2) {
            return currJobInfo = new DataXJobInfo(split[0], DBIdentity.parseId(split[1]), Optional.empty());
        } else {
            throw new IllegalStateException("illegal jobInfo:" + jobInfo);
        }
    }

    public static DataXJobInfo create(String jobFileName, DBIdentity dbFactoryId, List<String> targetTabs) {
        return currJobInfo = new DataXJobInfo(jobFileName, dbFactoryId, (targetTabs == null || targetTabs.isEmpty())
                ? Optional.empty() : Optional.of(targetTabs.toArray(new String[targetTabs.size()])));
    }

    public static DataXJobInfo getCurrent() {
        return Objects.requireNonNull(currJobInfo, "currJobInfo can not be null");
    }

    public static File getJobPath(File dataxCfgDir, String dbFactoryId, String jobFileName) {
        if (StringUtils.isEmpty(dbFactoryId)) {
            throw new IllegalArgumentException("param dbFactoryId can not be null");
        }
        File dataXCfg = new File(dataxCfgDir, dbFactoryId + File.separator + jobFileName);
        return dataXCfg;
    }

    public static File getDataXExecutorDir() {
        File workDir = dataXExecutorDir.get();
        if (!workDir.exists()) {
            throw new IllegalStateException("workDir is not exist:" + workDir.getAbsolutePath());
        }
        return workDir;
    }

    public File getJobPath(File dataxCfgDir) {
        return getJobPath(dataxCfgDir, this.dbFactoryId.identityValue(), jobFileName);
    }


    public DBIdentity getDbFactoryId() {
        return this.dbFactoryId;
    }

    public String serialize() {
        StringBuffer buffer = new StringBuffer(jobFileName);
        buffer.append(FILENAME_SPLIT_CHAR).append(this.dbFactoryId.identityValue());
        if (targetTableNames.isPresent()) {
            String[] tabs = targetTableNames.get();
            if (tabs.length > 0) {
                buffer.append(FILENAME_SPLIT_CHAR).append(String.join(TAB_SPLIT_CHAR, tabs));
            }
        }
        return buffer.toString();
    }

    @Override
    public String toString() {
        return this.serialize();
    }

    private DataXJobInfo(String jobFileName, DBIdentity dbFactoryId, Optional<String[]> targetTableName) {
        if (StringUtils.isEmpty(jobFileName)) {
            throw new IllegalArgumentException("param jobFileName can not be empty");
        }
        this.jobFileName = jobFileName;
        this.targetTableNames = targetTableName;
        this.dbFactoryId = dbFactoryId;
    }

    public Optional<String[]> getTargetTableNames() {
        return this.targetTableNames;
    }


    public String getJobFileName() {
        return this.jobFileName;
    }
}
