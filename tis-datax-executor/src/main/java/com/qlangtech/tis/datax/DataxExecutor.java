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
package com.qlangtech.tis.datax;


import com.alibaba.datax.common.statistics.PerfTrace;
import com.alibaba.datax.core.Engine;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.plugin.ComponentMeta;
import com.qlangtech.tis.plugin.IRepositoryResource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-20 12:38
 */
public class DataxExecutor {
    public void startWork(String dataxName, String jobPath) throws IOException, Exception {
        // TaskConfig config = TaskConfig.getInstance();
        String[] args = new String[]{"-mode", "standalone", "-jobid", "-1", "-job", jobPath};
        List<IRepositoryResource> keyedPluginStores = Lists.newArrayList();// Lists.newArrayList(DataxReader.getPluginStore(dataxName), DataxWriter.getPluginStore(dataxName));
        keyedPluginStores.add(DataxReader.getPluginStore(dataxName));
        keyedPluginStores.add(DataxWriter.getPluginStore(dataxName));
        ComponentMeta dataxComponentMeta = new ComponentMeta(keyedPluginStores);
        dataxComponentMeta.synchronizePluginsFromRemoteRepository();

        Thread.currentThread().setContextClassLoader(TIS.get().getPluginManager().uberClassLoader);
        try {
            Engine.entry(args);
        } catch (Throwable e) {
            throw new Exception(e);
        } finally {
            cleanPerfTrace();
        }
    }

    private void cleanPerfTrace() {
        try {
            Field istField = PerfTrace.class.getDeclaredField("instance");
            istField.setAccessible(true);

            istField.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
