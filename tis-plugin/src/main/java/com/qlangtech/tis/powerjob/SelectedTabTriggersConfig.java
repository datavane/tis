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

package com.qlangtech.tis.powerjob;

import com.qlangtech.tis.datax.StoreResourceType;
import org.apache.commons.lang.StringUtils;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-03-19 15:54
 **/
public class SelectedTabTriggersConfig extends TriggersConfig {

    private final String tabName;


    public SelectedTabTriggersConfig(StoreResourceType resType, String dataXName, String tabName) {
        super(dataXName, resType);
        if (StringUtils.isEmpty(tabName)) {
            throw new IllegalArgumentException("param tabName can not be empty");
        }
        this.tabName = tabName;

    }

    public String preTrigger;
    public String postTrigger;
    //private List<DataXTaskJobName> splitTabsCfg = Lists.newArrayList();

    public String getTabName() {
        return this.tabName;
    }

//    public List<DataXTaskJobName> getSplitTabsCfg() {
//        return this.splitTabsCfg;
//    }
//
//    public void addSplitCfg(DataXTaskJobName tskMsg) {
//        splitTabsCfg.add(tskMsg);
//    }

    public String getPreTrigger() {
        return preTrigger;
    }

    public String getPostTrigger() {
        return postTrigger;
    }
}
