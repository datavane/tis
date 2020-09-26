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
package com.qlangtech.tis.build.yarn;

import com.qlangtech.tis.build.NodeMaster;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.indexbuilder.HdfsIndexBuilder;
import com.qlangtech.tis.indexbuilder.map.IndexGetConfig;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import org.apache.commons.io.FileUtils;
import java.io.IOException;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-15 16:08
 */
public class IndexBuildNodeMaster extends NodeMaster {

    private HdfsIndexBuilder indexBuilder;

    private final IndexBuilderTriggerFactory propsGetter;

    public IndexBuildNodeMaster(IndexBuilderTriggerFactory propsGetter) {
        super(propsGetter);
        this.propsGetter = propsGetter;
    }

    @Override
    protected void startExecute(TaskContext context) {
        /* 执行索引build start */
        IndexGetConfig configJob = new IndexGetConfig(propsGetter.getFsFactory().getFileSystem());
        this.indexBuilder = new HdfsIndexBuilder(propsGetter, statusRpc);
        configJob.map(context);
        configJob.removeTmpConfigDirOnExit(context.getCollectionName());
        indexBuilder.map(context);
    }
}
