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
package com.qlangtech.tis.exec.impl;

import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fullbuild.indexbuild.ITabPartition;
import com.qlangtech.tis.fullbuild.indexbuild.IndexBuildSourcePathCreator;
import com.qlangtech.tis.fullbuild.servlet.BuildTriggerServlet;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 当直接使用数据中心构建好的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年3月11日
 */
public final class IndexBuildWithHdfsPathInterceptor extends IndexBuildInterceptor {

    private static final String HDFS_PATH = "hdfspath";

    private static final Logger logger = LoggerFactory.getLogger(IndexBuildWithHdfsPathInterceptor.class);

    @Override
    protected ExecuteResult execute(IExecChainContext execContext) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected IndexBuildSourcePathCreator createIndexBuildSourceCreator(final IExecChainContext execContext, ITabPartition ps) {
        return new IndexBuildSourcePathCreator() {
            @Override
            public String build(String group) {
                final String hdfspath = execContext.getString(HDFS_PATH);
                ITISFileSystem fs = execContext.getIndexBuildFileSystem();
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
        processinfo.setHdfsdelimiter(
                StringUtils.defaultIfEmpty(execContext.getString(ImportDataProcessInfo.KEY_DELIMITER), ImportDataProcessInfo.DELIMITER_001));
    }
}
