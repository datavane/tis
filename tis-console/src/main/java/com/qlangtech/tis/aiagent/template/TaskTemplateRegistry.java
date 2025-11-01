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
        mysqlToDoris.setSampleText("创建MySQL到Doris的数据同步管道，MySQL源端：host=192.168.28.200, port=3306, user=root, password=123456, database=order2。Doris目标端：host=192.168.28.200 ,port=9030, be节点地址为：192.168.28.200:8030, user=root, password=doris123。");
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

        // Oracle to Doris模板 - 企业级数仓场景
        TaskTemplate oracleToDoris = new TaskTemplate();
        oracleToDoris.setId("oracle-to-doris");
        oracleToDoris.setName("Oracle同步到Doris");
        oracleToDoris.setDescription("创建Oracle到Doris的企业级数据仓库同步管道");
        oracleToDoris.setSourceType("oracle");
        oracleToDoris.setTargetType("doris");
        oracleToDoris.setSampleText("搭建Oracle到Doris的实时数据同步管道。Oracle数据源配置：主机地址192.168.10.50，端口1521，服务名ORCL，用户名system，密码oracle123，需要同步的Schema为SALES和INVENTORY。Doris目标端配置：FE节点192.168.20.100:9030，BE节点192.168.20.101-103，用户名root，密码doris456，目标数据库warehouse。开启CDC实时捕获，使用Log Mining Strategy为redo_log_catalog模式，Poll Interval设置为500ms。Doris端配置StreamLoad参数，批量大小10000条，自动建表模式选择Unique模型，副本数3，分桶数20。同步完成后自动创建物化视图加速查询。");
        templates.put(oracleToDoris.getId(), oracleToDoris);

        // MySQL to ElasticSearch模板 - 全文搜索场景
        TaskTemplate mysqlToES = new TaskTemplate();
        mysqlToES.setId("mysql-to-elasticsearch");
        mysqlToES.setName("MySQL同步到ElasticSearch");
        mysqlToES.setDescription("创建MySQL到ElasticSearch的全文搜索索引同步管道");
        mysqlToES.setSourceType("mysql");
        mysqlToES.setTargetType("elasticsearch");
        mysqlToES.setSampleText("构建MySQL到ElasticSearch的实时搜索引擎。MySQL源端：主机192.168.1.10，端口3306，用户root，密码mysql123，数据库product_catalog，需要同步products三张表。ElasticSearch目标端：节点地址http://192.168.2.20:9200，认证方式user，用户名elastic，密码es789。索引配置：批量写入大小5000条，刷新间隔10秒，索引settings设置分片数5、副本数1。mapping配置：product_name和description字段使用ik分词器，price字段类型为double，created_at为date类型。完成后创建别名product_search指向新索引，支持零停机更新。开启实时同步，监听binlog变更事件，延迟控制在1秒内。");
        templates.put(mysqlToES.getId(), mysqlToES);

        // MongoDB to MySQL模板 - NoSQL到关系型迁移
        TaskTemplate mongoToMySQL = new TaskTemplate();
        mongoToMySQL.setId("mongodb-to-mysql");
        mongoToMySQL.setName("MongoDB同步到MySQL");
        mongoToMySQL.setDescription("创建MongoDB到MySQL的NoSQL数据关系化同步管道");
        mongoToMySQL.setSourceType("mongodb");
        mongoToMySQL.setTargetType("mysql");
        mongoToMySQL.setSampleText("实现MongoDB到MySQL的数据关系化转换。MongoDB源端：集群地址192.168.3.10:27017;192.168.3.11:27017;192.168.3.12:27017，数据库social_media，集合users、posts、comments，认证用户admin，密码mongo456。MySQL目标端：主机192.168.4.20，端口3306，用户root，密码mysql789，目标库relational_social。数据转换规则：MongoDB嵌套文档展开为关联表，数组字段转为子表，ObjectId映射为VARCHAR(24)。预读500条记录自动推断Schema。实时同步配置：监听MongoDB的oplog，起始位置INITIAL（全量+增量），补全策略FULL_CHANGE_LOG（包含before/after值），心跳间隔30秒。批量写入模式设为INSERT ON DUPLICATE KEY UPDATE，确保数据一致性。");
        templates.put(mongoToMySQL.getId(), mongoToMySQL);

        // SqlServer to PostgreSQL模板 - 数据库迁移场景
        TaskTemplate sqlserverToPG = new TaskTemplate();
        sqlserverToPG.setId("sqlserver-to-postgresql");
        sqlserverToPG.setName("SqlServer同步到PostgreSQL");
        sqlserverToPG.setDescription("创建SqlServer到PostgreSQL的异构数据库迁移管道");
        sqlserverToPG.setSourceType("sqlserver");
        sqlserverToPG.setTargetType("postgresql");
        sqlserverToPG.setSampleText("配置SqlServer到PostgreSQL的企业级数据库迁移。SqlServer源端：主机192.168.5.30，端口1433，实例名MSSQLSERVER，数据库ERP_PROD，Schema为dbo，用户sa，密码sqlserver123，启用分库分表支持。PostgreSQL目标端：主机192.168.6.40，端口5432，数据库erp_new，Schema为public，用户postgres，密码pg456。迁移配置：批量读取使用splitPk并行分片，每批fetchSize设为5000。数据类型映射：NVARCHAR转TEXT，DATETIME转TIMESTAMP，BIT转BOOLEAN。实时同步基于Transaction Log，起始选项Initial（全量+增量）。处理特殊字段：IDENTITY列转SERIAL，计算列跳过，空间数据类型使用PostGIS扩展。完成后执行VACUUM ANALYZE优化性能。");
        templates.put(sqlserverToPG.getId(), sqlserverToPG);

        // Kafka to Clickhouse模板 - 流式分析场景
        TaskTemplate kafkaToClickhouse = new TaskTemplate();
        kafkaToClickhouse.setId("kafka-to-clickhouse");
        kafkaToClickhouse.setName("Kafka同步到Clickhouse");
        kafkaToClickhouse.setDescription("创建Kafka到Clickhouse的实时流式分析管道");
        kafkaToClickhouse.setSourceType("kafka");
        kafkaToClickhouse.setTargetType("clickhouse");
        kafkaToClickhouse.setSampleText("搭建Kafka到Clickhouse的实时日志分析系统。Kafka源端：Bootstrap Servers为192.168.7.50:9092;192.168.7.51:9092;192.168.7.52:9092，消费Topic为app_logs、system_metrics、user_events，消费组ID为clickhouse_consumer，消息格式为JSON。认证配置：Protocol为SASL_PLAINTEXT，SASL机制PLAIN，用户kafka_user，密码kafka123。起始位置Earliest，自动推断字段类型。Clickhouse目标端：HTTP端口http://192.168.8.60:8123，用户default，密码ch789，数据库analytics。建表引擎使用MergeTree，分区键按天(toYYYYMMDD(timestamp))，排序键(timestamp, user_id)。批量写入：batchSize设为10000，batchByteSize为20MB。实时写入配置：语义保证exactly-once，并行度8，刷新间隔5秒。配置TTL自动清理30天前的数据。");
        templates.put(kafkaToClickhouse.getId(), kafkaToClickhouse);

        // MySQL to Kafka模板 - 事件发布场景
        TaskTemplate mysqlToKafka = new TaskTemplate();
        mysqlToKafka.setId("mysql-to-kafka");
        mysqlToKafka.setName("MySQL同步到Kafka");
        mysqlToKafka.setDescription("创建MySQL到Kafka的变更事件发布管道");
        mysqlToKafka.setSourceType("mysql");
        mysqlToKafka.setTargetType("kafka");
        mysqlToKafka.setSampleText("构建MySQL到Kafka的CDC事件流平台。MySQL源端：主机192.168.9.70，端口3306，用户root，密码mysql456，监控数据库order_service。开启分库分表支持，表名正则匹配order_\\d{4}。Kafka目标端：Bootstrap Servers为192.168.10.80:9092;192.168.10.81:9092，Topic使用动态路由{database}_{table}模式。消息格式选择Canal-JSON（包含DDL和DML信息）。生产者配置：认证Protocol为SASL_SSL，用户producer，密码prod123。可靠性参数：ACKs设为all，重试次数5，启用幂等性。压缩类型snappy，批量大小32KB，延迟时间100ms。实时CDC配置：监听binlog位点LATEST_OFFSET，事件过滤保留INSERT、UPDATE_AFTER、DELETE。并行度设置为4，确保顺序性使用表级别分区键。");
        templates.put(mysqlToKafka.getId(), mysqlToKafka);

        // Oracle to Paimon模板 - 数据湖场景
        TaskTemplate oracleToPaimon = new TaskTemplate();
        oracleToPaimon.setId("oracle-to-paimon");
        oracleToPaimon.setName("Oracle同步到Paimon");
        oracleToPaimon.setDescription("创建Oracle到Paimon的开放数据湖同步管道");
        oracleToPaimon.setSourceType("oracle");
        oracleToPaimon.setTargetType("paimon");
        oracleToPaimon.setSampleText("配置Oracle到Paimon的企业数据湖方案。Oracle源端：主机192.168.11.90，端口1521，ServiceName为PROD，用户system，密码oracle789，包含Schema：FINANCE。启用授权用户过滤，保留业务Schema。CDC配置：使用LogMiner，策略redo_log_catalog，Poll间隔200ms，捕获LOB字段。Paimon目标端：Hive Metastore地址192.168.12.100:9083，HDFS路径hdfs://192.168.12.101:9000/data/paimon，数据库datalake。写入优化：write-buffer-size设为512MB，target-file-size为256MB。分桶策略Dynamic Bucket，目标行数500万。写入模式Stream（低延迟15-30秒可见）。Compaction参数：min-file-num为3，sorted-run触发数5。Snapshot管理：保留最少20个，最多100个，时间保留24小时。开启Changelog Producer追踪所有变更，支持下游Flink/Spark流式消费。文件格式选择Parquet，启用Zstandard压缩。");
        templates.put(oracleToPaimon.getId(), oracleToPaimon);

        // PostgreSQL to ElasticSearch模板 - 地理空间搜索
        TaskTemplate pgToES = new TaskTemplate();
        pgToES.setId("postgresql-to-elasticsearch");
        pgToES.setName("PostgreSQL同步到ElasticSearch");
        pgToES.setDescription("创建PostgreSQL到ElasticSearch的地理空间搜索同步管道");
        pgToES.setSourceType("postgresql");
        pgToES.setTargetType("elasticsearch");
        pgToES.setSampleText("搭建PostgreSQL到ElasticSearch的地理位置搜索服务。PostgreSQL源端：主机192.168.13.110，端口5432，数据库location_service，Schema为public，用户postgres，密码pg789，同步表：stores、deliveries、user_locations。包含PostGIS空间数据类型。ElasticSearch目标端：集群节点http://192.168.14.120:9200;http://192.168.14.121:9200，认证用户elastic，密码es456。索引映射：geometry字段转geo_shape，point转geo_point，支持地理空间查询。索引设置：分片数6，副本数2，refresh_interval为1s。批量参数：batchSize为3000，discovery启用节点自动发现。实时同步：使用wal2json解码器（需预装插件），REPLICA IDENTITY设为FULL捕获完整变更。写入语义at-least-once，bulkFlushIntervalMs为5000，bulkFlushMaxActions为5000。完成后创建别名geo_search，支持半径搜索、多边形搜索等空间查询。");
        templates.put(pgToES.getId(), pgToES);

        // MySQL to MySQL跨区域同步模板
        TaskTemplate mysqlToMysqlCross = new TaskTemplate();
        mysqlToMysqlCross.setId("mysql-to-mysql-cross");
        mysqlToMysqlCross.setName("MySQL跨区域同步");
        mysqlToMysqlCross.setDescription("创建MySQL主从跨区域灾备同步管道");
        mysqlToMysqlCross.setSourceType("mysql");
        mysqlToMysqlCross.setTargetType("mysql");
        mysqlToMysqlCross.setSampleText("构建MySQL跨数据中心的灾备同步系统。主库配置：北京机房192.168.15.130，端口3306，用户repl_user，密码repl456，数据库business_core。开启分库分表，识别正则^(order|user)_\\d{4}$。从库配置：上海机房192.168.16.140，端口3306，用户root，密码mysql789。网络优化：启用压缩传输，连接超时30秒，编码UTF8MB4。批量同步：splitPk并行16个分片，fetchSize为8000，提升跨区域传输效率。实时同步：binlog独立监听资源隔离，起始位置LATEST_OFFSET。写入配置：writeMode为REPLACE保证幂等，batchSize为5000，并行度8。容错机制：失败重试3次，checkpoint间隔60秒。监控告警：延迟超过5秒触发告警。同步过滤：排除系统表、临时表、日志表。定期执行checksum校验确保数据一致性。");
        templates.put(mysqlToMysqlCross.getId(), mysqlToMysqlCross);

        // SqlServer to Doris模板 - BI分析场景
        TaskTemplate sqlserverToDoris = new TaskTemplate();
        sqlserverToDoris.setId("sqlserver-to-doris");
        sqlserverToDoris.setName("SqlServer同步到Doris");
        sqlserverToDoris.setDescription("创建SqlServer到Doris的BI实时分析管道");
        sqlserverToDoris.setSourceType("sqlserver");
        sqlserverToDoris.setTargetType("doris");
        sqlserverToDoris.setSampleText("配置SqlServer到Doris的实时BI分析平台。SqlServer源端：主机192.168.17.150，端口1433，实例BISERVER，数据库DW_SOURCE，Schema包括sales，用户bi_reader，密码sql789。启用分库分表，支持按月分区表fact_sales_202*。Doris目标端：FE负载均衡地址192.168.18.160:9030，BE节点192.168.18.161-165，用户admin，密码doris456，目标库bi_analytics。数据模型：事实表使用Aggregate模型，维度表使用Duplicate模型。分区策略：按天RANGE分区(sale_date)，分桶数30。StreamLoad优化：maxBatchRows为20000，maxBatchSize为200MB，column_separator为\\t。实时CDC：Transaction Log模式，Snapshot起始，包含Schema变更同步。并行写入：设置4个并发，连接超时60秒，socket超时180秒。后置处理：自动创建Rollup物化视图，加速聚合查询。数据保留策略：保留最近365天数据，自动清理历史分区。");
        templates.put(sqlserverToDoris.getId(), sqlserverToDoris);

        // Kafka to Paimon模板 - 流批一体场景
        TaskTemplate kafkaToPaimon = new TaskTemplate();
        kafkaToPaimon.setId("kafka-to-paimon");
        kafkaToPaimon.setName("Kafka同步到Paimon");
        kafkaToPaimon.setDescription("创建Kafka到Paimon的流批一体数据湖管道");
        kafkaToPaimon.setSourceType("kafka");
        kafkaToPaimon.setTargetType("paimon");
        kafkaToPaimon.setSampleText("搭建Kafka到Paimon的流批一体数据湖架构。Kafka源端：集群地址192.168.19.170:9092;192.168.19.171:9092;192.168.19.172:9092，Topic订阅：实时交易ods_transactions、用户行为ods_behaviors、系统日志ods_logs。消费组streaming_lake，消息格式Debezium-JSON（支持DELETE）。安全认证：SASL_SSL协议，Kerberos认证，keytab文件路径/etc/security/kafka.keytab。Paimon配置：Hive Metastore为192.168.20.180:9083，HDFS集群hdfs://namenode:8020/warehouse/streaming，catalog名streaming_catalog。表配置：Dynamic Bucket自适应，初始bucket数10，target-row-num为100万。写入模式Stream，checkpoint间隔30秒，write-buffer为256MB。文件格式ORC，snappy压缩。Compaction策略：sorted-run触发3，文件数触发5，size-ratio为1.5。Changelog模式all，记录完整变更历史。Snapshot保留：最少50个，最多500个，时间窗口7天。Tag管理：每小时创建一个tag，支持时间旅行查询。下游消费：同时支持Flink流计算和Spark批处理。");
        templates.put(kafkaToPaimon.getId(), kafkaToPaimon);

        // MongoDB to Clickhouse模板 - 日志分析场景
        TaskTemplate mongoToClickhouse = new TaskTemplate();
        mongoToClickhouse.setId("mongodb-to-clickhouse");
        mongoToClickhouse.setName("MongoDB同步到Clickhouse");
        mongoToClickhouse.setDescription("创建MongoDB到Clickhouse的日志分析同步管道");
        mongoToClickhouse.setSourceType("mongodb");
        mongoToClickhouse.setTargetType("clickhouse");
        mongoToClickhouse.setSampleText("配置MongoDB到Clickhouse的海量日志分析系统。MongoDB源端：副本集地址192.168.21.190:27017;192.168.21.191:27017;192.168.21.192:27017，数据库log_center，集合app_logs、error_logs、audit_logs、metrics。认证数据库admin，用户monitor，密码mongo789。预读1000条自动Schema推断。Clickhouse目标端：集群地址http://192.168.22.200:8123，分布式表配置，用户analytics，密码ch456，数据库logs_analysis。表引擎：使用ReplacingMergeTree去重，分区键toYYYYMM(log_date)，排序键(log_date, level, app_id)。数据转换：MongoDB的ObjectId转String，ISODate转DateTime，嵌套文档展平为列。批量写入：batchByteSize为50MB，batchSize为20000。实时同步：监听oplog，起始TIMESTAMP（最近1小时），补全策略UPDATE_LOOKUP减少传输量。并行度12充分利用集群。数据聚合：预聚合表按小时统计，使用SummingMergeTree。TTL配置：详细日志保留30天，聚合数据保留1年。监控指标：每分钟日志量、错误率、响应时间P99等实时计算。");
        templates.put(mongoToClickhouse.getId(), mongoToClickhouse);

        // Oracle to StarRocks模板 - 实时数仓场景
        TaskTemplate oracleToStarRocks = new TaskTemplate();
        oracleToStarRocks.setId("oracle-to-starrocks");
        oracleToStarRocks.setName("Oracle同步到StarRocks");
        oracleToStarRocks.setDescription("创建Oracle到StarRocks的实时数据仓库同步管道");
        oracleToStarRocks.setSourceType("oracle");
        oracleToStarRocks.setTargetType("starrocks");
        oracleToStarRocks.setSampleText("搭建Oracle到StarRocks的金融级实时数仓。Oracle源端：RAC集群scan地址192.168.23.210，端口1521，ServiceName为FINPROD，用户etl_user，密码oracle456。Schema范围：CORE_BANKING。会话参数：NLS_DATE_FORMAT='YYYY-MM-DD HH24:MI:SS'，NLS_TIMESTAMP_FORMAT包含时区。StarRocks目标端：FE高可用地址192.168.24.220:9030;192.168.24.221:9030，用户root，密码sr789，数据库finance_dw。数据模型选择：交易表Primary Key模型（支持更新），历史表Duplicate模型（追加）。分区分桶：RANGE分区按天(trade_date)，分桶键(account_id)，桶数50。StreamLoad参数：strict_mode为true严格模式，max_filter_ratio为0拒绝脏数据，timeout为600秒。CDC配置：LogMiner continuous模式，捕获DDL变更，LOB支持，心跳表监控延迟。写入优化：并行度16，loadProps设置column_separator='\\x01'避免冲突。物化视图：创建多个物化视图加速不同维度查询。监控告警：数据延迟超过30秒、错误率超过0.1%触发告警。");
        templates.put(oracleToStarRocks.getId(), oracleToStarRocks);

        // PostgreSQL to Kafka模板 - 事件驱动架构
        TaskTemplate pgToKafka = new TaskTemplate();
        pgToKafka.setId("postgresql-to-kafka");
        pgToKafka.setName("PostgreSQL同步到Kafka");
        pgToKafka.setDescription("创建PostgreSQL到Kafka的事件驱动架构同步管道");
        pgToKafka.setSourceType("postgresql");
        pgToKafka.setTargetType("kafka");
        pgToKafka.setSampleText("构建PostgreSQL到Kafka的微服务事件总线。PostgreSQL源端：主机192.168.25.230，端口5432，数据库microservices，Schema列表：orders、inventory、shipping、payment，用户cdc_user，密码pg456。需要预装wal2json插件支持。Kafka目标端：集群192.168.26.240:9092;192.168.26.241:9092;192.168.26.242:9092，Topic路由规则：postgres.{schema}.{table}。消息格式Debezium-JSON，包含完整Schema信息。生产者配置：SASL_PLAINTEXT认证，用户event_producer，密码kafka789。可靠性：ACKs=-1（全部副本确认），enable.idempotence=true（精确一次）。性能优化：compression.type=lz4，batch.size=64KB，linger.ms=50，buffer.memory=64MB。CDC配置：slot名称pg_to_kafka_slot，decoder使用wal2json，publication创建所有表。起始位置Latest，REPLICA IDENTITY FULL获取完整before值。事件过滤：只发送INSERT、UPDATE_AFTER、DELETE事件。分区策略：使用主键哈希分区保证顺序。监控指标：消息发送速率、延迟、积压量实时监控。下游消费者：支持多个微服务订阅不同Topic实现事件驱动。");
        templates.put(pgToKafka.getId(), pgToKafka);
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
