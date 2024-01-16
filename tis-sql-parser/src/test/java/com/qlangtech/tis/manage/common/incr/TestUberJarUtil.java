/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.manage.common.incr;

import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.manage.common.incr.StreamContextConstant.TISRes;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-01-08 18:30
 **/
public class TestUberJarUtil extends TestCase {


    public void testGetStreamUberJarFile() {
        TargetResName resName = new TargetResName("mysql_mysql4");
        TISRes streamUberJarFile = UberJarUtil.getStreamUberJarFile(resName);
        Assert.assertNotNull(streamUberJarFile);
        streamUberJarFile.sync2Local(true);
    }
}
