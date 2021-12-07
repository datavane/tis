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
package com.qlangtech.tis.indexbuilder.map;

import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.indexbuilder.source.impl.HDFSReaderFactory;
import com.qlangtech.tis.manage.common.IndexBuildParam;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class IndexConf {

    public static final Logger logger = LoggerFactory.getLogger(IndexConf.class);

    private final TaskContext context;

    public IndexConf(TaskContext context) {
        this.context = context;
    }

    // public String getFsName() {
    // return getSourceFsName();
    // }
    // public String getSourceFsName() {
    // return get(IndexBuildParam.INDEXING_SOURCE_FS_NAME);
    // }
    public String getOutputPath() {
        String outputPath = this.get(IndexBuildParam.INDEXING_OUTPUT_PATH);
        if (StringUtils.isEmpty(outputPath)) {
            throw new IllegalStateException("param " + IndexBuildParam.INDEXING_OUTPUT_PATH + " can not be null");
        }
        return outputPath;
    }

    public String getCollectionName() {
        return this.context.getCollectionName();
    }

    private String get(String key) {
        return this.context.getUserParam(key);
    }

    private int getInt(String key) {
        String val = null;
        try {
            val = this.context.getUserParam(key);
            return Integer.parseInt(val);
        } catch (Throwable e) {
            throw new IllegalStateException("key:" + key + ",val:" + val, e);
        }
    }

    public String getSchemaName() {
        return "schema.xml";
    }

    public String getSourcePath() {
        try {
            return URLDecoder.decode(get(IndexBuildParam.INDEXING_SOURCE_PATH), "utf8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public int getFlushCountThreshold() {
        return 90000000;
    }

    public long getFlushSizeThreshold() {
        return 100 * 1024 * 1024;
    }

    public int getDocQueueSize() {
        return 100;
    }

    public int getMinSplitSize() {
        return 128 * 1024 * 1024;
    }

    public String getCoreName() {
        return get(IndexBuildParam.INDEXING_CORE_NAME);
    }

    public int getDocMakerThreadCount() {
        return 5;
    }

    public int getIndexMakerThreadCount() {
        return 20;
    }

    public int getRamDirQueueSize() {
        return 4;
    }

    public int getMaxFailCount() {
        return getInt(IndexBuildParam.INDEXING_MAX_DOC_FAILD_LIMIT);
    }

    public long getOptimizeSizeThreshold() {
        // return getLong("indexing.optimze.optimizeSizeThreshold", );
        return 1300 * 1024 * 1024;
    }

    public int getMergeThreads() {
        return 6;
    }

    /**
     * @return
     */
    public String getSourceReaderFactory() {
        return HDFSReaderFactory.class.getName();
    }

    public String getIncrTime() {
        return get(IndexBuildParam.INDEXING_INCR_TIME);
    }
}
