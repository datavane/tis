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
package com.qlangtech.tis.indexbuilder.map;

import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.indexbuilder.exception.IndexBuildException;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.IndexBuildParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexGetConfig implements TaskMapper {

    public static final Logger logger = LoggerFactory.getLogger(IndexGetConfig.class);

    private final ITISFileSystem fs;

    long startTime;

    private static final String globalUniqueId = UUID.randomUUID().toString();

    public IndexGetConfig(ITISFileSystem fs) {
        startTime = System.currentTimeMillis();
        this.fs = fs;
    }

    public static IndexConf getIndexConf(TaskContext context) {
        IndexConf indexConf;
        indexConf = new IndexConf(context);
        return indexConf;
    }

    @Override
    public void map(TaskContext context) {
        final IndexConf indexConf = getIndexConf(context);
        try {
            String schemaPath = context.getUserParam(IndexBuildParam.INDEXING_SCHEMA_PATH);
            if (schemaPath == null) {
                logger.error(IndexBuildParam.INDEXING_SCHEMA_PATH + " param have not been config");
                throw new IndexBuildException(IndexBuildParam.INDEXING_SCHEMA_PATH + "  param have not been config");
            }
            String solrConfigPath = context.getUserParam(IndexBuildParam.INDEXING_SOLRCONFIG_PATH);
            if (solrConfigPath == null) {
                logger.error(IndexBuildParam.INDEXING_SOLRCONFIG_PATH + " param have not been config");
                throw new IndexBuildException(IndexBuildParam.INDEXING_SOLRCONFIG_PATH + "  param have not been config");
            }
            copyRemoteFile2Local(new PathStrategy() {

                @Override
                public String getRemotePath() {
                    return schemaPath;
                }

                @Override
                public File getLocalDestFile() {
                    return getLocalTmpSchemaFile(indexConf.getCollectionName());
                }
            });
            copyRemoteFile2Local(new PathStrategy() {

                @Override
                public String getRemotePath() {
                    return solrConfigPath;
                }

                @Override
                public File getLocalDestFile() {
                    return getLocalTmpSolrConfigFile(indexConf.getCollectionName());
                }
            });
        // } catch (IOException e) {
        // 
        // return new TaskReturn(TaskReturn.ReturnCode.FAILURE,
        // "get schema error:" + ExceptionUtils.getStackTrace(e));
        // }
        // return new TaskReturn(TaskReturn.ReturnCode.SUCCESS, "success");
        } catch (Throwable e) {
            // return new TaskReturn(TaskReturn.ReturnCode.FAILURE, "get schema fail:" + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public static File getLocalTmpSchemaFile(String collectionName) {
        return new File(getTmpConifgDir(collectionName), ConfigFileReader.FILE_SCHEMA.getFileName());
    }

    public static File getLocalTmpSolrConfigFile(String collectionName) {
        return new File(getTmpConifgDir(collectionName), ConfigFileReader.FILE_SOLR.getFileName());
    }

    private static final File getTmpConifgDir(String collectionName) {
        if (StringUtils.isBlank(globalUniqueId)) {
            throw new IllegalStateException("globalUniqueId have not been set");
        }
        try {
            File tmpConfigDir = new File(File.separator + "tmp", collectionName + File.separator + globalUniqueId);
            FileUtils.forceMkdir(tmpConfigDir);
            return tmpConfigDir;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 任务退出之后需要将临时目录删除干净
     *
     * @param collectionName
     */
    public void removeTmpConfigDirOnExit(String collectionName) {
        File tmpConifgDir = getTmpConifgDir(collectionName);
        try {
            FileUtils.forceDeleteOnExit(tmpConifgDir);
        } catch (IOException e) {
            throw new RuntimeException(tmpConifgDir.getAbsolutePath(), e);
        }
    }

    protected File copyRemoteFile2Local(PathStrategy pStrategy) throws IOException {
        IPath remotePath = fs.getPath(pStrategy.getRemotePath());
        File dstP = pStrategy.getLocalDestFile();
        FileUtils.forceMkdirParent(dstP);
        File dstPath = new File(dstP.getParent());
        fs.copyToLocalFile(remotePath, dstPath);
        logger.info("remote:" + remotePath + " copy to local:" + dstP + " succsessful");
        return dstP;
    }

    private interface PathStrategy {

        public String getRemotePath();

        public File getLocalDestFile();
    }
}
