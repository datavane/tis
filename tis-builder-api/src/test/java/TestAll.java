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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 该模块可离线运行的单元测试聚合套件(由测试验证脚本自动整理生成)。
 * 依赖数据库/本地文件/内网服务/Testcontainers 的用例见 TEST_SKIP_REPORT.md。
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    com.qlangtech.tis.AppTest.class,
    com.qlangtech.tis.coredefine.module.action.TestSpecification.class,
    com.qlangtech.tis.fs.TestIPath.class,
    com.qlangtech.tis.realtime.transfer.TestUnderlineUtils.class,
    com.qlangtech.tis.utils.TestTisMetaProps.class,
    com.qlangtech.tis.utils.TestUtils.class,
})
public class TestAll {
}
