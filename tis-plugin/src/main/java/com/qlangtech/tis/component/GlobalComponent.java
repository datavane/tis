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
package com.qlangtech.tis.component;

import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.offline.TableDumpFactory;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class GlobalComponent {

    // 在页面上显示扩展点详细信息吗？显示则可以将页面当作一个产品文档很直观地看到每个扩展点的内容
    private boolean showExtensionDetail;

    public boolean isShowExtensionDetail() {
        return showExtensionDetail;
    }

    public GlobalComponent setShowExtensionDetail(boolean showExtensionDetail) {
        this.showExtensionDetail = showExtensionDetail;
        return this;
    }

    // private List<FileSystemFactory> fsFactories;
    private List<TableDumpFactory> dsDumpFactories;

    private List<IndexBuilderTriggerFactory> indexBuilderFactories;

    // private List<FlatTableBuilder> flatTableBuilders;
    // private IncrK8sConfig incrK8sConfig;
    // public IncrK8sConfig getIncrK8sConfig() {
    // return this.incrK8sConfig;
    // }
    // 
    // public void setIncrK8sConfig(IncrK8sConfig incrK8sConfig) {
    // this.incrK8sConfig = incrK8sConfig;
    // }
    // public List<FlatTableBuilder> getFlatTableBuilders() {
    // if (flatTableBuilders == null) {
    // return Collections.emptyList();
    // }
    // return flatTableBuilders;
    // }
    // 
    // public void setFlatTableBuilders(List<FlatTableBuilder> flatTableBuilders) {
    // this.flatTableBuilders = flatTableBuilders;
    // }
    // public List<FileSystemFactory> getFsFactories() {
    // if (this.fsFactories == null) {
    // return Collections.emptyList();
    // }
    // return this.fsFactories;
    // }
    // 
    // public void setFsFactories(List<FileSystemFactory> fsFactories) {
    // this.fsFactories = fsFactories;
    // }
    public List<TableDumpFactory> getDsDumpFactories() {
        return this.dsDumpFactories;
    }

    public void setDsDumpFactories(List<TableDumpFactory> dsDumpFactories) {
        this.dsDumpFactories = dsDumpFactories;
    }

    public List<IndexBuilderTriggerFactory> getIndexBuilderFactories() {
        return indexBuilderFactories;
    }

    public void setIndexBuilderFactories(List<IndexBuilderTriggerFactory> indexBuilderFactories) {
        this.indexBuilderFactories = indexBuilderFactories;
    }
}
