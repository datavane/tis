/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin.datax;

import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.plugin.ds.*;
import com.qlangtech.tis.util.UploadPluginMeta;
import junit.framework.TestCase;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-03-24 15:06
 **/
public class TestSelectedTab extends TestCase {

    public void testGetContextTableCols() {
        DataxReader.DataSourceMetaPlugin sourceMetaPlugin = new DataxReader.DataSourceMetaPlugin();

        UploadPluginMeta pluginMeta = UploadPluginMeta.parse("test");

        SuFormProperties.setSuFormGetterContext(sourceMetaPlugin, pluginMeta, DataxReader.DataSourceMetaPlugin.tabName);

        List<CMeta> selectableCols = SelectedTab.getColsCandidate();
        Assert.assertEquals(2, selectableCols.size());

        List<CMeta> selectedCols = SelectedTab.getSelectedCols();
        Assert.assertEquals(1, selectedCols.size());
    }


}
