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

package com.qlangtech.tis.plugin;

import com.google.common.collect.Lists;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.offline.module.action.OfflineDatasourceAction;
import junit.framework.TestCase;

import java.util.List;
import java.util.jar.Manifest;

/**
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-05-10 14:29
 **/
public class TestPluginAndCfgsSnapshotOnWorkflow extends TestCase {

  @Override
  protected void setUp() throws Exception {
    // super.setUp();
    CenterResource.setNotFetchFromCenterRepository();
    List<Option> opts = Lists.newArrayList();
    opts.add(new Option("orderdb", "orderdb"));
    OfflineDatasourceAction.existDbs = opts;
  }

  public void testCreateDataBatchJobManifestCfgAttrsWithDataFlow() throws Exception {
    IDataxProcessor processor = DataxProcessor.load(null, StoreResourceType.DataFlow, "tpch_parse_on_hive");
    Manifest manifest = PluginAndCfgsSnapshot.createDataBatchJobManifestCfgAttrs(processor);
    Assert.assertNotNull(manifest);
  }
}
