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
package com.qlangtech.tis.aiagent.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务模板注册表
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public class TaskTemplateRegistry {
    
    private final Map<String, TaskTemplate> templates = new HashMap<>();

    public TaskTemplateRegistry() {
        initTemplates();
    }

    /**
     * 初始化内置模板
     */
    private void initTemplates() {
        // MySQL to Paimon模板
        TaskTemplate mysqlToPaimon = new TaskTemplate();
        mysqlToPaimon.setId("mysql-to-paimon");
        mysqlToPaimon.setName("MySQL同步到Paimon");
        mysqlToPaimon.setDescription("创建MySQL到Paimon的数据同步管道");
        mysqlToPaimon.setSourceType("mysql");
        mysqlToPaimon.setTargetType("paimon");
        mysqlToPaimon.setSampleText("我需要创建一个数据同步管道，从MySQL同步到Paimon数据库，MySQL数据源用户名为root，密码为123456，主机地址为192.168.1.100，端口为3306，数据库名称为mydb。Paimon端的Hive配置为，地址：192.168.1.200，数据库名称：default。同步管道创建完成后自动触发历史数据同步，并开启增量同步。");
        templates.put(mysqlToPaimon.getId(), mysqlToPaimon);

        // MySQL to Doris模板
        TaskTemplate mysqlToDoris = new TaskTemplate();
        mysqlToDoris.setId("mysql-to-doris");
        mysqlToDoris.setName("MySQL同步到Doris");
        mysqlToDoris.setDescription("创建MySQL到Doris的数据同步管道");
        mysqlToDoris.setSourceType("mysql");
        mysqlToDoris.setTargetType("doris");
        mysqlToDoris.setSampleText("创建MySQL到Doris的数据同步管道，MySQL源端：host=192.168.1.10, port=3306, user=admin, password=pass123, database=orders。Doris目标端：host=192.168.1.20, port=9030, user=root, password=doris123。");
        templates.put(mysqlToDoris.getId(), mysqlToDoris);

        // PostgreSQL to StarRocks模板
        TaskTemplate pgToStarRocks = new TaskTemplate();
        pgToStarRocks.setId("postgresql-to-starrocks");
        pgToStarRocks.setName("PostgreSQL同步到StarRocks");
        pgToStarRocks.setDescription("创建PostgreSQL到StarRocks的数据同步管道");
        pgToStarRocks.setSourceType("postgresql");
        pgToStarRocks.setTargetType("starrocks");
        pgToStarRocks.setSampleText("配置PostgreSQL到StarRocks的实时同步，PG数据库位于10.0.1.5:5432，用户postgres，密码pg123，数据库analytics。StarRocks集群FE节点10.0.2.10:9030，用户admin。");
        templates.put(pgToStarRocks.getId(), pgToStarRocks);
    }

    /**
     * 获取所有模板
     */
    public List<TaskTemplate> getAllTemplates() {
        return new ArrayList<>(templates.values());
    }

    /**
     * 根据ID获取模板
     */
    public TaskTemplate getTemplate(String templateId) {
        return templates.get(templateId);
    }

    /**
     * 根据源端和目标端类型获取模板
     */
    public TaskTemplate findTemplate(String sourceType, String targetType) {
        for (TaskTemplate template : templates.values()) {
            if (template.getSourceType().equalsIgnoreCase(sourceType) && 
                template.getTargetType().equalsIgnoreCase(targetType)) {
                return template;
            }
        }
        return null;
    }

    /**
     * 注册新模板
     */
    public void registerTemplate(TaskTemplate template) {
        templates.put(template.getId(), template);
    }

    /**
     * 任务模板定义
     */
    public static class TaskTemplate {
        private String id;
        private String name;
        private String description;
        private String sourceType;
        private String targetType;
        private String sampleText;
        private Map<String, Object> defaultConfig;

        public TaskTemplate() {
            this.defaultConfig = new HashMap<>();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public String getTargetType() {
            return targetType;
        }

        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }

        public String getSampleText() {
            return sampleText;
        }

        public void setSampleText(String sampleText) {
            this.sampleText = sampleText;
        }

        public Map<String, Object> getDefaultConfig() {
            return defaultConfig;
        }

        public void setDefaultConfig(Map<String, Object> defaultConfig) {
            this.defaultConfig = defaultConfig;
        }
    }
}