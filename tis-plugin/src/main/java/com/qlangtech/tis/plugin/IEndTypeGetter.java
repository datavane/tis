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
        Data,
        Assist,
        Transformer,
        // 图标
        Icon
    }

    /**
     * 端类型
     */
    enum EndType implements IEndType {
        Greenplum("greenplum", EndTypeCategory.Data) //
        , MySQL("mysql", EndTypeCategory.Data, true) //
        , OceanBase("oceanbase", EndTypeCategory.Data, true) //
        , Paimon("paimon", EndTypeCategory.Data, true) //
        , MariaDB("mariaDB", EndTypeCategory.Data, true) //
        , Postgres("pg", EndTypeCategory.Data, true)//
        , Oracle("oracle", EndTypeCategory.Data, true) //
        , ElasticSearch("es", EndTypeCategory.Data, true) //
        , MongoDB("mongoDB", EndTypeCategory.Data, true) //
        , StarRocks("starRocks", EndTypeCategory.Data, true)//
        , Doris("doris", EndTypeCategory.Data, true) //
        , KingBase("kingbase", EndTypeCategory.Data, true) //
        , Clickhouse("clickhouse", EndTypeCategory.Data, true) //
        , Hudi("hudi", EndTypeCategory.Data, true) //
        , TDFS("t-dfs", EndTypeCategory.Data, true) //
        , Cassandra("cassandra", EndTypeCategory.Data) //, HDFS("hdfs")
        , SqlServer("sqlServer", EndTypeCategory.Data, true) //
        , TiDB("TiDB", EndTypeCategory.Data, true) //
        , RocketMQ("rocketMq", EndTypeCategory.Data, true) //
        , Kafka("kafka", EndTypeCategory.Data, true) //
        //, DataFlow("dataflow", EndTypeCategory.Assist) //
        , DaMeng("daMeng", EndTypeCategory.Data, true) //
        , AliyunODPS("aliyunOdps", EndTypeCategory.Data, true) //
        , HiveMetaStore("hms", EndTypeCategory.Data, true) //
        , Spark("spark", EndTypeCategory.Data, true) //
        , RabbitMQ("rabbitmq", EndTypeCategory.Data, true) //


        , PowerJob("powerjob", EndTypeCategory.Assist, true), Flink("flink", EndTypeCategory.Assist, true)//
        , RateController("rate-controller", EndTypeCategory.Assist, true)//
        , Docker("docker", EndTypeCategory.Assist, true) //
        , K8S("k8s", EndTypeCategory.Assist, true)//
        , Dolphinscheduler("ds", EndTypeCategory.Assist, true) //
        , Deepseek("deepseek", EndTypeCategory.Assist, true) //
        , QWen("qwen", EndTypeCategory.Assist, true) //
        , UserProfile("user-profile", EndTypeCategory.Assist, true), Pipeline("pipeline", EndTypeCategory.Assist, true), Workflow("workflow", EndTypeCategory.Assist, true)
        //
        //

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

        , License("license", EndTypeCategory.Icon, true);

        private final String val;
        private final boolean containICON;
        public
        final EndTypeCategory category;

        private static final DefaultIconReference unknowStorageType = new DefaultIconReference(UnKnowStoreType);

        public static String KEY_END_TYPE = "endType";
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

        public static Set<EndType> getDataEnds() {
            if (_dataEnds == null) {
                _dataEnds = filterTypes2Set(EndTypeCategory.Data);
            }
            return _dataEnds;
        }

        private static Set<EndType> filterTypes2Set(EndTypeCategory data) {
            return Arrays.stream(EndType.values())
                    .filter((end) -> end.category == data).collect(Collectors.toSet());
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

        public static Set<EndType> getTransformerTypes() {
            if (_transformerTypes == null) {
                _transformerTypes = filterTypes2Set(EndTypeCategory.Transformer);
            }
            return _transformerTypes;
        }

        EndType(String val, EndTypeCategory category) {
            this(val, category, false);
        }

        EndType(String val, EndTypeCategory category, boolean containICON) {
            this.val = val;
            this.containICON = containICON;
            this.category = category;
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
                        return IOUtils.loadResourceFromClasspath(IEndTypeGetter.class
                                , "endtype/icon/" + val + "/" + theme + ".svg", throwErr);
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
