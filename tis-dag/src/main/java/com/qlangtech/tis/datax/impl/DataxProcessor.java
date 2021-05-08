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
package com.qlangtech.tis.datax.impl;

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.DescriptorExtensionList;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.IBasicAppSource;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DataX任务执行方式的抽象
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 16:46
 */
public abstract class DataxProcessor implements IBasicAppSource, IdentityName, IDataxProcessor {

    protected static final String DEFAULT_DATAX_PROCESSOR_NAME = "DataxProcessor";
    public static final String DATAX_CFG_DIR_NAME = "dataxCfg";

    public static Descriptor<IAppSource> getPluginDescMeta() {
        DescriptorExtensionList<IAppSource, Descriptor<IAppSource>> descs = TIS.get().getDescriptorList(IAppSource.class);
        Optional<Descriptor<IAppSource>> dataxProcessDescs
                = descs.stream().filter((des) -> DEFAULT_DATAX_PROCESSOR_NAME.equals(des.getDisplayName())).findFirst();
        if (!dataxProcessDescs.isPresent()) {
            throw new IllegalStateException("dataX process descriptor:" + DEFAULT_DATAX_PROCESSOR_NAME + " relevant descriptor can not be null");
        }
        return dataxProcessDescs.get();
    }

    public abstract Application buildApp();

    private List<TableAlias> tableMaps;

    @Override
    public <T> T accept(IAppSourceVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Map<String, TableAlias> getTabAlias() {
        if (tableMaps == null) {
            return Collections.emptyMap();
        }
        return this.tableMaps.stream().collect(Collectors.toMap((m) -> m.getFrom(), (m) -> m));
    }

    @Override
    public IDataxReader getReader() {
        return DataxReader.load(this.identityValue());
    }

    @Override
    public IDataxWriter getWriter() {
        return DataxWriter.load(this.identityValue());
    }

    public void setTableMaps(List<TableAlias> tableMaps) {
        this.tableMaps = tableMaps;
    }

    public File getDataxCfgDir() {
        KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(this.identityValue());
        File targetFile = readerStore.getTargetFile();
        return new File(targetFile.getParentFile(), DATAX_CFG_DIR_NAME);
    }

    /**
     * dataX配置文件列表
     *
     * @return
     */
    public List<String> getDataxCfgFileNames() {
        File dataxCfgDir = getDataxCfgDir();
        if (!dataxCfgDir.exists()) {
            throw new IllegalStateException("dataxCfgDir is not exist:" + dataxCfgDir.getAbsolutePath());
        }
        if (dataxCfgDir.list().length < 1) {
            throw new IllegalStateException("dataxCfgDir is empty can not find any files:" + dataxCfgDir.getAbsolutePath());
        }
        return Lists.newArrayList(dataxCfgDir.list());
    }

//     "setting": {
//        "speed": {
//            "channel": 3
//        },
//        "errorLimit": {
//            "record": 0,
//                    "percentage": 0.02
//        }
//    },

}
