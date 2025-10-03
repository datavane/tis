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

package com.qlangtech.tis.aiagent.core;

import com.qlangtech.tis.datax.job.SSERunnable;
import com.qlangtech.tis.manage.common.CenterResource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/18
 */
public class TestRealTISPlanAndExecuteAgent {

  @BeforeClass
  public static void initBefore() {
    CenterResource.setNotFetchFromCenterRepository();
  }

  @Test
  public void testExecuteSuccess() throws Exception {

    try (SSERunnable.SSEEventWriter printWriter = new SSERunnable.SSEEventWriter(new PrintWriter(new StringWriter()))) {
      AgentContext agentContext = new AgentContext(String.valueOf(UUID.randomUUID()), printWriter);
      TISPlanAndExecuteAgent executeAgent = new TISPlanAndExecuteAgent(agentContext);

      executeAgent.execute("我需要创建一个数据同步管道，从MySQL 同步到 Paimon 数据库，MySql 数据源，用户名为baisui，密码为123456，主机地址为192.168.28.200，端口为3306，数据库名称为order2\n" +
        "Paimon端的Hive配置为，db地址：192.168.28.200，db名称：default。同步管道创建完成自动触发历史数据同步，并开启增量同步，谢谢");
    }


  }
}
