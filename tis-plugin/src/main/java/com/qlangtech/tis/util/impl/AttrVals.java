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

package com.qlangtech.tis.util.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.impl.BaseSubFormProperties;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.AttrValMap;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import static com.qlangtech.tis.extension.Descriptor.KEY_DESC_VAL;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_VALS;
import static com.qlangtech.tis.util.impl.PluginEqualResult.notEqual;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-08-12 21:55
 **/
public class AttrVals implements AttrValMap.IAttrVals {
    protected final Map<String, JSON> /*** attrName*/
            attrValMap;

    public AttrVals(Map<String, JSON> attrValMap) {
        this.attrValMap = attrValMap;
    }


    public static AttrVals parseAttrValMap(Object vals) {
        Map<String, JSON> attrValMap = Maps.newHashMap();
        if (vals == null) {
            return new AttrVals(attrValMap);
        }
        // Object vals = jsonObject.get("vals");
        if (vals instanceof Map) {
            ((Map<String, Object>) vals).forEach((attrName, val) -> {
                try {
                    attrValMap.put(attrName, (JSON) val);
                } catch (Exception e) {
                    // 在multiSelectItem的场景下，可能存在提交的itemProperty没有使用‘ItemPropVal’包装的情况
                    if (val instanceof String) {
                        JSONObject o = new JSONObject();
                        o.put(Descriptor.KEY_primaryVal, val);
                        attrValMap.put(attrName, o);
                    } else {
                        throw new RuntimeException("attrName:" + attrName + ",valType:" + val.getClass().getSimpleName(), e);
                    }
                }
            });
        }
        return new AttrVals(attrValMap);
    }

    @Override
    public Map<String, JSONObject> asRootFormVals() {
        Map<String, JSONObject> result = Maps.newHashMap();
        vistAttrValMap((key, val) -> {
            JSON j = val;
            if (!(j instanceof JSONObject)) {
                throw new IllegalStateException("type must be a object:\n" + JsonUtil.toString(j));
            }
            result.put(key, (JSONObject) val);
        });
        return result;
    }

    //    public AttrVals createNew(BiFunction<String, JSON, JSON> mapper) {
    //        Map<String, JSON> vals = new HashMap<>();
    //        this.vistAttrValMap((key, val) -> {
    //            vals.put(key, mapper.apply(key, val));
    //        });
    //        return new AttrVals(vals);
    //    }


    public JSONObject getAttrVal(String fieldName) {
        JSON prop = this.attrValMap.get(fieldName);
        if (prop != null && !(prop instanceof JSONObject)) {
            throw new IllegalStateException("type must be a object:\n" + JsonUtil.toString(prop));
        }
        return (JSONObject) prop;
    }

    public void setAttrVal(String fieldName, JSON prop) {
        this.attrValMap.put(fieldName, Objects.requireNonNull(prop,
                "fieldName:" + fieldName + " relevant prop can " + "not be null"));
        //        JSON prop = this.attrValMap.get(fieldName);
        //        if (prop != null && !(prop instanceof JSONObject)) {
        //            throw new IllegalStateException("type must be a object:\n" + JsonUtil.toString(prop));
        //        }
        //        return (JSONObject) prop;
    }

    public Object getPrimaryVal(String fieldName) {
        JSONObject attrVal = getAttrVal(fieldName);
        if (attrVal == null) {
            return null;
        }
        Object val = attrVal.get(Descriptor.KEY_primaryVal);
        if (val instanceof String) {
            return StringUtils.trimToNull((String) val);
        }
        return val;
    }

    public void setPrimaryVal(String fieldName, Object val) {
        if (val == null) {
            throw new IllegalArgumentException("field:" + fieldName + " relevant val can not be null");
        }
        JSONObject attrVal = getAttrVal(fieldName);
        Objects.requireNonNull(attrVal, "field:" + fieldName + " relevatn attrVal can not be null").put(Descriptor.KEY_primaryVal, val);
    }

    /**
     * 为了子表单可以同时支持多个descriptor的item提交
     * <pre>
     * vals:{
     *  tableName1:[
     *    {
     *      impl:"impl1"
     *      vals:{
     *         k1:v1,k2:v2
     *      }
     *    },
     *    {
     *      impl:"impl2"
     *      vals:{
     *         k1:v1,k2:v2
     *      }
     *    }
     *  ]
     * }
     *
     * </pre>
     *
     * @return
     */
    @Override
    public Map<String, JSONArray> asSubFormDetails() {
        Map<String, JSONArray> result = Maps.newHashMap();
        vistAttrValMap((key, val) -> {
            JSON j = val;
            if (!(j instanceof JSONArray)) {
                throw new IllegalStateException("type must be a array:\n" + JsonUtil.toString(j));
            }
            result.put(key, (JSONArray) val);
        });
        return result;
    }

    public void vistAttrValMap(BiConsumer<String, JSON> tabValConsumer) {
        this.attrValMap.entrySet().forEach((entry -> {
            tabValConsumer.accept(entry.getKey(), entry.getValue());
        }));
    }


    @Override
    public int size() {
        return this.attrValMap.size();
    }

    public PluginEqualResult isPluginEqual(Describable plugin) {
        // 空值检查
        Descriptor desc = Objects.requireNonNull(plugin, "plugin can not be null").getDescriptor();
        PluginFormProperties propertyTypes = desc.getPluginFormPropertyTypes();

        return Objects.requireNonNull(propertyTypes, "propertyTypes can not be null").accept(new PluginFormProperties.IVisitor() {
            @Override
            public PluginEqualResult visit(RootFormProperties props) {
                try {
                    PropertyType pt = null;
                    String fieldName = null;
                    JSONObject describle = null;
                    for (Map.Entry<String, PropertyType> entry : props.getSortedUseableProperties()) {
                        pt = entry.getValue();
                        fieldName = entry.getKey();
                        Object exist = null;
                        if (pt.isIdentity()) {
                            continue;
                        }
                        exist = pt.getFrontendOutput(plugin);

                        if (pt.isDescribable()) {
                            // 检查 pluginVals 中是否存在该字段
                            describle = getAttrVal(fieldName);
                            if (describle == null) {
                                // throw new IllegalStateException("fieldName:" + fieldName + " relevant describle
                                // can not be null");
                                return notEqual("fieldName:" + fieldName + " relevant describle is null");
                            }

                            Object vals = Objects.requireNonNull(describle.getJSONObject(KEY_DESC_VAL),
                                    "key:" + KEY_DESC_VAL + " " + "relevant json can not be null").get(PLUGIN_EXTENSION_VALS);
                            if (vals == null) {
                                // 如果 exist 也是 null，则认为相等
                                if (exist == null) {
                                    continue;
                                }
                                return notEqual("fieldName:" + fieldName + " vals is null but exist vals is not null");
                            }

                            if (exist == null) {
                                return notEqual("fieldName:" + fieldName + " relevant exist vals is  null");
                            }

                            PluginEqualResult compareResult = null;
                            if (!(compareResult = parseAttrValMap(vals).isPluginEqual((Describable) exist)).equal) {
                                return notEqual("desc field:" + fieldName + "," + compareResult.unEqualLogger).setStack(compareResult.stack);
                            }

                        } else {
                            Object primaryVal = getPrimaryVal(fieldName);
                            if (primaryVal == null && exist == null) {
                                continue;
                            }
                            if (primaryVal == null ^ exist == null) {
                                return notEqual("fieldName:" + fieldName + ",primaryVal(" + primaryVal + ") == null ^"
                                        + " exist(\"" + exist + "\") == null");
                            }
                            if (!StringUtils.equals(String.valueOf(exist), String.valueOf(primaryVal))) {
                                return notEqual("fieldName:" + fieldName + ",exist(" + exist + ") != " + primaryVal);
                            }
                        }
                    }
                    return new PluginEqualResult(true, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Void visit(BaseSubFormProperties props) {
                // 对于子表单属性，暂时不支持比较
                throw new UnsupportedOperationException();
            }
        });
    }
}
