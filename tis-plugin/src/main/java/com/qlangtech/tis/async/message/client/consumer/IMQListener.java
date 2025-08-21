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
package com.qlangtech.tis.async.message.client.consumer;

import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.incr.IConsumerRateLimiter;
import com.qlangtech.tis.plugin.incr.IncrStreamFactory;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IMQListener<SOURCE> {

//    String getTopic();
//
//    void setDeserialize(IAsyncMsgDeserialize deserialize);

//    IConsumerHandle getConsumerHandle();

    /**
     * Listener启动
     *
     * @param flinkCDCPipelineEnable sinkFunc 是否使用flinkCDC Pipeline流程
     * @param dataxName
     * @param rdbmsReader
     * @param tabs
     * @param dataXProcessor
     * @return
     * @throws MQConsumeException
     */
    AsyncMsg<SOURCE> start(IConsumerRateLimiter streamFactory, boolean flinkCDCPipelineEnable, DataXName dataxName
            , IDataxReader rdbmsReader, List<ISelectedTab> tabs, IDataxProcessor dataXProcessor) throws MQConsumeException;
}
