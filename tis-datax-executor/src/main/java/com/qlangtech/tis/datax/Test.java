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

import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.plugin.KeyedPluginStore;

import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-08 13:02
 **/
public class Test {
    static {
        Config.setTestDataDir();
    }

    public static void main(String[] args) {
        String dataxName = "baisuitest";
        DataxExecutor.synchronizeDataXPluginsFromRemoteRepository(dataxName);
        KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(dataxName);
        DataxReader reader = readerStore.getPlugin();
        Objects.requireNonNull(reader, "reader can not be null");
        IDataXPluginMeta.DataXMeta dataxMeta = reader.getDataxMeta();
        System.out.println(dataxMeta.getImplClass());
    }
}
