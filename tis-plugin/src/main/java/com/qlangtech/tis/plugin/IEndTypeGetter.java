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

package com.qlangtech.tis.plugin;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.manage.common.IEnumTypeSer;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.OptionWithEndType;
import com.qlangtech.tis.plugin.ontology.OntologyType;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 端类型
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-03-05 10:13
 **/
public interface IEndTypeGetter {

    String EndTypeDesc = "endTypeDesc";
    String EndTypeKey = "key";
    String EndTypeDescription = "description";

    /**
     * 供应商
     *
     * @return
     */
    default PluginVender getVender() {
        return PluginVender.TIS;
    }

    enum PluginVender {
        FLINK_CDC("FlinkCDC", "flink-cdc", "https://ververica.github.io/flink-cdc-connectors") //
        , CHUNJUN("Chunjun", "chunjun", "https://dtstack.github.io/chunjun") //
        , TIS("TIS", "tis", "https://github.com/qlangtech/tis") //
        , DATAX("DataX", "datax", "https://github.com/alibaba/DataX");
        final String name;
        final String tokenId;
        final String url;

        private PluginVender(String name, String tokenId, String url) {
            this.name = name;
            this.tokenId = tokenId;
            this.url = url;
        }

        public String getTokenId() {
            return this.tokenId;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }


    interface IEndType {
        public String getVal();

        public Optional<String> getDesc();

        public Icon getIcon();
    }


    public static void main(String[] args) {
        for (EndType value : EndType.values()) {
            if (!value.containICON) {
                System.out.println(value.val + ",");
            }

        }
    }

    /**
     * 取得数据端类型
     *
     * @return
     */
    public default EndType getEndType() {
        return EndType.Blank;
    }

    enum EndTypeCategory {
        Data, Assist, // 报警类别
        Alert, Transformer, // 图标
        Icon, DataType, Ontology
    }

    public static List<Option> excludeDataType() {
        return OptionWithEndType.createEnumOptions(EndType.class, (e) -> {
            return e instanceof EndType endType && endType.category != EndTypeCategory.Data;
        });
    }

    /**
     * 端类型
     */
    enum EndType implements IEndType, IEndTypeGetter, DescriptorUseableShortComment, IEnumTypeSer {
        // Greenplum("greenplum", EndTypeCategory.Data)
        MySQL("mysql", EndTypeCategory.Data, true, Optional.of("开源关系型数据库，官网：https://www.mysql.com/")) //
        , OceanBase("oceanbase", EndTypeCategory.Data, true,
                Optional.of("阿里巴巴自研分布式数据库，官网：https://www.oceanbase.com/")) //
        , Paimon("paimon", EndTypeCategory.Data, true, Optional.of("Apache流批统一存储，官网：https://paimon.apache.org/")) //
        , MariaDB("mariaDB", EndTypeCategory.Data, true, Optional.of("MySQL社区分支，官网：https://mariadb.org/")) //
        , Postgres("pg", EndTypeCategory.Data, true, Optional.of("PostgreSQL对象关系型数据库，官网：https://www.postgresql.org/"))//
        , Oracle("oracle", EndTypeCategory.Data, true, Optional.of("甲骨文公司商业数据库，官网：https://www.oracle.com/database/")) //
        , ElasticSearch("es", EndTypeCategory.Data, true, Optional.of("Elastic搜索分析引擎，官网：https://www.elastic.co/")) //
        , MongoDB("mongoDB", EndTypeCategory.Data, true, Optional.of("文档型NoSQL数据库，官网：https://www.mongodb.com/")) //
        , StarRocks("starRocks", EndTypeCategory.Data, true, Optional.of("极速MPP分析数据库，官网：https://www.starrocks.io/"))//
        , Doris("doris", EndTypeCategory.Data, true, Optional.of("Apache实时分析数据库，官网：https://doris.apache.org/"), 1) //
        , KingBase("kingbase", EndTypeCategory.Data, true, Optional.of("人大金仓国产数据库，官网：https://www.kingbase.com.cn/")) //
        , Clickhouse("clickhouse", EndTypeCategory.Data, true,
                Optional.of("Yandex列式分析数据库，官网：https://clickhouse.com/")) //
        , Hudi("hudi", EndTypeCategory.Data, true, Optional.of("Apache增量数据湖，官网：https://hudi.apache.org/")) //
        , TDFS("t-dfs", EndTypeCategory.Data, true, Optional.of("本地文本、阿里云OSS、HDFS、FTP数据（支持text，csv等格式）")) //
        // , Cassandra("cassandra", EndTypeCategory.Data, true, Optional.of("Apache分布式NoSQL，官网：https://cassandra.apache.org/"))
        // , HDFS("hdfs")
        , SqlServer("sqlServer", EndTypeCategory.Data, true, Optional.of("微软关系型数据库，官网：https://www.microsoft"
                + ".com/sql-server/")) //
        , TiDB("TiDB", EndTypeCategory.Data, true, Optional.of("PingCAP NewSQL数据库，官网：https://www.pingcap.com/")) //
        , RocketMQ("rocketMq", EndTypeCategory.Data, true
                , Optional.of("Apache分布式消息系统，官网：https://rocketmq.apache.org/")) //
        , Kafka("kafka", EndTypeCategory.Data, true, Optional.of("Apache流处理平台，官网：https://kafka.apache.org/")) //
        //, DataFlow("dataflow", EndTypeCategory.Assist) //
        , DaMeng("daMeng", EndTypeCategory.Data, true, Optional.of("达梦数据库，https://www.dameng.com/")) //
        , AliyunODPS("aliyunOdps", EndTypeCategory.Data, true
                , Optional.of("阿里云MaxCompute，官网：https://www.aliyun.com/product/odps")) //
        , HiveMetaStore("hms", EndTypeCategory.Data, true
                , Optional.of("Hive元数据服务，官网：https://hive.apache.org/")) //
        , Spark("spark", EndTypeCategory.Data, true
                , Optional.of("Apache统一分析引擎，官网：https://spark.apache.org/")) //
        , RabbitMQ("rabbitmq", EndTypeCategory.Data, true
                , Optional.of("Erlang消息队列，官网：https://www.rabbitmq.com/")) //

        , HDFS("hdfs", EndTypeCategory.Assist, true) //
        , AmazonS3("s3", EndTypeCategory.Assist, true) //
        , PowerJob("powerjob", EndTypeCategory.Assist, true) //
        , Flink("flink", EndTypeCategory.Assist, true)//
        , RateController("rate-controller", EndTypeCategory.Assist, true)//
        , Docker("docker", EndTypeCategory.Assist, true) //
        , K8S("k8s", EndTypeCategory.Assist, true)//
        , Dolphinscheduler("ds", EndTypeCategory.Assist, true) //
        , Deepseek("deepseek", EndTypeCategory.Assist, true) //
        , Anthropic("anthropic", EndTypeCategory.Assist, true) //
        , OpenAI("openai", EndTypeCategory.Assist, true) //
        , QWen("qwen", EndTypeCategory.Assist, true) //
        , Zhipu("zhipu", EndTypeCategory.Assist, true) //
        , Kimi("kimi", EndTypeCategory.Assist, true) //
        , Baichuan("baichuan", EndTypeCategory.Assist, true) //
        , Yi("yi_model", EndTypeCategory.Assist, true) //
        , Mistral("mistral", EndTypeCategory.Assist, true) //
        , Grok("grok", EndTypeCategory.Assist, true) //
        , Doubao("doubao", EndTypeCategory.Assist, true) //
        , Hunyuan("hunyuan", EndTypeCategory.Assist, true) //
        , Gemini("gemini", EndTypeCategory.Assist, true) //
        , MiniMax("minimax", EndTypeCategory.Assist, true) //
        , Ernie("ernie", EndTypeCategory.Assist, true) //
        , LLM_Mimo("llm_mimo", EndTypeCategory.Assist, true) //
        , UserProfile("user-profile", EndTypeCategory.Assist, true) //
        , Pipeline("pipeline", EndTypeCategory.Assist, true) //
        , Workflow("workflow", EndTypeCategory.Assist, true) //
        /**
         * alter channel
         */
        , DingTalk("dingding", EndTypeCategory.Alert, true) //
        , WeCom("weCom", EndTypeCategory.Alert, true) //
        // 飞书
        , Lark("lark", EndTypeCategory.Alert, true) //
        , Email("email", EndTypeCategory.Alert, true) //
        , Http("http", EndTypeCategory.Alert, true) //
        , Alert("tis-alert", EndTypeCategory.Alert, true) //
        //
        //

        , HttpProxy("http-proxy", EndTypeCategory.Assist, true) //
        , Aliyun("aliyun", EndTypeCategory.Assist, true) //

        // 预览按钮
        , UnKnowStoreType("unknowStoreType", EndTypeCategory.Icon, true) //
        , BatchComputing("batch-computing", EndTypeCategory.Icon, true)//
        , StreamComputing("stream-computing", EndTypeCategory.Icon, true) //
        , BliBli("blibli", EndTypeCategory.Icon, true) //
        , Hand("hand", EndTypeCategory.Icon, true) //
        , Preview("preview", EndTypeCategory.Icon, true) //
        , Clone("clone", EndTypeCategory.Icon, true) //
        , Blank("blank", EndTypeCategory.Icon, true) //
        , Stop("tis-stop", EndTypeCategory.Icon, true) //
        , Link("tis-link", EndTypeCategory.Icon, true) //
        , Ontology("ontology", EndTypeCategory.Icon, true, Optional.empty(), 1) //
        , Keys("keys", EndTypeCategory.Icon, true)//
        , Table("table", EndTypeCategory.Icon, true)//
        , DataBase("database", EndTypeCategory.Icon, true)//
        , ChatBI("chat-bi", EndTypeCategory.Icon, true)//
        , Forbiden("forbiden", EndTypeCategory.Icon, true)//
        , File("file", EndTypeCategory.Icon, true)//
        , Bucket("bucket", EndTypeCategory.Icon, true)//
        , PivotTable("pivot-table", EndTypeCategory.Icon, true)//
        , Gantt("gantt", EndTypeCategory.Icon, true)//
        , AlignCenter("align-center", EndTypeCategory.Icon, false)//
        , AlignLeft("align-left", EndTypeCategory.Icon, false)//
        , AlignRight("align-right", EndTypeCategory.Icon, false)//
        // Workshop 页面布局模板图标，与 plugins/tis-ontology-plugin 的 PageTemplate 枚举
        // 一一对应（示意原为 page-template-picker.component.ts 中的纯 CSS 版本）
        , LayoutBlank("layout-blank", EndTypeCategory.Icon, true)//
        , LayoutDetails("layout-details", EndTypeCategory.Icon, true)//
        , LayoutGrid("layout-grid", EndTypeCategory.Icon, true)//
        , LayoutInbox("layout-inbox", EndTypeCategory.Icon, true)//
        , LayoutOverview("layout-overview", EndTypeCategory.Icon, true)//
        , LayoutSettings("layout-settings", EndTypeCategory.Icon, true)//
        , LayoutTwoColumn("layout-two-column", EndTypeCategory.Icon, true)//
        , LayoutThreeColumn("layout-three-column", EndTypeCategory.Icon, true)//

        // Workshop Widget 调色板图标 —— 与 plugins/tis-ontology-plugin 的 WorkShopWidgetType
        // 一一对应（每个 Widget 一条）。取值的合法性由那个枚举的类型系统保证：它的 icon 字段
        // 是这里声明的 EndType，写错一个名字编译期就挂。
        //
        // 为什么这批一律 containICON=false：它们的 val 同时是合法的 Ant Design 图标名，
        // 前端构建期已把 @ant-design/icons-angular 的 inline-svg 整体拷进 assets/
        // （见 tis-console/angular.json），NgZorroIconService 按名动态加载即可 ——
        // 再往 endtype/icon/ 下放一份同名 svg 是纯粹的第二份拷贝。
        // 仅 OBJECT_TABLE / PIVOT_TABLE / GANTT_CHART 例外：它们复用上文已有的
        // Table / PivotTable / Gantt，那三条是自绘 svg（containICON=true）。
        //
        // ⚠️ 加这里的一条时，val 必须同时是合法的 Ant Design 图标名，否则前端
        // `<i nz-icon [nzType]="...">` 静默渲染成空（ng-zorro 只 log 不抛）。
        // 上面这 17 条只是「有 Widget 在用」，不代表可用范围 —— 本枚举末尾那段
        // 「Ant Design 图标全集」才是：挑图标时去那里找，别在这里加。
        , UnorderedList("unordered-list", EndTypeCategory.Icon, false)//
        , Eye("eye", EndTypeCategory.Icon, false)//
        , Profile("profile", EndTypeCategory.Icon, false)//
        , FontSize("font-size", EndTypeCategory.Icon, false)//
        , LineChart("line-chart", EndTypeCategory.Icon, false)//
        , PieChart("pie-chart", EndTypeCategory.Icon, false)//
        , Dashboard("dashboard", EndTypeCategory.Icon, false)//
        , FieldTime("field-time", EndTypeCategory.Icon, false)//
        , FileMarkdown("file-markdown", EndTypeCategory.Icon, false)//
        , Filter("filter", EndTypeCategory.Icon, false)//
        , DownSquare("down-square", EndTypeCategory.Icon, false)//
        , Form("form", EndTypeCategory.Icon, false)//
        , Calendar("calendar", EndTypeCategory.Icon, false)//
        , CheckSquare("check-square", EndTypeCategory.Icon, false)//
        , Block("block", EndTypeCategory.Icon, false)//
        , Bars("bars", EndTypeCategory.Icon, false)//
        , PlaySquare("play-square", EndTypeCategory.Icon, false)//


        /**
         * data type for ontology
         * @see OntologyType
         */
        , DataTypeString("type_string", EndTypeCategory.DataType, true) //
        , DataTypeInteger("type_integer", EndTypeCategory.DataType, true) //
        , DataTypeTime("type_time", EndTypeCategory.DataType, true) //
        , DataTypeBoolean("type_boolean", EndTypeCategory.DataType, true) //
        , DataTypeByte("type_byte", EndTypeCategory.DataType, true)//
        , DataTypeDecimal("type_decimal", EndTypeCategory.DataType, true)//
        , DataTypeArray("type_array", EndTypeCategory.DataType, true)//
        , DataTypeStruct("type_struct", EndTypeCategory.DataType, true)//
        , DataTypeBlob("type_blob", EndTypeCategory.DataType, true)//
        // 地理相关
        , DataTypeGeo("type_geo", EndTypeCategory.DataType, true)//
        , DataTypeVector("type_vector", EndTypeCategory.DataType, true)//

        , Shared("shared", EndTypeCategory.Ontology, true)//
        , OntologyLink("ontology-link", EndTypeCategory.Ontology, true)//
        , OntologyValueType("ontology-value-type", EndTypeCategory.Ontology, true)//
        , OntologyObjectType("ontology-object-type", EndTypeCategory.Ontology, true)//
        , OntologyGlossary("ontology-glossary", EndTypeCategory.Ontology, true)//
        , OntologyAction("ontology-action", EndTypeCategory.Ontology, true)//
        , OntologyFunction("ontology-function", EndTypeCategory.Ontology, true)//
        , OntologyWorkshop("ontology-workshop", EndTypeCategory.Ontology, true)//
        , OntologyMetric("ontology-metric", EndTypeCategory.Icon, true) //
        , OntologyProperty("ontology-property", EndTypeCategory.Ontology, true)


        //
        , Replace("replace", EndTypeCategory.Transformer, true)//
        , Concat("concat", EndTypeCategory.Transformer, true)//
        , Mask("mask", EndTypeCategory.Transformer, true)//
        , Splitter("splitter", EndTypeCategory.Transformer, true)//
        , SubString("substr", EndTypeCategory.Transformer, true) //
        , AutoGen("auto-generate", EndTypeCategory.Transformer, true) //
        , Copy("clone", EndTypeCategory.Transformer, true) //
        , Constant("constant", EndTypeCategory.Transformer, true) //
        , License("license", EndTypeCategory.Icon, true) //
        , Crontab("crontab", EndTypeCategory.Icon, true)//

        // ==================================================================
        // Ant Design 图标全集（ng-zorro / @ant-design/icons-angular 17.0.0）
        //
        // 生成来源：tis-console/node_modules/@ant-design/icons-angular/src/inline-svg/ 下的
        // {outline,fill,twotone}/*.svg —— 正是 tis-console/angular.json 拷进 assets/ 的那一份，
        // 也就是 NgZorroIconService 动态加载时真正会去取的文件（没有 svg = 图标渲染不出来）。
        //
        // 粒度是「一个可渲染的 nzType 一条」：421 个 outline 名 + 218 个 -fill + 150 个 -twotone
        // = 789 条，减去已有 val 已覆盖的 25 条、再减去与已有常量撞名而跳过的 4 条
        // （alert / copy / link / stop —— 分别与 Alert("tis-alert")、Copy("clone")、
        // Link("tis-link")、Stop("tis-stop") 撞名），共 760 条。
        //
        // 一律 containICON=false：图标资源在前端 assets/ 里，不需要再往 endtype/icon/ 下放一份。
        //
        // ⚠️ val 必须逐字等于可渲染的 nzType，主题后缀不能省：
        //     outline 图标 → 裸名（nzType 默认主题即 outline，_parseIconType 会补上 "-o"）
        //     fill 图标    → 必须带 "-fill"（alreadyHasAThemeSuffix 认得它；少了后缀会去取
        //                    assets/<name>/ 而 404 —— 34 个品牌图标只有 fill 版，
        //                    如 qq-circle-fill、taobao-square-fill）
        //     twotone 图标 → 同理必须带 "-twotone"
        // 写错的后果是图标静默渲染成空：ng-zorro 只 log 不抛。
        //
        // ⚠️ 本段是机器生成的，不要手工维护。升级 icons-angular 后按上述目录重新生成，
        // 保持「一个 nzType 一条 / 按主题分组 / 组内字母序」三个约定。
        // ==================================================================
        // ---------- outline ----------
        , AccountBook("account-book", EndTypeCategory.Icon, false)//
        , Aim("aim", EndTypeCategory.Icon, false)//
        , Alibaba("alibaba", EndTypeCategory.Icon, false)//
        , Alipay("alipay", EndTypeCategory.Icon, false)//
        , AlipayCircle("alipay-circle", EndTypeCategory.Icon, false)//
        , Aliwangwang("aliwangwang", EndTypeCategory.Icon, false)//
        , Amazon("amazon", EndTypeCategory.Icon, false)//
        , Android("android", EndTypeCategory.Icon, false)//
        , AntCloud("ant-cloud", EndTypeCategory.Icon, false)//
        , AntDesign("ant-design", EndTypeCategory.Icon, false)//
        , Apartment("apartment", EndTypeCategory.Icon, false)//
        , Api("api", EndTypeCategory.Icon, false)//
        , Apple("apple", EndTypeCategory.Icon, false)//
        , Appstore("appstore", EndTypeCategory.Icon, false)//
        , AppstoreAdd("appstore-add", EndTypeCategory.Icon, false)//
        , AreaChart("area-chart", EndTypeCategory.Icon, false)//
        , ArrowDown("arrow-down", EndTypeCategory.Icon, false)//
        , ArrowLeft("arrow-left", EndTypeCategory.Icon, false)//
        , ArrowRight("arrow-right", EndTypeCategory.Icon, false)//
        , ArrowUp("arrow-up", EndTypeCategory.Icon, false)//
        , ArrowsAlt("arrows-alt", EndTypeCategory.Icon, false)//
        , Audio("audio", EndTypeCategory.Icon, false)//
        , AudioMuted("audio-muted", EndTypeCategory.Icon, false)//
        , Audit("audit", EndTypeCategory.Icon, false)//
        , Backward("backward", EndTypeCategory.Icon, false)//
        , Bank("bank", EndTypeCategory.Icon, false)//
        , BarChart("bar-chart", EndTypeCategory.Icon, false)//
        , Barcode("barcode", EndTypeCategory.Icon, false)//
        , Behance("behance", EndTypeCategory.Icon, false)//
        , BehanceSquare("behance-square", EndTypeCategory.Icon, false)//
        , Bell("bell", EndTypeCategory.Icon, false)//
        , BgColors("bg-colors", EndTypeCategory.Icon, false)//
        , Bold("bold", EndTypeCategory.Icon, false)//
        , Book("book", EndTypeCategory.Icon, false)//
        , Border("border", EndTypeCategory.Icon, false)//
        , BorderBottom("border-bottom", EndTypeCategory.Icon, false)//
        , BorderHorizontal("border-horizontal", EndTypeCategory.Icon, false)//
        , BorderInner("border-inner", EndTypeCategory.Icon, false)//
        , BorderLeft("border-left", EndTypeCategory.Icon, false)//
        , BorderOuter("border-outer", EndTypeCategory.Icon, false)//
        , BorderRight("border-right", EndTypeCategory.Icon, false)//
        , BorderTop("border-top", EndTypeCategory.Icon, false)//
        , BorderVerticle("border-verticle", EndTypeCategory.Icon, false)//
        , BorderlessTable("borderless-table", EndTypeCategory.Icon, false)//
        , BoxPlot("box-plot", EndTypeCategory.Icon, false)//
        , Branches("branches", EndTypeCategory.Icon, false)//
        , Bug("bug", EndTypeCategory.Icon, false)//
        , Build("build", EndTypeCategory.Icon, false)//
        , Bulb("bulb", EndTypeCategory.Icon, false)//
        , Calculator("calculator", EndTypeCategory.Icon, false)//
        , Camera("camera", EndTypeCategory.Icon, false)//
        , Car("car", EndTypeCategory.Icon, false)//
        , CaretDown("caret-down", EndTypeCategory.Icon, false)//
        , CaretLeft("caret-left", EndTypeCategory.Icon, false)//
        , CaretRight("caret-right", EndTypeCategory.Icon, false)//
        , CaretUp("caret-up", EndTypeCategory.Icon, false)//
        , CarryOut("carry-out", EndTypeCategory.Icon, false)//
        , Check("check", EndTypeCategory.Icon, false)//
        , CheckCircle("check-circle", EndTypeCategory.Icon, false)//
        , Chrome("chrome", EndTypeCategory.Icon, false)//
        , Ci("ci", EndTypeCategory.Icon, false)//
        , CiCircle("ci-circle", EndTypeCategory.Icon, false)//
        , Clear("clear", EndTypeCategory.Icon, false)//
        , ClockCircle("clock-circle", EndTypeCategory.Icon, false)//
        , Close("close", EndTypeCategory.Icon, false)//
        , CloseCircle("close-circle", EndTypeCategory.Icon, false)//
        , CloseSquare("close-square", EndTypeCategory.Icon, false)//
        , Cloud("cloud", EndTypeCategory.Icon, false)//
        , CloudDownload("cloud-download", EndTypeCategory.Icon, false)//
        , CloudServer("cloud-server", EndTypeCategory.Icon, false)//
        , CloudSync("cloud-sync", EndTypeCategory.Icon, false)//
        , CloudUpload("cloud-upload", EndTypeCategory.Icon, false)//
        , Cluster("cluster", EndTypeCategory.Icon, false)//
        , Code("code", EndTypeCategory.Icon, false)//
        , CodeSandbox("code-sandbox", EndTypeCategory.Icon, false)//
        , Codepen("codepen", EndTypeCategory.Icon, false)//
        , CodepenCircle("codepen-circle", EndTypeCategory.Icon, false)//
        , Coffee("coffee", EndTypeCategory.Icon, false)//
        , ColumnHeight("column-height", EndTypeCategory.Icon, false)//
        , ColumnWidth("column-width", EndTypeCategory.Icon, false)//
        , Comment("comment", EndTypeCategory.Icon, false)//
        , Compass("compass", EndTypeCategory.Icon, false)//
        , Compress("compress", EndTypeCategory.Icon, false)//
        , ConsoleSql("console-sql", EndTypeCategory.Icon, false)//
        , Contacts("contacts", EndTypeCategory.Icon, false)//
        , Container("container", EndTypeCategory.Icon, false)//
        , Control("control", EndTypeCategory.Icon, false)//
        , Copyright("copyright", EndTypeCategory.Icon, false)//
        , CopyrightCircle("copyright-circle", EndTypeCategory.Icon, false)//
        , CreditCard("credit-card", EndTypeCategory.Icon, false)//
        , Crown("crown", EndTypeCategory.Icon, false)//
        , CustomerService("customer-service", EndTypeCategory.Icon, false)//
        , Dash("dash", EndTypeCategory.Icon, false)//
        , Delete("delete", EndTypeCategory.Icon, false)//
        , DeleteColumn("delete-column", EndTypeCategory.Icon, false)//
        , DeleteRow("delete-row", EndTypeCategory.Icon, false)//
        , DeliveredProcedure("delivered-procedure", EndTypeCategory.Icon, false)//
        , DeploymentUnit("deployment-unit", EndTypeCategory.Icon, false)//
        , Desktop("desktop", EndTypeCategory.Icon, false)//
        , Diff("diff", EndTypeCategory.Icon, false)//
        , Dingtalk("dingtalk", EndTypeCategory.Icon, false)//
        , Disconnect("disconnect", EndTypeCategory.Icon, false)//
        , Dislike("dislike", EndTypeCategory.Icon, false)//
        , Dollar("dollar", EndTypeCategory.Icon, false)//
        , DollarCircle("dollar-circle", EndTypeCategory.Icon, false)//
        , DotChart("dot-chart", EndTypeCategory.Icon, false)//
        , DoubleLeft("double-left", EndTypeCategory.Icon, false)//
        , DoubleRight("double-right", EndTypeCategory.Icon, false)//
        , Down("down", EndTypeCategory.Icon, false)//
        , DownCircle("down-circle", EndTypeCategory.Icon, false)//
        , Download("download", EndTypeCategory.Icon, false)//
        , Drag("drag", EndTypeCategory.Icon, false)//
        , Dribbble("dribbble", EndTypeCategory.Icon, false)//
        , DribbbleSquare("dribbble-square", EndTypeCategory.Icon, false)//
        , Dropbox("dropbox", EndTypeCategory.Icon, false)//
        , Edit("edit", EndTypeCategory.Icon, false)//
        , Ellipsis("ellipsis", EndTypeCategory.Icon, false)//
        , Enter("enter", EndTypeCategory.Icon, false)//
        , Environment("environment", EndTypeCategory.Icon, false)//
        , Euro("euro", EndTypeCategory.Icon, false)//
        , EuroCircle("euro-circle", EndTypeCategory.Icon, false)//
        , Exception("exception", EndTypeCategory.Icon, false)//
        , Exclamation("exclamation", EndTypeCategory.Icon, false)//
        , ExclamationCircle("exclamation-circle", EndTypeCategory.Icon, false)//
        , Expand("expand", EndTypeCategory.Icon, false)//
        , ExpandAlt("expand-alt", EndTypeCategory.Icon, false)//
        , Experiment("experiment", EndTypeCategory.Icon, false)//
        , Export("export", EndTypeCategory.Icon, false)//
        , EyeInvisible("eye-invisible", EndTypeCategory.Icon, false)//
        , Facebook("facebook", EndTypeCategory.Icon, false)//
        , Fall("fall", EndTypeCategory.Icon, false)//
        , FastBackward("fast-backward", EndTypeCategory.Icon, false)//
        , FastForward("fast-forward", EndTypeCategory.Icon, false)//
        , FieldBinary("field-binary", EndTypeCategory.Icon, false)//
        , FieldNumber("field-number", EndTypeCategory.Icon, false)//
        , FieldString("field-string", EndTypeCategory.Icon, false)//
        , FileAdd("file-add", EndTypeCategory.Icon, false)//
        , FileDone("file-done", EndTypeCategory.Icon, false)//
        , FileExcel("file-excel", EndTypeCategory.Icon, false)//
        , FileExclamation("file-exclamation", EndTypeCategory.Icon, false)//
        , FileGif("file-gif", EndTypeCategory.Icon, false)//
        , FileImage("file-image", EndTypeCategory.Icon, false)//
        , FileJpg("file-jpg", EndTypeCategory.Icon, false)//
        , FilePdf("file-pdf", EndTypeCategory.Icon, false)//
        , FilePpt("file-ppt", EndTypeCategory.Icon, false)//
        , FileProtect("file-protect", EndTypeCategory.Icon, false)//
        , FileSearch("file-search", EndTypeCategory.Icon, false)//
        , FileSync("file-sync", EndTypeCategory.Icon, false)//
        , FileText("file-text", EndTypeCategory.Icon, false)//
        , FileUnknown("file-unknown", EndTypeCategory.Icon, false)//
        , FileWord("file-word", EndTypeCategory.Icon, false)//
        , FileZip("file-zip", EndTypeCategory.Icon, false)//
        , Fire("fire", EndTypeCategory.Icon, false)//
        , Flag("flag", EndTypeCategory.Icon, false)//
        , Folder("folder", EndTypeCategory.Icon, false)//
        , FolderAdd("folder-add", EndTypeCategory.Icon, false)//
        , FolderOpen("folder-open", EndTypeCategory.Icon, false)//
        , FolderView("folder-view", EndTypeCategory.Icon, false)//
        , FontColors("font-colors", EndTypeCategory.Icon, false)//
        , Fork("fork", EndTypeCategory.Icon, false)//
        , FormatPainter("format-painter", EndTypeCategory.Icon, false)//
        , Forward("forward", EndTypeCategory.Icon, false)//
        , Frown("frown", EndTypeCategory.Icon, false)//
        , Fullscreen("fullscreen", EndTypeCategory.Icon, false)//
        , FullscreenExit("fullscreen-exit", EndTypeCategory.Icon, false)//
        , Function("function", EndTypeCategory.Icon, false)//
        , Fund("fund", EndTypeCategory.Icon, false)//
        , FundProjectionScreen("fund-projection-screen", EndTypeCategory.Icon, false)//
        , FundView("fund-view", EndTypeCategory.Icon, false)//
        , FunnelPlot("funnel-plot", EndTypeCategory.Icon, false)//
        , Gateway("gateway", EndTypeCategory.Icon, false)//
        , Gif("gif", EndTypeCategory.Icon, false)//
        , Gift("gift", EndTypeCategory.Icon, false)//
        , Github("github", EndTypeCategory.Icon, false)//
        , Gitlab("gitlab", EndTypeCategory.Icon, false)//
        , Global("global", EndTypeCategory.Icon, false)//
        , Gold("gold", EndTypeCategory.Icon, false)//
        , Google("google", EndTypeCategory.Icon, false)//
        , GooglePlus("google-plus", EndTypeCategory.Icon, false)//
        , Group("group", EndTypeCategory.Icon, false)//
        , Hdd("hdd", EndTypeCategory.Icon, false)//
        , Heart("heart", EndTypeCategory.Icon, false)//
        , HeatMap("heat-map", EndTypeCategory.Icon, false)//
        , Highlight("highlight", EndTypeCategory.Icon, false)//
        , History("history", EndTypeCategory.Icon, false)//
        , Holder("holder", EndTypeCategory.Icon, false)//
        , Home("home", EndTypeCategory.Icon, false)//
        , Hourglass("hourglass", EndTypeCategory.Icon, false)//
        , Html5("html5", EndTypeCategory.Icon, false)//
        , Idcard("idcard", EndTypeCategory.Icon, false)//
        , Ie("ie", EndTypeCategory.Icon, false)//
        , Import("import", EndTypeCategory.Icon, false)//
        , Inbox("inbox", EndTypeCategory.Icon, false)//
        , Info("info", EndTypeCategory.Icon, false)//
        , InfoCircle("info-circle", EndTypeCategory.Icon, false)//
        , InsertRowAbove("insert-row-above", EndTypeCategory.Icon, false)//
        , InsertRowBelow("insert-row-below", EndTypeCategory.Icon, false)//
        , InsertRowLeft("insert-row-left", EndTypeCategory.Icon, false)//
        , InsertRowRight("insert-row-right", EndTypeCategory.Icon, false)//
        , Instagram("instagram", EndTypeCategory.Icon, false)//
        , Insurance("insurance", EndTypeCategory.Icon, false)//
        , Interaction("interaction", EndTypeCategory.Icon, false)//
        , IssuesClose("issues-close", EndTypeCategory.Icon, false)//
        , Italic("italic", EndTypeCategory.Icon, false)//
        , Key("key", EndTypeCategory.Icon, false)//
        , Laptop("laptop", EndTypeCategory.Icon, false)//
        , Layout("layout", EndTypeCategory.Icon, false)//
        , Left("left", EndTypeCategory.Icon, false)//
        , LeftCircle("left-circle", EndTypeCategory.Icon, false)//
        , LeftSquare("left-square", EndTypeCategory.Icon, false)//
        , Like("like", EndTypeCategory.Icon, false)//
        , Line("line", EndTypeCategory.Icon, false)//
        , LineHeight("line-height", EndTypeCategory.Icon, false)//
        , Linkedin("linkedin", EndTypeCategory.Icon, false)//
        , Loading("loading", EndTypeCategory.Icon, false)//
        , Loading3Quarters("loading-3-quarters", EndTypeCategory.Icon, false)//
        , Lock("lock", EndTypeCategory.Icon, false)//
        , Login("login", EndTypeCategory.Icon, false)//
        , Logout("logout", EndTypeCategory.Icon, false)//
        , MacCommand("mac-command", EndTypeCategory.Icon, false)//
        , Mail("mail", EndTypeCategory.Icon, false)//
        , Man("man", EndTypeCategory.Icon, false)//
        , MedicineBox("medicine-box", EndTypeCategory.Icon, false)//
        , Medium("medium", EndTypeCategory.Icon, false)//
        , MediumWorkmark("medium-workmark", EndTypeCategory.Icon, false)//
        , Meh("meh", EndTypeCategory.Icon, false)//
        , Menu("menu", EndTypeCategory.Icon, false)//
        , MenuFold("menu-fold", EndTypeCategory.Icon, false)//
        , MenuUnfold("menu-unfold", EndTypeCategory.Icon, false)//
        , MergeCells("merge-cells", EndTypeCategory.Icon, false)//
        , Message("message", EndTypeCategory.Icon, false)//
        , Minus("minus", EndTypeCategory.Icon, false)//
        , MinusCircle("minus-circle", EndTypeCategory.Icon, false)//
        , MinusSquare("minus-square", EndTypeCategory.Icon, false)//
        , Mobile("mobile", EndTypeCategory.Icon, false)//
        , MoneyCollect("money-collect", EndTypeCategory.Icon, false)//
        , Monitor("monitor", EndTypeCategory.Icon, false)//
        , More("more", EndTypeCategory.Icon, false)//
        , NodeCollapse("node-collapse", EndTypeCategory.Icon, false)//
        , NodeExpand("node-expand", EndTypeCategory.Icon, false)//
        , NodeIndex("node-index", EndTypeCategory.Icon, false)//
        , Notification("notification", EndTypeCategory.Icon, false)//
        , Number("number", EndTypeCategory.Icon, false)//
        , OneToOne("one-to-one", EndTypeCategory.Icon, false)//
        , OrderedList("ordered-list", EndTypeCategory.Icon, false)//
        , PaperClip("paper-clip", EndTypeCategory.Icon, false)//
        , Partition("partition", EndTypeCategory.Icon, false)//
        , Pause("pause", EndTypeCategory.Icon, false)//
        , PauseCircle("pause-circle", EndTypeCategory.Icon, false)//
        , PayCircle("pay-circle", EndTypeCategory.Icon, false)//
        , Percentage("percentage", EndTypeCategory.Icon, false)//
        , Phone("phone", EndTypeCategory.Icon, false)//
        , PicCenter("pic-center", EndTypeCategory.Icon, false)//
        , PicLeft("pic-left", EndTypeCategory.Icon, false)//
        , PicRight("pic-right", EndTypeCategory.Icon, false)//
        , Picture("picture", EndTypeCategory.Icon, false)//
        , PlayCircle("play-circle", EndTypeCategory.Icon, false)//
        , Plus("plus", EndTypeCategory.Icon, false)//
        , PlusCircle("plus-circle", EndTypeCategory.Icon, false)//
        , PlusSquare("plus-square", EndTypeCategory.Icon, false)//
        , Pound("pound", EndTypeCategory.Icon, false)//
        , PoundCircle("pound-circle", EndTypeCategory.Icon, false)//
        , Poweroff("poweroff", EndTypeCategory.Icon, false)//
        , Printer("printer", EndTypeCategory.Icon, false)//
        , Project("project", EndTypeCategory.Icon, false)//
        , PropertySafety("property-safety", EndTypeCategory.Icon, false)//
        , PullRequest("pull-request", EndTypeCategory.Icon, false)//
        , Pushpin("pushpin", EndTypeCategory.Icon, false)//
        , Qq("qq", EndTypeCategory.Icon, false)//
        , Qrcode("qrcode", EndTypeCategory.Icon, false)//
        , Question("question", EndTypeCategory.Icon, false)//
        , QuestionCircle("question-circle", EndTypeCategory.Icon, false)//
        , RadarChart("radar-chart", EndTypeCategory.Icon, false)//
        , RadiusBottomleft("radius-bottomleft", EndTypeCategory.Icon, false)//
        , RadiusBottomright("radius-bottomright", EndTypeCategory.Icon, false)//
        , RadiusSetting("radius-setting", EndTypeCategory.Icon, false)//
        , RadiusUpleft("radius-upleft", EndTypeCategory.Icon, false)//
        , RadiusUpright("radius-upright", EndTypeCategory.Icon, false)//
        , Read("read", EndTypeCategory.Icon, false)//
        , Reconciliation("reconciliation", EndTypeCategory.Icon, false)//
        , RedEnvelope("red-envelope", EndTypeCategory.Icon, false)//
        , Reddit("reddit", EndTypeCategory.Icon, false)//
        , Redo("redo", EndTypeCategory.Icon, false)//
        , Reload("reload", EndTypeCategory.Icon, false)//
        , Rest("rest", EndTypeCategory.Icon, false)//
        , Retweet("retweet", EndTypeCategory.Icon, false)//
        , Right("right", EndTypeCategory.Icon, false)//
        , RightCircle("right-circle", EndTypeCategory.Icon, false)//
        , RightSquare("right-square", EndTypeCategory.Icon, false)//
        , Rise("rise", EndTypeCategory.Icon, false)//
        , Robot("robot", EndTypeCategory.Icon, false)//
        , Rocket("rocket", EndTypeCategory.Icon, false)//
        , Rollback("rollback", EndTypeCategory.Icon, false)//
        , RotateLeft("rotate-left", EndTypeCategory.Icon, false)//
        , RotateRight("rotate-right", EndTypeCategory.Icon, false)//
        , Safety("safety", EndTypeCategory.Icon, false)//
        , SafetyCertificate("safety-certificate", EndTypeCategory.Icon, false)//
        , Save("save", EndTypeCategory.Icon, false)//
        , Scan("scan", EndTypeCategory.Icon, false)//
        , Schedule("schedule", EndTypeCategory.Icon, false)//
        , Scissor("scissor", EndTypeCategory.Icon, false)//
        , Search("search", EndTypeCategory.Icon, false)//
        , SecurityScan("security-scan", EndTypeCategory.Icon, false)//
        , Select("select", EndTypeCategory.Icon, false)//
        , Send("send", EndTypeCategory.Icon, false)//
        , Setting("setting", EndTypeCategory.Icon, false)//
        , Shake("shake", EndTypeCategory.Icon, false)//
        , ShareAlt("share-alt", EndTypeCategory.Icon, false)//
        , Shop("shop", EndTypeCategory.Icon, false)//
        , Shopping("shopping", EndTypeCategory.Icon, false)//
        , ShoppingCart("shopping-cart", EndTypeCategory.Icon, false)//
        , Shrink("shrink", EndTypeCategory.Icon, false)//
        , Sisternode("sisternode", EndTypeCategory.Icon, false)//
        , Sketch("sketch", EndTypeCategory.Icon, false)//
        , Skin("skin", EndTypeCategory.Icon, false)//
        , Skype("skype", EndTypeCategory.Icon, false)//
        , Slack("slack", EndTypeCategory.Icon, false)//
        , SlackSquare("slack-square", EndTypeCategory.Icon, false)//
        , Sliders("sliders", EndTypeCategory.Icon, false)//
        , SmallDash("small-dash", EndTypeCategory.Icon, false)//
        , Smile("smile", EndTypeCategory.Icon, false)//
        , Snippets("snippets", EndTypeCategory.Icon, false)//
        , Solution("solution", EndTypeCategory.Icon, false)//
        , SortAscending("sort-ascending", EndTypeCategory.Icon, false)//
        , SortDescending("sort-descending", EndTypeCategory.Icon, false)//
        , Sound("sound", EndTypeCategory.Icon, false)//
        , SplitCells("split-cells", EndTypeCategory.Icon, false)//
        , Star("star", EndTypeCategory.Icon, false)//
        , StepBackward("step-backward", EndTypeCategory.Icon, false)//
        , StepForward("step-forward", EndTypeCategory.Icon, false)//
        , Stock("stock", EndTypeCategory.Icon, false)//
        , Strikethrough("strikethrough", EndTypeCategory.Icon, false)//
        , Subnode("subnode", EndTypeCategory.Icon, false)//
        , Swap("swap", EndTypeCategory.Icon, false)//
        , SwapLeft("swap-left", EndTypeCategory.Icon, false)//
        , SwapRight("swap-right", EndTypeCategory.Icon, false)//
        , Switcher("switcher", EndTypeCategory.Icon, false)//
        , Sync("sync", EndTypeCategory.Icon, false)//
        , Tablet("tablet", EndTypeCategory.Icon, false)//
        , Tag("tag", EndTypeCategory.Icon, false)//
        , Tags("tags", EndTypeCategory.Icon, false)//
        , Taobao("taobao", EndTypeCategory.Icon, false)//
        , TaobaoCircle("taobao-circle", EndTypeCategory.Icon, false)//
        , Team("team", EndTypeCategory.Icon, false)//
        , Thunderbolt("thunderbolt", EndTypeCategory.Icon, false)//
        , ToTop("to-top", EndTypeCategory.Icon, false)//
        , Tool("tool", EndTypeCategory.Icon, false)//
        , Trademark("trademark", EndTypeCategory.Icon, false)//
        , TrademarkCircle("trademark-circle", EndTypeCategory.Icon, false)//
        , Transaction("transaction", EndTypeCategory.Icon, false)//
        , Translation("translation", EndTypeCategory.Icon, false)//
        , Trophy("trophy", EndTypeCategory.Icon, false)//
        , Twitter("twitter", EndTypeCategory.Icon, false)//
        , Underline("underline", EndTypeCategory.Icon, false)//
        , Undo("undo", EndTypeCategory.Icon, false)//
        , Ungroup("ungroup", EndTypeCategory.Icon, false)//
        , Unlock("unlock", EndTypeCategory.Icon, false)//
        , Up("up", EndTypeCategory.Icon, false)//
        , UpCircle("up-circle", EndTypeCategory.Icon, false)//
        , UpSquare("up-square", EndTypeCategory.Icon, false)//
        , Upload("upload", EndTypeCategory.Icon, false)//
        , Usb("usb", EndTypeCategory.Icon, false)//
        , User("user", EndTypeCategory.Icon, false)//
        , UserAdd("user-add", EndTypeCategory.Icon, false)//
        , UserDelete("user-delete", EndTypeCategory.Icon, false)//
        , UserSwitch("user-switch", EndTypeCategory.Icon, false)//
        , UsergroupAdd("usergroup-add", EndTypeCategory.Icon, false)//
        , UsergroupDelete("usergroup-delete", EndTypeCategory.Icon, false)//
        , Verified("verified", EndTypeCategory.Icon, false)//
        , VerticalAlignBottom("vertical-align-bottom", EndTypeCategory.Icon, false)//
        , VerticalAlignMiddle("vertical-align-middle", EndTypeCategory.Icon, false)//
        , VerticalAlignTop("vertical-align-top", EndTypeCategory.Icon, false)//
        , VerticalLeft("vertical-left", EndTypeCategory.Icon, false)//
        , VerticalRight("vertical-right", EndTypeCategory.Icon, false)//
        , VideoCamera("video-camera", EndTypeCategory.Icon, false)//
        , VideoCameraAdd("video-camera-add", EndTypeCategory.Icon, false)//
        , Wallet("wallet", EndTypeCategory.Icon, false)//
        , Warning("warning", EndTypeCategory.Icon, false)//
        , Wechat("wechat", EndTypeCategory.Icon, false)//
        , Weibo("weibo", EndTypeCategory.Icon, false)//
        , WeiboCircle("weibo-circle", EndTypeCategory.Icon, false)//
        , WeiboSquare("weibo-square", EndTypeCategory.Icon, false)//
        , WhatsApp("whats-app", EndTypeCategory.Icon, false)//
        , Wifi("wifi", EndTypeCategory.Icon, false)//
        , Windows("windows", EndTypeCategory.Icon, false)//
        , Woman("woman", EndTypeCategory.Icon, false)//
        , Yahoo("yahoo", EndTypeCategory.Icon, false)//
        , Youtube("youtube", EndTypeCategory.Icon, false)//
        , Yuque("yuque", EndTypeCategory.Icon, false)//
        , Zhihu("zhihu", EndTypeCategory.Icon, false)//
        , ZoomIn("zoom-in", EndTypeCategory.Icon, false)//
        , ZoomOut("zoom-out", EndTypeCategory.Icon, false)//

        // ---------- fill ----------
        , AccountBookFill("account-book-fill", EndTypeCategory.Icon, false)//
        , AlertFill("alert-fill", EndTypeCategory.Icon, false)//
        , AlipayCircleFill("alipay-circle-fill", EndTypeCategory.Icon, false)//
        , AlipaySquareFill("alipay-square-fill", EndTypeCategory.Icon, false)//
        , AliwangwangFill("aliwangwang-fill", EndTypeCategory.Icon, false)//
        , AmazonCircleFill("amazon-circle-fill", EndTypeCategory.Icon, false)//
        , AmazonSquareFill("amazon-square-fill", EndTypeCategory.Icon, false)//
        , AndroidFill("android-fill", EndTypeCategory.Icon, false)//
        , ApiFill("api-fill", EndTypeCategory.Icon, false)//
        , AppleFill("apple-fill", EndTypeCategory.Icon, false)//
        , AppstoreFill("appstore-fill", EndTypeCategory.Icon, false)//
        , AudioFill("audio-fill", EndTypeCategory.Icon, false)//
        , BackwardFill("backward-fill", EndTypeCategory.Icon, false)//
        , BankFill("bank-fill", EndTypeCategory.Icon, false)//
        , BehanceCircleFill("behance-circle-fill", EndTypeCategory.Icon, false)//
        , BehanceSquareFill("behance-square-fill", EndTypeCategory.Icon, false)//
        , BellFill("bell-fill", EndTypeCategory.Icon, false)//
        , BookFill("book-fill", EndTypeCategory.Icon, false)//
        , BoxPlotFill("box-plot-fill", EndTypeCategory.Icon, false)//
        , BugFill("bug-fill", EndTypeCategory.Icon, false)//
        , BuildFill("build-fill", EndTypeCategory.Icon, false)//
        , BulbFill("bulb-fill", EndTypeCategory.Icon, false)//
        , CalculatorFill("calculator-fill", EndTypeCategory.Icon, false)//
        , CalendarFill("calendar-fill", EndTypeCategory.Icon, false)//
        , CameraFill("camera-fill", EndTypeCategory.Icon, false)//
        , CarFill("car-fill", EndTypeCategory.Icon, false)//
        , CaretDownFill("caret-down-fill", EndTypeCategory.Icon, false)//
        , CaretLeftFill("caret-left-fill", EndTypeCategory.Icon, false)//
        , CaretRightFill("caret-right-fill", EndTypeCategory.Icon, false)//
        , CaretUpFill("caret-up-fill", EndTypeCategory.Icon, false)//
        , CarryOutFill("carry-out-fill", EndTypeCategory.Icon, false)//
        , CheckCircleFill("check-circle-fill", EndTypeCategory.Icon, false)//
        , CheckSquareFill("check-square-fill", EndTypeCategory.Icon, false)//
        , ChromeFill("chrome-fill", EndTypeCategory.Icon, false)//
        , CiCircleFill("ci-circle-fill", EndTypeCategory.Icon, false)//
        , ClockCircleFill("clock-circle-fill", EndTypeCategory.Icon, false)//
        , CloseCircleFill("close-circle-fill", EndTypeCategory.Icon, false)//
        , CloseSquareFill("close-square-fill", EndTypeCategory.Icon, false)//
        , CloudFill("cloud-fill", EndTypeCategory.Icon, false)//
        , CodeFill("code-fill", EndTypeCategory.Icon, false)//
        , CodeSandboxCircleFill("code-sandbox-circle-fill", EndTypeCategory.Icon, false)//
        , CodeSandboxSquareFill("code-sandbox-square-fill", EndTypeCategory.Icon, false)//
        , CodepenCircleFill("codepen-circle-fill", EndTypeCategory.Icon, false)//
        , CodepenSquareFill("codepen-square-fill", EndTypeCategory.Icon, false)//
        , CompassFill("compass-fill", EndTypeCategory.Icon, false)//
        , ContactsFill("contacts-fill", EndTypeCategory.Icon, false)//
        , ContainerFill("container-fill", EndTypeCategory.Icon, false)//
        , ControlFill("control-fill", EndTypeCategory.Icon, false)//
        , CopyFill("copy-fill", EndTypeCategory.Icon, false)//
        , CopyrightCircleFill("copyright-circle-fill", EndTypeCategory.Icon, false)//
        , CreditCardFill("credit-card-fill", EndTypeCategory.Icon, false)//
        , CrownFill("crown-fill", EndTypeCategory.Icon, false)//
        , CustomerServiceFill("customer-service-fill", EndTypeCategory.Icon, false)//
        , DashboardFill("dashboard-fill", EndTypeCategory.Icon, false)//
        , DatabaseFill("database-fill", EndTypeCategory.Icon, false)//
        , DeleteFill("delete-fill", EndTypeCategory.Icon, false)//
        , DiffFill("diff-fill", EndTypeCategory.Icon, false)//
        , DingtalkCircleFill("dingtalk-circle-fill", EndTypeCategory.Icon, false)//
        , DingtalkSquareFill("dingtalk-square-fill", EndTypeCategory.Icon, false)//
        , DislikeFill("dislike-fill", EndTypeCategory.Icon, false)//
        , DollarCircleFill("dollar-circle-fill", EndTypeCategory.Icon, false)//
        , DownCircleFill("down-circle-fill", EndTypeCategory.Icon, false)//
        , DownSquareFill("down-square-fill", EndTypeCategory.Icon, false)//
        , DribbbleCircleFill("dribbble-circle-fill", EndTypeCategory.Icon, false)//
        , DribbbleSquareFill("dribbble-square-fill", EndTypeCategory.Icon, false)//
        , DropboxCircleFill("dropbox-circle-fill", EndTypeCategory.Icon, false)//
        , DropboxSquareFill("dropbox-square-fill", EndTypeCategory.Icon, false)//
        , EditFill("edit-fill", EndTypeCategory.Icon, false)//
        , EnvironmentFill("environment-fill", EndTypeCategory.Icon, false)//
        , EuroCircleFill("euro-circle-fill", EndTypeCategory.Icon, false)//
        , ExclamationCircleFill("exclamation-circle-fill", EndTypeCategory.Icon, false)//
        , ExperimentFill("experiment-fill", EndTypeCategory.Icon, false)//
        , EyeFill("eye-fill", EndTypeCategory.Icon, false)//
        , EyeInvisibleFill("eye-invisible-fill", EndTypeCategory.Icon, false)//
        , FacebookFill("facebook-fill", EndTypeCategory.Icon, false)//
        , FastBackwardFill("fast-backward-fill", EndTypeCategory.Icon, false)//
        , FastForwardFill("fast-forward-fill", EndTypeCategory.Icon, false)//
        , FileAddFill("file-add-fill", EndTypeCategory.Icon, false)//
        , FileExcelFill("file-excel-fill", EndTypeCategory.Icon, false)//
        , FileExclamationFill("file-exclamation-fill", EndTypeCategory.Icon, false)//
        , FileFill("file-fill", EndTypeCategory.Icon, false)//
        , FileImageFill("file-image-fill", EndTypeCategory.Icon, false)//
        , FileMarkdownFill("file-markdown-fill", EndTypeCategory.Icon, false)//
        , FilePdfFill("file-pdf-fill", EndTypeCategory.Icon, false)//
        , FilePptFill("file-ppt-fill", EndTypeCategory.Icon, false)//
        , FileTextFill("file-text-fill", EndTypeCategory.Icon, false)//
        , FileUnknownFill("file-unknown-fill", EndTypeCategory.Icon, false)//
        , FileWordFill("file-word-fill", EndTypeCategory.Icon, false)//
        , FileZipFill("file-zip-fill", EndTypeCategory.Icon, false)//
        , FilterFill("filter-fill", EndTypeCategory.Icon, false)//
        , FireFill("fire-fill", EndTypeCategory.Icon, false)//
        , FlagFill("flag-fill", EndTypeCategory.Icon, false)//
        , FolderAddFill("folder-add-fill", EndTypeCategory.Icon, false)//
        , FolderFill("folder-fill", EndTypeCategory.Icon, false)//
        , FolderOpenFill("folder-open-fill", EndTypeCategory.Icon, false)//
        , FormatPainterFill("format-painter-fill", EndTypeCategory.Icon, false)//
        , ForwardFill("forward-fill", EndTypeCategory.Icon, false)//
        , FrownFill("frown-fill", EndTypeCategory.Icon, false)//
        , FundFill("fund-fill", EndTypeCategory.Icon, false)//
        , FunnelPlotFill("funnel-plot-fill", EndTypeCategory.Icon, false)//
        , GiftFill("gift-fill", EndTypeCategory.Icon, false)//
        , GithubFill("github-fill", EndTypeCategory.Icon, false)//
        , GitlabFill("gitlab-fill", EndTypeCategory.Icon, false)//
        , GoldFill("gold-fill", EndTypeCategory.Icon, false)//
        , GoldenFill("golden-fill", EndTypeCategory.Icon, false)//
        , GoogleCircleFill("google-circle-fill", EndTypeCategory.Icon, false)//
        , GooglePlusCircleFill("google-plus-circle-fill", EndTypeCategory.Icon, false)//
        , GooglePlusSquareFill("google-plus-square-fill", EndTypeCategory.Icon, false)//
        , GoogleSquareFill("google-square-fill", EndTypeCategory.Icon, false)//
        , HddFill("hdd-fill", EndTypeCategory.Icon, false)//
        , HeartFill("heart-fill", EndTypeCategory.Icon, false)//
        , HighlightFill("highlight-fill", EndTypeCategory.Icon, false)//
        , HomeFill("home-fill", EndTypeCategory.Icon, false)//
        , HourglassFill("hourglass-fill", EndTypeCategory.Icon, false)//
        , Html5Fill("html5-fill", EndTypeCategory.Icon, false)//
        , IdcardFill("idcard-fill", EndTypeCategory.Icon, false)//
        , IeCircleFill("ie-circle-fill", EndTypeCategory.Icon, false)//
        , IeSquareFill("ie-square-fill", EndTypeCategory.Icon, false)//
        , InfoCircleFill("info-circle-fill", EndTypeCategory.Icon, false)//
        , InstagramFill("instagram-fill", EndTypeCategory.Icon, false)//
        , InsuranceFill("insurance-fill", EndTypeCategory.Icon, false)//
        , InteractionFill("interaction-fill", EndTypeCategory.Icon, false)//
        , LayoutFill("layout-fill", EndTypeCategory.Icon, false)//
        , LeftCircleFill("left-circle-fill", EndTypeCategory.Icon, false)//
        , LeftSquareFill("left-square-fill", EndTypeCategory.Icon, false)//
        , LikeFill("like-fill", EndTypeCategory.Icon, false)//
        , LinkedinFill("linkedin-fill", EndTypeCategory.Icon, false)//
        , LockFill("lock-fill", EndTypeCategory.Icon, false)//
        , MacCommandFill("mac-command-fill", EndTypeCategory.Icon, false)//
        , MailFill("mail-fill", EndTypeCategory.Icon, false)//
        , MedicineBoxFill("medicine-box-fill", EndTypeCategory.Icon, false)//
        , MediumCircleFill("medium-circle-fill", EndTypeCategory.Icon, false)//
        , MediumSquareFill("medium-square-fill", EndTypeCategory.Icon, false)//
        , MehFill("meh-fill", EndTypeCategory.Icon, false)//
        , MessageFill("message-fill", EndTypeCategory.Icon, false)//
        , MinusCircleFill("minus-circle-fill", EndTypeCategory.Icon, false)//
        , MinusSquareFill("minus-square-fill", EndTypeCategory.Icon, false)//
        , MobileFill("mobile-fill", EndTypeCategory.Icon, false)//
        , MoneyCollectFill("money-collect-fill", EndTypeCategory.Icon, false)//
        , NotificationFill("notification-fill", EndTypeCategory.Icon, false)//
        , PauseCircleFill("pause-circle-fill", EndTypeCategory.Icon, false)//
        , PayCircleFill("pay-circle-fill", EndTypeCategory.Icon, false)//
        , PhoneFill("phone-fill", EndTypeCategory.Icon, false)//
        , PictureFill("picture-fill", EndTypeCategory.Icon, false)//
        , PieChartFill("pie-chart-fill", EndTypeCategory.Icon, false)//
        , PlayCircleFill("play-circle-fill", EndTypeCategory.Icon, false)//
        , PlaySquareFill("play-square-fill", EndTypeCategory.Icon, false)//
        , PlusCircleFill("plus-circle-fill", EndTypeCategory.Icon, false)//
        , PlusSquareFill("plus-square-fill", EndTypeCategory.Icon, false)//
        , PoundCircleFill("pound-circle-fill", EndTypeCategory.Icon, false)//
        , PrinterFill("printer-fill", EndTypeCategory.Icon, false)//
        , ProfileFill("profile-fill", EndTypeCategory.Icon, false)//
        , ProjectFill("project-fill", EndTypeCategory.Icon, false)//
        , PropertySafetyFill("property-safety-fill", EndTypeCategory.Icon, false)//
        , PushpinFill("pushpin-fill", EndTypeCategory.Icon, false)//
        , QqCircleFill("qq-circle-fill", EndTypeCategory.Icon, false)//
        , QqSquareFill("qq-square-fill", EndTypeCategory.Icon, false)//
        , QuestionCircleFill("question-circle-fill", EndTypeCategory.Icon, false)//
        , ReadFill("read-fill", EndTypeCategory.Icon, false)//
        , ReconciliationFill("reconciliation-fill", EndTypeCategory.Icon, false)//
        , RedEnvelopeFill("red-envelope-fill", EndTypeCategory.Icon, false)//
        , RedditCircleFill("reddit-circle-fill", EndTypeCategory.Icon, false)//
        , RedditSquareFill("reddit-square-fill", EndTypeCategory.Icon, false)//
        , RestFill("rest-fill", EndTypeCategory.Icon, false)//
        , RightCircleFill("right-circle-fill", EndTypeCategory.Icon, false)//
        , RightSquareFill("right-square-fill", EndTypeCategory.Icon, false)//
        , RobotFill("robot-fill", EndTypeCategory.Icon, false)//
        , RocketFill("rocket-fill", EndTypeCategory.Icon, false)//
        , SafetyCertificateFill("safety-certificate-fill", EndTypeCategory.Icon, false)//
        , SaveFill("save-fill", EndTypeCategory.Icon, false)//
        , ScheduleFill("schedule-fill", EndTypeCategory.Icon, false)//
        , SecurityScanFill("security-scan-fill", EndTypeCategory.Icon, false)//
        , SettingFill("setting-fill", EndTypeCategory.Icon, false)//
        , ShopFill("shop-fill", EndTypeCategory.Icon, false)//
        , ShoppingFill("shopping-fill", EndTypeCategory.Icon, false)//
        , SignalFill("signal-fill", EndTypeCategory.Icon, false)//
        , SketchCircleFill("sketch-circle-fill", EndTypeCategory.Icon, false)//
        , SketchSquareFill("sketch-square-fill", EndTypeCategory.Icon, false)//
        , SkinFill("skin-fill", EndTypeCategory.Icon, false)//
        , SkypeFill("skype-fill", EndTypeCategory.Icon, false)//
        , SlackCircleFill("slack-circle-fill", EndTypeCategory.Icon, false)//
        , SlackSquareFill("slack-square-fill", EndTypeCategory.Icon, false)//
        , SlidersFill("sliders-fill", EndTypeCategory.Icon, false)//
        , SmileFill("smile-fill", EndTypeCategory.Icon, false)//
        , SnippetsFill("snippets-fill", EndTypeCategory.Icon, false)//
        , SoundFill("sound-fill", EndTypeCategory.Icon, false)//
        , StarFill("star-fill", EndTypeCategory.Icon, false)//
        , StepBackwardFill("step-backward-fill", EndTypeCategory.Icon, false)//
        , StepForwardFill("step-forward-fill", EndTypeCategory.Icon, false)//
        , StopFill("stop-fill", EndTypeCategory.Icon, false)//
        , SwitcherFill("switcher-fill", EndTypeCategory.Icon, false)//
        , TabletFill("tablet-fill", EndTypeCategory.Icon, false)//
        , TagFill("tag-fill", EndTypeCategory.Icon, false)//
        , TagsFill("tags-fill", EndTypeCategory.Icon, false)//
        , TaobaoCircleFill("taobao-circle-fill", EndTypeCategory.Icon, false)//
        , TaobaoSquareFill("taobao-square-fill", EndTypeCategory.Icon, false)//
        , ThunderboltFill("thunderbolt-fill", EndTypeCategory.Icon, false)//
        , ToolFill("tool-fill", EndTypeCategory.Icon, false)//
        , TrademarkCircleFill("trademark-circle-fill", EndTypeCategory.Icon, false)//
        , TrophyFill("trophy-fill", EndTypeCategory.Icon, false)//
        , TwitterCircleFill("twitter-circle-fill", EndTypeCategory.Icon, false)//
        , TwitterSquareFill("twitter-square-fill", EndTypeCategory.Icon, false)//
        , UnlockFill("unlock-fill", EndTypeCategory.Icon, false)//
        , UpCircleFill("up-circle-fill", EndTypeCategory.Icon, false)//
        , UpSquareFill("up-square-fill", EndTypeCategory.Icon, false)//
        , UsbFill("usb-fill", EndTypeCategory.Icon, false)//
        , VideoCameraFill("video-camera-fill", EndTypeCategory.Icon, false)//
        , WalletFill("wallet-fill", EndTypeCategory.Icon, false)//
        , WarningFill("warning-fill", EndTypeCategory.Icon, false)//
        , WechatFill("wechat-fill", EndTypeCategory.Icon, false)//
        , WeiboCircleFill("weibo-circle-fill", EndTypeCategory.Icon, false)//
        , WeiboSquareFill("weibo-square-fill", EndTypeCategory.Icon, false)//
        , WindowsFill("windows-fill", EndTypeCategory.Icon, false)//
        , YahooFill("yahoo-fill", EndTypeCategory.Icon, false)//
        , YoutubeFill("youtube-fill", EndTypeCategory.Icon, false)//
        , YuqueFill("yuque-fill", EndTypeCategory.Icon, false)//
        , ZhihuCircleFill("zhihu-circle-fill", EndTypeCategory.Icon, false)//
        , ZhihuSquareFill("zhihu-square-fill", EndTypeCategory.Icon, false)//

        // ---------- twotone ----------
        , AccountBookTwotone("account-book-twotone", EndTypeCategory.Icon, false)//
        , AlertTwotone("alert-twotone", EndTypeCategory.Icon, false)//
        , ApiTwotone("api-twotone", EndTypeCategory.Icon, false)//
        , AppstoreTwotone("appstore-twotone", EndTypeCategory.Icon, false)//
        , AudioTwotone("audio-twotone", EndTypeCategory.Icon, false)//
        , BankTwotone("bank-twotone", EndTypeCategory.Icon, false)//
        , BellTwotone("bell-twotone", EndTypeCategory.Icon, false)//
        , BookTwotone("book-twotone", EndTypeCategory.Icon, false)//
        , BoxPlotTwotone("box-plot-twotone", EndTypeCategory.Icon, false)//
        , BugTwotone("bug-twotone", EndTypeCategory.Icon, false)//
        , BuildTwotone("build-twotone", EndTypeCategory.Icon, false)//
        , BulbTwotone("bulb-twotone", EndTypeCategory.Icon, false)//
        , CalculatorTwotone("calculator-twotone", EndTypeCategory.Icon, false)//
        , CalendarTwotone("calendar-twotone", EndTypeCategory.Icon, false)//
        , CameraTwotone("camera-twotone", EndTypeCategory.Icon, false)//
        , CarTwotone("car-twotone", EndTypeCategory.Icon, false)//
        , CarryOutTwotone("carry-out-twotone", EndTypeCategory.Icon, false)//
        , CheckCircleTwotone("check-circle-twotone", EndTypeCategory.Icon, false)//
        , CheckSquareTwotone("check-square-twotone", EndTypeCategory.Icon, false)//
        , CiCircleTwotone("ci-circle-twotone", EndTypeCategory.Icon, false)//
        , CiTwotone("ci-twotone", EndTypeCategory.Icon, false)//
        , ClockCircleTwotone("clock-circle-twotone", EndTypeCategory.Icon, false)//
        , CloseCircleTwotone("close-circle-twotone", EndTypeCategory.Icon, false)//
        , CloseSquareTwotone("close-square-twotone", EndTypeCategory.Icon, false)//
        , CloudTwotone("cloud-twotone", EndTypeCategory.Icon, false)//
        , CodeTwotone("code-twotone", EndTypeCategory.Icon, false)//
        , CompassTwotone("compass-twotone", EndTypeCategory.Icon, false)//
        , ContactsTwotone("contacts-twotone", EndTypeCategory.Icon, false)//
        , ContainerTwotone("container-twotone", EndTypeCategory.Icon, false)//
        , ControlTwotone("control-twotone", EndTypeCategory.Icon, false)//
        , CopyTwotone("copy-twotone", EndTypeCategory.Icon, false)//
        , CopyrightCircleTwotone("copyright-circle-twotone", EndTypeCategory.Icon, false)//
        , CopyrightTwotone("copyright-twotone", EndTypeCategory.Icon, false)//
        , CreditCardTwotone("credit-card-twotone", EndTypeCategory.Icon, false)//
        , CrownTwotone("crown-twotone", EndTypeCategory.Icon, false)//
        , CustomerServiceTwotone("customer-service-twotone", EndTypeCategory.Icon, false)//
        , DashboardTwotone("dashboard-twotone", EndTypeCategory.Icon, false)//
        , DatabaseTwotone("database-twotone", EndTypeCategory.Icon, false)//
        , DeleteTwotone("delete-twotone", EndTypeCategory.Icon, false)//
        , DiffTwotone("diff-twotone", EndTypeCategory.Icon, false)//
        , DislikeTwotone("dislike-twotone", EndTypeCategory.Icon, false)//
        , DollarCircleTwotone("dollar-circle-twotone", EndTypeCategory.Icon, false)//
        , DollarTwotone("dollar-twotone", EndTypeCategory.Icon, false)//
        , DownCircleTwotone("down-circle-twotone", EndTypeCategory.Icon, false)//
        , DownSquareTwotone("down-square-twotone", EndTypeCategory.Icon, false)//
        , EditTwotone("edit-twotone", EndTypeCategory.Icon, false)//
        , EnvironmentTwotone("environment-twotone", EndTypeCategory.Icon, false)//
        , EuroCircleTwotone("euro-circle-twotone", EndTypeCategory.Icon, false)//
        , EuroTwotone("euro-twotone", EndTypeCategory.Icon, false)//
        , ExclamationCircleTwotone("exclamation-circle-twotone", EndTypeCategory.Icon, false)//
        , ExperimentTwotone("experiment-twotone", EndTypeCategory.Icon, false)//
        , EyeInvisibleTwotone("eye-invisible-twotone", EndTypeCategory.Icon, false)//
        , EyeTwotone("eye-twotone", EndTypeCategory.Icon, false)//
        , FileAddTwotone("file-add-twotone", EndTypeCategory.Icon, false)//
        , FileExcelTwotone("file-excel-twotone", EndTypeCategory.Icon, false)//
        , FileExclamationTwotone("file-exclamation-twotone", EndTypeCategory.Icon, false)//
        , FileImageTwotone("file-image-twotone", EndTypeCategory.Icon, false)//
        , FileMarkdownTwotone("file-markdown-twotone", EndTypeCategory.Icon, false)//
        , FilePdfTwotone("file-pdf-twotone", EndTypeCategory.Icon, false)//
        , FilePptTwotone("file-ppt-twotone", EndTypeCategory.Icon, false)//
        , FileTextTwotone("file-text-twotone", EndTypeCategory.Icon, false)//
        , FileTwotone("file-twotone", EndTypeCategory.Icon, false)//
        , FileUnknownTwotone("file-unknown-twotone", EndTypeCategory.Icon, false)//
        , FileWordTwotone("file-word-twotone", EndTypeCategory.Icon, false)//
        , FileZipTwotone("file-zip-twotone", EndTypeCategory.Icon, false)//
        , FilterTwotone("filter-twotone", EndTypeCategory.Icon, false)//
        , FireTwotone("fire-twotone", EndTypeCategory.Icon, false)//
        , FlagTwotone("flag-twotone", EndTypeCategory.Icon, false)//
        , FolderAddTwotone("folder-add-twotone", EndTypeCategory.Icon, false)//
        , FolderOpenTwotone("folder-open-twotone", EndTypeCategory.Icon, false)//
        , FolderTwotone("folder-twotone", EndTypeCategory.Icon, false)//
        , FrownTwotone("frown-twotone", EndTypeCategory.Icon, false)//
        , FundTwotone("fund-twotone", EndTypeCategory.Icon, false)//
        , FunnelPlotTwotone("funnel-plot-twotone", EndTypeCategory.Icon, false)//
        , GiftTwotone("gift-twotone", EndTypeCategory.Icon, false)//
        , GoldTwotone("gold-twotone", EndTypeCategory.Icon, false)//
        , HddTwotone("hdd-twotone", EndTypeCategory.Icon, false)//
        , HeartTwotone("heart-twotone", EndTypeCategory.Icon, false)//
        , HighlightTwotone("highlight-twotone", EndTypeCategory.Icon, false)//
        , HomeTwotone("home-twotone", EndTypeCategory.Icon, false)//
        , HourglassTwotone("hourglass-twotone", EndTypeCategory.Icon, false)//
        , Html5Twotone("html5-twotone", EndTypeCategory.Icon, false)//
        , IdcardTwotone("idcard-twotone", EndTypeCategory.Icon, false)//
        , InfoCircleTwotone("info-circle-twotone", EndTypeCategory.Icon, false)//
        , InsuranceTwotone("insurance-twotone", EndTypeCategory.Icon, false)//
        , InteractionTwotone("interaction-twotone", EndTypeCategory.Icon, false)//
        , LayoutTwotone("layout-twotone", EndTypeCategory.Icon, false)//
        , LeftCircleTwotone("left-circle-twotone", EndTypeCategory.Icon, false)//
        , LeftSquareTwotone("left-square-twotone", EndTypeCategory.Icon, false)//
        , LikeTwotone("like-twotone", EndTypeCategory.Icon, false)//
        , LockTwotone("lock-twotone", EndTypeCategory.Icon, false)//
        , MailTwotone("mail-twotone", EndTypeCategory.Icon, false)//
        , MedicineBoxTwotone("medicine-box-twotone", EndTypeCategory.Icon, false)//
        , MehTwotone("meh-twotone", EndTypeCategory.Icon, false)//
        , MessageTwotone("message-twotone", EndTypeCategory.Icon, false)//
        , MinusCircleTwotone("minus-circle-twotone", EndTypeCategory.Icon, false)//
        , MinusSquareTwotone("minus-square-twotone", EndTypeCategory.Icon, false)//
        , MobileTwotone("mobile-twotone", EndTypeCategory.Icon, false)//
        , MoneyCollectTwotone("money-collect-twotone", EndTypeCategory.Icon, false)//
        , NotificationTwotone("notification-twotone", EndTypeCategory.Icon, false)//
        , PauseCircleTwotone("pause-circle-twotone", EndTypeCategory.Icon, false)//
        , PhoneTwotone("phone-twotone", EndTypeCategory.Icon, false)//
        , PictureTwotone("picture-twotone", EndTypeCategory.Icon, false)//
        , PieChartTwotone("pie-chart-twotone", EndTypeCategory.Icon, false)//
        , PlayCircleTwotone("play-circle-twotone", EndTypeCategory.Icon, false)//
        , PlaySquareTwotone("play-square-twotone", EndTypeCategory.Icon, false)//
        , PlusCircleTwotone("plus-circle-twotone", EndTypeCategory.Icon, false)//
        , PlusSquareTwotone("plus-square-twotone", EndTypeCategory.Icon, false)//
        , PoundCircleTwotone("pound-circle-twotone", EndTypeCategory.Icon, false)//
        , PrinterTwotone("printer-twotone", EndTypeCategory.Icon, false)//
        , ProfileTwotone("profile-twotone", EndTypeCategory.Icon, false)//
        , ProjectTwotone("project-twotone", EndTypeCategory.Icon, false)//
        , PropertySafetyTwotone("property-safety-twotone", EndTypeCategory.Icon, false)//
        , PushpinTwotone("pushpin-twotone", EndTypeCategory.Icon, false)//
        , QuestionCircleTwotone("question-circle-twotone", EndTypeCategory.Icon, false)//
        , ReconciliationTwotone("reconciliation-twotone", EndTypeCategory.Icon, false)//
        , RedEnvelopeTwotone("red-envelope-twotone", EndTypeCategory.Icon, false)//
        , RestTwotone("rest-twotone", EndTypeCategory.Icon, false)//
        , RightCircleTwotone("right-circle-twotone", EndTypeCategory.Icon, false)//
        , RightSquareTwotone("right-square-twotone", EndTypeCategory.Icon, false)//
        , RocketTwotone("rocket-twotone", EndTypeCategory.Icon, false)//
        , SafetyCertificateTwotone("safety-certificate-twotone", EndTypeCategory.Icon, false)//
        , SaveTwotone("save-twotone", EndTypeCategory.Icon, false)//
        , ScheduleTwotone("schedule-twotone", EndTypeCategory.Icon, false)//
        , SecurityScanTwotone("security-scan-twotone", EndTypeCategory.Icon, false)//
        , SettingTwotone("setting-twotone", EndTypeCategory.Icon, false)//
        , ShopTwotone("shop-twotone", EndTypeCategory.Icon, false)//
        , ShoppingTwotone("shopping-twotone", EndTypeCategory.Icon, false)//
        , SkinTwotone("skin-twotone", EndTypeCategory.Icon, false)//
        , SlidersTwotone("sliders-twotone", EndTypeCategory.Icon, false)//
        , SmileTwotone("smile-twotone", EndTypeCategory.Icon, false)//
        , SnippetsTwotone("snippets-twotone", EndTypeCategory.Icon, false)//
        , SoundTwotone("sound-twotone", EndTypeCategory.Icon, false)//
        , StarTwotone("star-twotone", EndTypeCategory.Icon, false)//
        , StopTwotone("stop-twotone", EndTypeCategory.Icon, false)//
        , SwitcherTwotone("switcher-twotone", EndTypeCategory.Icon, false)//
        , TabletTwotone("tablet-twotone", EndTypeCategory.Icon, false)//
        , TagTwotone("tag-twotone", EndTypeCategory.Icon, false)//
        , TagsTwotone("tags-twotone", EndTypeCategory.Icon, false)//
        , ThunderboltTwotone("thunderbolt-twotone", EndTypeCategory.Icon, false)//
        , ToolTwotone("tool-twotone", EndTypeCategory.Icon, false)//
        , TrademarkCircleTwotone("trademark-circle-twotone", EndTypeCategory.Icon, false)//
        , TrophyTwotone("trophy-twotone", EndTypeCategory.Icon, false)//
        , UnlockTwotone("unlock-twotone", EndTypeCategory.Icon, false)//
        , UpCircleTwotone("up-circle-twotone", EndTypeCategory.Icon, false)//
        , UpSquareTwotone("up-square-twotone", EndTypeCategory.Icon, false)//
        , UsbTwotone("usb-twotone", EndTypeCategory.Icon, false)//
        , VideoCameraTwotone("video-camera-twotone", EndTypeCategory.Icon, false)//
        , WalletTwotone("wallet-twotone", EndTypeCategory.Icon, false)//
        , WarningTwotone("warning-twotone", EndTypeCategory.Icon, false);


        private final String val;
        private final boolean containICON;
        public final EndTypeCategory category;
        private final Optional<String> desc;
        public final int priority;

        private static final DefaultIconReference unknowStorageType = new DefaultIconReference(UnKnowStoreType);

        public static String KEY_END_TYPE_DESC = "endTypeDesc";
        public static String KEY_SUPPORT_ICON = "supportIcon";

        public static EndType parse(String endType) {
            return parse(endType, true, true);
        }

        public static EndType parse(String endType, boolean compareByVal, boolean validateNull) {
            for (EndType end : EndType.values()) {
                if ((compareByVal ? end.val : String.valueOf(end)).equals(endType)) {
                    return end;
                }
            }
            if (validateNull) {
                throw new IllegalStateException("illegal endType:" + endType);
            }
            return null;
        }

        private static Set<EndType> _dataEnds;
        private static Set<EndType> _assistTypes;
        private static Set<EndType> _transformerTypes;
        private static Set<EndType> _alertTypes;
        private static Set<EndType> _ontologyTypes;

        public static Set<EndType> getDataEnds() {
            if (_dataEnds == null) {
                _dataEnds = filterTypes2Set(EndTypeCategory.Data);
            }
            return _dataEnds;
        }

        public static Set<EndType> getOntologyEnds() {
            if (_ontologyTypes == null) {
                _ontologyTypes = filterTypes2Set(EndTypeCategory.Ontology);
            }
            return _ontologyTypes;
        }

        private static Set<EndType> filterTypes2Set(EndTypeCategory data) {
            return Arrays.stream(EndType.values()).filter((end) -> end.category == data).collect(Collectors.toSet());
        }


        public Optional<String> getDesc() {
            return Objects.requireNonNull(this.desc, "desc can not be null");
        }

        /**
         * 取得辅助组件类
         *
         * @return
         */
        public static Set<EndType> getAssistTypes() {
            if (_assistTypes == null) {
                _assistTypes = filterTypes2Set(EndTypeCategory.Assist);
            }
            return _assistTypes;
        }

        public static Set<EndType> getAlertTypes() {
            if (_alertTypes == null) {
                _alertTypes = filterTypes2Set(EndTypeCategory.Alert);
            }
            return _alertTypes;
        }

        public static Set<EndType> getTransformerTypes() {
            if (_transformerTypes == null) {
                _transformerTypes = filterTypes2Set(EndTypeCategory.Transformer);
            }
            return _transformerTypes;
        }

        EndType(String val, EndTypeCategory category) {
            this(val, category, false, Optional.empty(), 0);
        }

        EndType(String val, EndTypeCategory category, boolean containICON) {
            this(val, category, containICON, Optional.empty(), 0);
        }

        EndType(String val, EndTypeCategory category, boolean containICON, Optional<String> desc) {
            this(val, category, containICON, desc, 0);
        }

        /**
         *
         * @param val
         * @param category
         * @param containICON
         * @param desc
         * @param priority    优先级
         */
        EndType(String val, EndTypeCategory category, boolean containICON, Optional<String> desc, int priority) {
            this.val = val;
            this.containICON = containICON;
            this.category = category;
            this.desc = desc;
            this.priority = priority;
        }


        @Override
        public String getVal() {
            return this.val;
        }

        private Icon icon;

        @Override
        public Icon getIcon() {


            if (icon == null) {

                //                if (!this.containICON) {
                //                    return (icon = unknowStorageType);
                //                }

                icon = new Icon() {
                    private String loadIconWithSuffix(String theme, boolean throwErr) {
                        if (!containICON) {
                            return null;
                        }
                        return IOUtils.loadResourceFromClasspath(IEndTypeGetter.class,
                                "endtype/icon/" + val + "/" + theme + ".svg", throwErr);
                    }

                    @Override
                    public boolean setRes(JSONObject icon, boolean fillStyle) {
                        String iconContent = fillStyle ? this.fillType() : this.outlineType();
                        icon.put("icon", iconContent);
                        return StringUtils.isNotEmpty(iconContent);
                    }

                    @Override
                    public String fillType() {
                        return loadIconWithSuffix("fill", true);
                    }

                    @Override
                    public String outlineType() {
                        return loadIconWithSuffix("outline", false);
                    }
                };
            }
            return icon;
        }

        public boolean containIn(Set<String> endTypes) {
            return endTypes.contains(this.getVal());
        }

        @Override
        public EndType getEndType() {
            return this;
        }

        @Override
        public String shortComment() {
            return this.val;
        }

        @Override
        public String valueToken() {
            return this.val;
        }
    }


    public interface Icon {
        public String fillType();

        public String outlineType();

        public boolean setRes(JSONObject icon, boolean fillStyle);//{
        //            if (isRef) {
        //                icon.put("ref", ((IconReference) i).endType().getVal());
        //            } else {
        //                icon.put("icon", i.fillType());
        //            }
        //        }
    }

    public interface IconReference {
        public static String KEY_RESOURCE_REFERENCE = "ref";

        public EndType endType();
    }

    public static class DefaultIconReference implements Icon, IconReference {

        private final EndType endType;

        public DefaultIconReference(EndType endType) {
            this.endType = endType;
        }

        @Override
        public EndType endType() {
            return this.endType;
        }

        @Override
        public boolean setRes(JSONObject icon, boolean fillStyle) {
            icon.put(KEY_RESOURCE_REFERENCE, endType.getVal());
            return true;
        }

        @Override
        public String fillType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String outlineType() {
            throw new UnsupportedOperationException();
        }


    }
}
