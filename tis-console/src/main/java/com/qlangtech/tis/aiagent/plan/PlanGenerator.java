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
package com.qlangtech.tis.aiagent.plan;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.plugin.IEndTypeGetter;

/**
 * 任务计划生成器
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public class PlanGenerator {

  public static String KEY_SOURCE = "source";
  public static String KEY_TARGET = "target";
  public static String KEY_TYPE = "type";
  public static String KEY_EXTRACT_INFO = "extractInfo";

  private final LLMProvider llmProvider;

  public PlanGenerator(LLMProvider llmProvider) {
    this.llmProvider = llmProvider;
  }


  /**
   * 根据用户输入和LLM分析结果生成执行计划
   */
  public TaskPlan generatePlan(String userInput, JSONObject llmAnalysis) {

    JSONObject source = llmAnalysis.getJSONObject(PlanGenerator.KEY_SOURCE);
    JSONObject target = llmAnalysis.getJSONObject(PlanGenerator.KEY_TARGET);

    TaskPlan.DataEndCfg sourceEnd
      = new TaskPlan.DataEndCfg(IEndTypeGetter.EndType.valueOf(source.getString(PlanGenerator.KEY_TYPE)));
    sourceEnd.setRelevantDesc(source.getString(PlanGenerator.KEY_EXTRACT_INFO));

    TaskPlan.DataEndCfg targetEnd
      = new TaskPlan.DataEndCfg(IEndTypeGetter.EndType.valueOf(target.getString(PlanGenerator.KEY_TYPE)));
    targetEnd.setRelevantDesc(target.getString(PlanGenerator.KEY_EXTRACT_INFO));

    TaskPlan plan = new TaskPlan(sourceEnd, targetEnd, this.llmProvider);
    plan.setUserInput(userInput);

    //  String sourceType = ;
    //String targetType = llmAnalysis.getString("target_type");
//    plan.setSourceEnd(sourceEnd);
//    plan.setTargetEnd(targetEnd);

    // 根据源端和目标端类型，生成对应的执行步骤
//    if (plan.getSourceEnd().getType() == IEndTypeGetter.EndType.MySQL
//      && plan.getTargetEnd().getType() == IEndTypeGetter.EndType.Paimon) {
//      generateMySQLToPaimonPlan(plan, llmAnalysis.getJSONObject("options"));
//    } else {
    // 其他类型的同步计划
    generateGenericPlan(plan, llmAnalysis);
    //}

    return plan;
  }

  /**
   * 生成MySQL到Paimon的同步计划
   */
  private void generateMySQLToPaimonPlan(TaskPlan plan, JSONObject options) {
//    JSONObject sourceConfig = llmAnalysis.getJSONObject("source_config");
//    JSONObject targetConfig = llmAnalysis.getJSONObject("target_config");
//    JSONObject options = llmAnalysis.getJSONObject("options");

    // Step 1: 检查并安装插件
    TaskStep installStep = new TaskStep("检查并安装必要插件", TaskStep.StepType.PLUGIN_INSTALL);
    installStep.setDescription("检查" + plan.getSourceEnd() + " Reader和" + plan.getTargetEnd() + " Writer插件是否已安装");
    plan.addStep(installStep);

    // Step 2: 创建MySQL数据源
    TaskStep mysqlDsStep = new TaskStep("创建MySQL数据源", TaskStep.StepType.PLUGIN_CREATE);
    mysqlDsStep.setPluginImpl("com.qlangtech.tis.plugin.ds.mysql.MySQLV5DataSourceFactory");
    mysqlDsStep.setDescription("配置MySQL数据源连接信息");

    JSONObject mysqlDsConfig = new JSONObject();
//    mysqlDsConfig.put("host", sourceConfig.getString("host"));
//    mysqlDsConfig.put("port", sourceConfig.getIntValue("port"));
//    mysqlDsConfig.put("username", sourceConfig.getString("username"));
//    mysqlDsConfig.put("password", sourceConfig.getString("password"));
//    mysqlDsConfig.put("database", sourceConfig.getString("database"));
    mysqlDsStep.setPluginConfig(mysqlDsConfig);
    plan.addStep(mysqlDsStep);

    // Step 3: 创建MySQL Reader
    TaskStep mysqlReaderStep = new TaskStep("创建MySQL Reader", TaskStep.StepType.PLUGIN_CREATE);
    mysqlReaderStep.setPluginImpl("com.qlangtech.tis.plugin.datax.DataxMySQLReader");
    mysqlReaderStep.setDescription("配置MySQL数据读取器");

    JSONObject readerConfig = new JSONObject();
//    readerConfig.put("dbName", sourceConfig.getString("database"));
//    readerConfig.put("splitPk", false);
//    readerConfig.put("fetchSize", 2000);
    mysqlReaderStep.setPluginConfig(readerConfig);
    plan.addStep(mysqlReaderStep);

    // Step 4: 创建HDFS配置
    TaskStep hdfsStep = new TaskStep("配置HDFS资源", TaskStep.StepType.PLUGIN_CREATE);
    hdfsStep.setPluginImpl("com.qlangtech.tis.hdfs.impl.HdfsFileSystemFactory");
    hdfsStep.setDescription("配置HDFS文件系统");
    plan.addStep(hdfsStep);

    // Step 5: 创建Hive数据源
    TaskStep hiveDsStep = new TaskStep("创建Hive数据源", TaskStep.StepType.PLUGIN_CREATE);
    hiveDsStep.setPluginImpl("com.qlangtech.tis.hive.Hiveserver2DataSourceFactory");
    hiveDsStep.setDescription("配置Hive MetaStore连接");

    JSONObject hiveConfig = new JSONObject();
//    hiveConfig.put("host", targetConfig.getString("host"));
//    hiveConfig.put("database", targetConfig.getString("database"));
    hiveDsStep.setPluginConfig(hiveConfig);
    plan.addStep(hiveDsStep);

    // Step 6: 创建Paimon Writer
    TaskStep paimonWriterStep = new TaskStep("创建Paimon Writer", TaskStep.StepType.PLUGIN_CREATE);
    paimonWriterStep.setPluginImpl("com.qlangtech.tis.plugin.paimon.datax.DataxPaimonWriter");
    paimonWriterStep.setDescription("配置Paimon数据写入器");
    plan.addStep(paimonWriterStep);

    // Step 7: 选择同步表
    TaskStep selectTablesStep = new TaskStep("选择同步表", TaskStep.StepType.SELECT_TABLES);
    selectTablesStep.setDescription("选择需要同步的数据表");
    selectTablesStep.setRequireUserConfirm(true);
    plan.addStep(selectTablesStep);

    // Step 8: 执行批量同步（可选）
    if (options != null && options.getBooleanValue("execute_batch")) {
      TaskStep batchStep = new TaskStep("执行批量历史数据同步", TaskStep.StepType.EXECUTE_BATCH);
      batchStep.setDescription("同步历史存量数据");
      plan.addStep(batchStep);
    }

    // Step 9: 配置增量同步（可选）
    if (options != null && options.getBooleanValue("enable_incr")) {
      // 创建MySQL Binlog监听
      TaskStep binlogStep = new TaskStep("配置MySQL Binlog监听", TaskStep.StepType.PLUGIN_CREATE);
      binlogStep.setPluginImpl("com.qlangtech.tis.plugins.incr.flink.cdc.mysql.FlinkCDCMySQLSourceFactory");
      binlogStep.setDescription("配置MySQL实时变更数据捕获");
      plan.addStep(binlogStep);

      // 创建Paimon实时写入
      TaskStep paimonIncrStep = new TaskStep("配置Paimon实时写入", TaskStep.StepType.PLUGIN_CREATE);
      paimonIncrStep.setPluginImpl("com.qlangtech.tis.plugins.incr.flink.pipeline.paimon.sink.PaimonPipelineSinkFactory");
      paimonIncrStep.setDescription("配置Paimon实时数据写入");
      plan.addStep(paimonIncrStep);

      // 启动增量同步
      TaskStep startIncrStep = new TaskStep("启动增量同步任务", TaskStep.StepType.EXECUTE_INCR);
      startIncrStep.setDescription("启动实时增量数据同步");
      plan.addStep(startIncrStep);
    }
  }

  /**
   * 生成通用同步计划
   */
  private void generateGenericPlan(TaskPlan plan, JSONObject llmAnalysis) {
    // 基础步骤：检查插件、创建Reader、创建Writer、执行同步

    TaskStep installStep = new TaskStep("检查并安装必要插件", TaskStep.StepType.PLUGIN_INSTALL);
    plan.addStep(installStep);

    TaskStep readerStep = new TaskStep("创建数据源Reader、Writer插件实例", TaskStep.StepType.PLUGIN_CREATE);
    readerStep.setDescription("配置源端数据读取器及写入器");
    plan.addStep(readerStep);

//    TaskStep writerStep = new TaskStep("创建目标端Writer", TaskStep.StepType.PLUGIN_CREATE);
//    writerStep.setDescription("配置目标端数据写入器");
//    plan.addStep(writerStep);

    TaskStep executeStep = new TaskStep("执行数据同步", TaskStep.StepType.EXECUTE_BATCH);
    executeStep.setDescription("执行数据同步任务");
    plan.addStep(executeStep);
  }
}
