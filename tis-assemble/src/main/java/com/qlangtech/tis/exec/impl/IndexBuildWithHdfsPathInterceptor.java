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
package com.qlangtech.tis.exec.impl;

import com.qlangtech.tis.fs.ITISFileSystem;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.indexbuild.HdfsSourcePathCreator;
import com.qlangtech.tis.fullbuild.indexbuild.ITabPartition;
import com.qlangtech.tis.fullbuild.servlet.BuildTriggerServlet;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo;

/**
 * 当直接使用数据中心构建好的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年3月11日
 */
public final class IndexBuildWithHdfsPathInterceptor extends IndexBuildInterceptor {

    // private static final String COMPONENT_NAME = "directbuild";
    private static final String HDFS_PATH = "hdfspath";

    private static final Logger logger = LoggerFactory.getLogger(IndexBuildWithHdfsPathInterceptor.class);

    // @Override
    // public ExecuteResult intercept(ActionInvocation invocation) throws
    // Exception {
    // 
    // IExecChainContext execContext = invocation.getContext();
    // 
    // // 删除历史build索引文件
    // HiveRemoveHistoryDataTask removeHistoryDataTask = new
    // HiveRemoveHistoryDataTask(execContext.getIndexName(),
    // execContext.getContextUserName(), execContext.getDistributeFileSystem());
    // removeHistoryDataTask.removeHistoryBuildFile();
    // 
    // return super.intercept(invocation);
    // }
    @Override
    protected ExecuteResult execute(IExecChainContext execContext) throws Exception {
        throw new UnsupportedOperationException();
    // return super.execute(execContext);
    }

    @Override
    protected HdfsSourcePathCreator createIndexBuildSourceCreator(final IExecChainContext execContext, ITabPartition ps) {
        return new HdfsSourcePathCreator() {

            @Override
            public String build(String group) {
                final String hdfspath = execContext.getString(HDFS_PATH);
                ITISFileSystem fs = execContext.getIndexBuildFileSystem().getFileSystem();
                String path = hdfspath + "/pmod=" + group;
                try {
                    if (fs.exists(fs.getPath(path))) {
                        return path;
                    }
                    logger.info("sourcepath not exist:" + path);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                final String targetPath = "0".equals(group) ? hdfspath : path;
                logger.info("source hdfs path:" + targetPath);
                return targetPath;
            }
        };
    }

    @Override
    protected void setBuildTableTitleItems(String indexName, ImportDataProcessInfo processinfo, IExecChainContext execContext) {
        processinfo.setBuildTableTitleItems(execContext.getString(BuildTriggerServlet.KEY_COLS));
        // if( execContext.getString(ImportDataProcessInfo.KEY_DELIMITER)){
        // 
        // }
        // 
        processinfo.setHdfsdelimiter(StringUtils.defaultIfEmpty(execContext.getString(ImportDataProcessInfo.KEY_DELIMITER), ImportDataProcessInfo.DELIMITER_001));
    }
    // @Override
    // protected int getGroupSize(String indexName,
    // HdfsSourcePathCreator pathCreator, FileSystem fileSystem)
    // throws Exception {
    // 
    // return GROUP_SIZE;
    // }
    // @Override
    // public String getName() {
    // return COMPONENT_NAME;
    // }
}
