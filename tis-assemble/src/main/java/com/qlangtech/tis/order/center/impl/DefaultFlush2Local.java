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

package com.qlangtech.tis.order.center.impl;

import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.fullbuild.phasestatus.IFlush2Local;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BasicPhaseStatus;

import java.io.File;
import java.util.Collections;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-04-15 18:38
 **/
public class DefaultFlush2Local implements IFlush2Local {
    private final XmlFile xmlFile;

    public DefaultFlush2Local(File localFile) {
        this.xmlFile = new XmlFile(localFile);
    }

    @Override
    public void write(BasicPhaseStatus status) throws Exception {
        xmlFile.write(status, Collections.emptySet());
    }

    @Override
    public BasicPhaseStatus loadPhase() throws Exception {
        return (BasicPhaseStatus) xmlFile.read();
    }
}
