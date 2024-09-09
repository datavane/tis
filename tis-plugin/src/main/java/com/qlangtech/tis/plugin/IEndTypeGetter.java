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

import java.util.Set;

/**
 * 端类型
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-03-05 10:13
 **/
public interface IEndTypeGetter {
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

    /**
     * 端类型
     */
    enum EndType implements IEndType {
        Greenplum("greenplum"), MySQL("mysql", true) //
        , MariaDB("mariaDB", true) //
        , Postgres("pg", true), Oracle("oracle", true) //
        , ElasticSearch("es", true), MongoDB("mongoDB", true) //
        , StarRocks("starRocks", true), Doris("doris", true) //
        , Clickhouse("clickhouse", true), Hudi("hudi", true) //, AliyunOSS("aliyunOSS")
        , TDFS("t-dfs", true) //
        , Cassandra("cassandra") //, HDFS("hdfs")
        , SqlServer("sqlServer", true), TiDB("TiDB", true) //
        , RocketMQ("rocketMq", true), Kafka("kafka", true), DataFlow("dataflow") //
        , DaMeng("daMeng", true), AliyunODPS("aliyunOdps"), HiveMetaStore("hms", true) //
        , Spark("spark", true) //
        , RabbitMQ("rabbitmq", true), UnKnowStoreType("unknowStoreType", true),

        PowerJob("powerjob", true),
        Flink("flink", true), Docker("docker", true), K8S("k8s", true),
        BliBli("blibli", true),
        StreamComputing("stream-computing", true),
        BatchComputing("batch-computing", true), Dolphinscheduler("ds", true)
        // 预览按钮
        , Preview("preview", true) //
        , Clone("clone", true) //
        , Blank("blank", true) //
        , Concat("concat", true)//
        , Mask("mask", true)//
        , Splitter("splitter", true)//
        , SubString("substr", true);

        private final String val;
        private final boolean containICON;

        private static final DefaultIconReference unknowStorageType = new DefaultIconReference(UnKnowStoreType);

        public static String KEY_END_TYPE = "endType";
        public static String KEY_SUPPORT_ICON = "supportIcon";

        public static EndType parse(String endType) {
            for (EndType end : EndType.values()) {
                if (end.val.equals(endType)) {
                    return end;
                }
            }
            throw new IllegalStateException("illegal endType:" + endType);
        }

        EndType(String val) {
            this(val, false);
        }

        EndType(String val, boolean containICON) {
            this.val = val;
            this.containICON = containICON;
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
