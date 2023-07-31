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

package com.qlangtech.tis.plugin.ds.split;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.ds.SplitTableStrategy;
import org.junit.Assert;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-07-31 18:18
 **/
public class SplitTableStrategyUtils {
    public static SplitTableStrategy createSplitTableStrategy() {
        Descriptor.FormData strategy = new Descriptor.FormData();
        strategy.addProp("tabPattern", SplitTableStrategy.PATTERN_PHYSICS_TABLE.toString());
        Descriptor strategyDesc = TIS.get().getDescriptor("com.qlangtech.tis.plugin.ds.split.DefaultSplitTableStrategy");
        Assert.assertNotNull("kuserTokenDesc can not be null", strategyDesc);
        return (SplitTableStrategy) strategyDesc.newInstance("test", strategy).getInstance();
    }
}
