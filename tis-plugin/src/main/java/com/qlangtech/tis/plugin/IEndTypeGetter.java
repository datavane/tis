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
import com.qlangtech.tis.extension.impl.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
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
            System.out.print(value.val + ",");
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
        Icon
    }

    /**
     * 端类型
     */
    enum EndType implements IEndType {
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
        , Doris("doris", EndTypeCategory.Data, true, Optional.of("Apache实时分析数据库，官网：https://doris.apache.org/")) //
        , KingBase("kingbase", EndTypeCategory.Data, true, Optional.of("人大金仓国产数据库，官网：https://www.kingbase.com.cn/")) //
        , Clickhouse("clickhouse", EndTypeCategory.Data, true,
                Optional.of("Yandex列式分析数据库，官网：https://clickhouse.com/")) //
        , Hudi("hudi", EndTypeCategory.Data, true, Optional.of("Apache增量数据湖，官网：https://hudi.apache.org/")) //
        , TDFS("t-dfs", EndTypeCategory.Data, true, Optional.of("本地文本、阿里云OSS、HDFS、FTP数据（支持text，csv等格式）")) //
        // , Cassandra("cassandra", EndTypeCategory.Data, true, Optional.of("Apache分布式NoSQL，官网：https://cassandra.apache.org/"))
        // , HDFS("hdfs")
        , SqlServer("sqlServer", EndTypeCategory.Data, true, Optional.of("微软关系型数据库，官网：https://www.microsoft" + ".com"
                + "/sql-server/")) //
        , TiDB("TiDB", EndTypeCategory.Data, true, Optional.of("PingCAP NewSQL数据库，官网：https://www.pingcap.com/")) //
        , RocketMQ("rocketMq", EndTypeCategory.Data, true, Optional.of("Apache分布式消息系统，官网：https://rocketmq.apache" +
                ".org/")) //
        , Kafka("kafka", EndTypeCategory.Data, true, Optional.of("Apache流处理平台，官网：https://kafka.apache.org/")) //
        //, DataFlow("dataflow", EndTypeCategory.Assist) //
        , DaMeng("daMeng", EndTypeCategory.Data, true, Optional.of("达梦数据库，https://www.dameng.com/")) //
        , AliyunODPS("aliyunOdps", EndTypeCategory.Data, true, Optional.of("阿里云MaxCompute，官网：https://www.aliyun" +
                ".com/product/odps")) //
        , HiveMetaStore("hms", EndTypeCategory.Data, true, Optional.of("Hive元数据服务，官网：https://hive.apache.org/")) //
        , Spark("spark", EndTypeCategory.Data, true, Optional.of("Apache统一分析引擎，官网：https://spark.apache.org/")) //
        , RabbitMQ("rabbitmq", EndTypeCategory.Data, true, Optional.of("Erlang消息队列，官网：https://www.rabbitmq.com/")) //


        , PowerJob("powerjob", EndTypeCategory.Assist, true) //
        , Flink("flink", EndTypeCategory.Assist, true)//
        , RateController("rate-controller", EndTypeCategory.Assist, true)//
        , Docker("docker", EndTypeCategory.Assist, true) //
        , K8S("k8s", EndTypeCategory.Assist, true)//
        , Dolphinscheduler("ds", EndTypeCategory.Assist, true) //
        , Deepseek("deepseek", EndTypeCategory.Assist, true) //
        , QWen("qwen", EndTypeCategory.Assist, true) //
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
        , Crontab("crontab", EndTypeCategory.Icon, true);

        private final String val;
        private final boolean containICON;
        public final EndTypeCategory category;
        private final Optional<String> desc;

        private static final DefaultIconReference unknowStorageType = new DefaultIconReference(UnKnowStoreType);

        public static String KEY_END_TYPE = "endType";
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

        public static Set<EndType> getDataEnds() {
            if (_dataEnds == null) {
                _dataEnds = filterTypes2Set(EndTypeCategory.Data);
            }
            return _dataEnds;
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
            this(val, category, false, Optional.empty());
        }

        EndType(String val, EndTypeCategory category, boolean containICON) {
            this(val, category, containICON, Optional.empty());
        }

        EndType(String val, EndTypeCategory category, boolean containICON, Optional<String> desc) {
            this.val = val;
            this.containICON = containICON;
            this.category = category;
            this.desc = desc;
        }


        @Override
        public String getVal() {
            return this.val;
        }

        private Icon icon;

        @Override
        public Icon getIcon() {


            if (icon == null) {

                if (!this.containICON) {
                    return (icon = unknowStorageType);
                }

                icon = new Icon() {
                    private String loadIconWithSuffix(String theme, boolean throwErr) {
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
