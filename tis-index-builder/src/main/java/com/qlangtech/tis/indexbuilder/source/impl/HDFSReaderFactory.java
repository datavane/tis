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
package com.qlangtech.tis.indexbuilder.source.impl;

import com.qlangtech.tis.build.metrics.Counters;
import com.qlangtech.tis.build.metrics.Messages;
import com.qlangtech.tis.fs.IFileSplit;
import com.qlangtech.tis.fs.IFileSplitor;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fs.IndexBuildConfig;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.source.SourceReader;
import com.qlangtech.tis.indexbuilder.source.SourceReaderFactory;
import com.qlangtech.tis.indexbuilder.utils.Context;
import com.qlangtech.tis.manage.common.IndexBuildParam;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HDFSReaderFactory implements SourceReaderFactory {

    public static final Logger logger = LoggerFactory.getLogger(HDFSReaderFactory.class);

    protected Context context;

    protected IndexConf indexConf;

    protected ITISFileSystem fs;

    protected List<IFileSplit> fileSplits;

    private Long totalSize;

    // private IndexSchema indexSchema;
    // protected BlockingQueue<FileSplit> splitQueues;
    // private String delimiter = "\t";
    // public IndexSchema getIndexSchema() {
    // return indexSchema;
    // }
    public void setFs(ITISFileSystem fs) {
        this.fs = fs;
    }

    // public void setIndexSchema(IndexSchema indexSchema) {
    // this.indexSchema = indexSchema;
    // }
    protected TaskContext taskContext;

    private String[] titleText;

    int fileSplitsindex = 0;

    public synchronized SourceReader nextReader() throws Exception {
        if (fileSplitsindex >= fileSplits.size()) {
            logger.info("last fileSplitsindex:" + fileSplitsindex);
            return null;
        }
        IFileSplit split = fileSplits.get(fileSplitsindex++);
        // FileSplit split = (FileSplit) this.splitQueues.poll();
        // if (split != null) {
        HDFSReader reader = new HDFSReader(this.context, split, this.fs);
        reader.setTitleText(this.titleText);
        // }
        return reader;
    // }
    // return null;
    }

    public Long getTotalSize() {
        if (this.totalSize == null) {
            throw new IllegalStateException("prop totalSize can not be null");
        }
        return this.totalSize;
    }

    public void init(Context context) throws Exception {
        this.context = context;
        this.taskContext = ((TaskContext) context.get("taskcontext"));
        this.indexConf = ((IndexConf) context.get("indexconf"));
        String buildtabletitleitems = taskContext.getUserParam(IndexBuildParam.INDEXING_BUILD_TABLE_TITLE_ITEMS);
        if (StringUtils.isBlank(buildtabletitleitems)) {
            throw new IllegalStateException(IndexBuildParam.INDEXING_BUILD_TABLE_TITLE_ITEMS + " shall be set in user param ");
        }
        this.titleText = StringUtils.split(buildtabletitleitems, ",");
        Counters counters = this.taskContext.getCounters();
        Messages messages = this.taskContext.getMessages();
        context.put("filesystem", this.fs);
        IFileSplitor fileSplitor = this.fs.getSplitor(this.fs.getPath(this.indexConf.getSourcePath()));
        this.fileSplits = fileSplitor.getSplits(new IndexBuildConfig() {

            @Override
            public int getDocMakerThreadCount() {
                return indexConf.getDocMakerThreadCount();
            }

            @Override
            public long getMinSplitSize() {
                return indexConf.getMinSplitSize();
            }
        });
        if (this.fileSplits.size() < 1) {
            throw new IllegalStateException("fileSplits size can not small than 1");
        }
        this.totalSize = fileSplitor.getTotalSize();
        if (this.totalSize < 1) {
            throw new IllegalStateException("index build source totalSize can not small than 1");
        }
        counters.setCounterValue(Counters.Counter.MAP_INPUT_BYTES, this.totalSize);
    }
}
