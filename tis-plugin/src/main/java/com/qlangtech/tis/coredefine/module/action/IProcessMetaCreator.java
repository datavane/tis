package com.qlangtech.tis.coredefine.module.action;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.impl.DataXBasicProcessMeta;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.util.IPluginContext;

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

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-01-25 17:49
 **/
interface IProcessMetaCreator {
    default DataXBasicProcessMeta createProcessMeta(IPluginContext pluginContext
            , String dataXName, JSONObject reader, JSONObject writer) throws Exception {
        DataxReader.BaseDataxReaderDescriptor readerDesc
                = (DataxReader.BaseDataxReaderDescriptor) TIS.get().getDescriptor(reader.getString("impl"));
        DataxWriter.BaseDataxWriterDescriptor writerDesc
                = (DataxWriter.BaseDataxWriterDescriptor) TIS.get().getDescriptor(writer.getString("impl"));

        return createProcessMeta(pluginContext, dataXName, readerDesc, writerDesc);
    }

    DataXBasicProcessMeta createProcessMeta(IPluginContext pluginContext
            , String dataXName, DataxReader.BaseDataxReaderDescriptor reader, DataxWriter.BaseDataxWriterDescriptor writer) throws Exception;
}
