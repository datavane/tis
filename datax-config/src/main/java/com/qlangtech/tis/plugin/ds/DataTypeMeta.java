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

package com.qlangtech.tis.plugin.ds;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-08-10 13:36
 **/
public class DataTypeMeta {

    public static final int DEFAULT_DECIMAL_PRECISION = 38;

    private final DataType _type;

    /**
     * 正规化decimal的精度
     *
     * @param type
     * @return
     */
    public static int[] normalizeDecimalPrecisionAndScale(DataType type) {
        int precision = type.getColumnSize();
        Integer scale = type.getDecimalDigits();
        if (precision < 1 || precision > DataTypeMeta.DEFAULT_DECIMAL_PRECISION) {
            precision = DataTypeMeta.DEFAULT_DECIMAL_PRECISION;
        }
        return new int[]{precision, scale};
    }

    public interface IMultiItemsView {
        public static IMultiItemsView unknow() {
            return new IMultiItemsView() {
                @Override
                public List<String> getElementPropertyKeys() {
                    return Collections.emptyList();
                }

                @Override
                public ViewContent getViewContent() {
                    return ViewContent.Unknow;
                }
            };
        }

        public List<String> getElementPropertyKeys();

        public ViewContent getViewContent();

        /**
         * 向客户端传输的json内容中额外添加自动移属性
         *
         * @param biz
         */
        default void appendExternalJsonProp(JSONObject biz) {

        }
    }

    /**
     * 接受的两种类型:
     * <ol>
     *     <li>IDataxProcessor.TableMap</li>
     *     <li>List<CMeta></li>
     * </ol>
     *
     * @param tabMapper
     * @return
     */
    public static Map<String, Object> createViewBiz(IMultiItemsView multiItemsView, Object tabMapper) {
        List<String> elementGetKeys = Objects.requireNonNull(multiItemsView).getElementPropertyKeys();
        JSONObject biz = new JSONObject();
        biz.put("viewContentType", multiItemsView.getViewContent().getToken());
        // Map<String, Object> biz = new HashMap<>();
        if (elementGetKeys != null && !elementGetKeys.isEmpty()) {
            biz.put("elementKeys", elementGetKeys);
        }
        biz.put("tabMapper", Objects.requireNonNull(tabMapper, "tabMapper can not be null"));

        JSONArray types = new JSONArray();
        JSONObject type = null;
        JSONObject dt = null;
        DataType t = null;
        DataTypeMeta meta = null;
        JDBCTypes jdbcType = null;

        for (Map.Entry<JDBCTypes, DataTypeMeta> entry
                : DataTypeMeta.getTypeMetasDic().entrySet()) {
            meta = entry.getValue();
            jdbcType = entry.getKey();
            type = new JSONObject();
            type.put("containColSize", meta.isContainColSize());
            type.put("containDecimalRange", meta.isContainDecimalRange());
            dt = new JSONObject();

            t = meta.getType();

            dt.put("columnSize", t.getColumnSize());
            dt.put("decimalDigits", t.getDecimalDigits());
            dt.put("type", jdbcType.type);
            dt.put("typeDesc", t.getTypeDesc());
            // dt.put("typeName", t.typeName);
            dt.put("typeName", jdbcType.getLiteria());

            ColSizeRange colsSizeRange = meta.getColsSizeRange();
            if (colsSizeRange != null) {
                //                "colsSizeRange": {
                //                    "max": 46,
                //                            "min": 1
                //                },
                JSONObject range = new JSONObject();
                range.put("max", colsSizeRange.getMax());
                range.put("min", colsSizeRange.getMin());
                type.put("colsSizeRange", range);
            }

            DecimalRange decimalRange = meta.getDecimalRange();
            if (decimalRange != null) {
                JSONObject range = new JSONObject();
                //                "decimalRange": {
                //                    "max": 20,
                //                            "min": 1
                //                },
                range.put("max", decimalRange.getMax());
                range.put("min", decimalRange.getMin());
                type.put("decimalRange", range);
            }

            type.put("type", dt);
            types.add(type);
        }

        biz.put("colMetas", types);
        multiItemsView.appendExternalJsonProp(biz);
        return biz;
    }

    private final Optional<ColSizeRange> colSizeRange;
    private final Optional<DecimalRange> decimalRange;


    //            case :
    //            case :
    //

    //            case Types.:
    //            case Types.:
    //            case Types.REAL:
    //            case Types.:
    //
    //            case Types.DATE:
    //            case Types.TIME:
    //            case Types.:
    //            case Types.BIT:
    //            case Types.BOOLEAN:
    //            case Types.BLOB:
    //            case Types.BINARY:
    //            case Types.LONGVARBINARY:
    //            case Types.VARBINARY:

    private static List<DataTypeMeta> createTypes(DataTypeMeta... metas) {
        List<DataTypeMeta> result = new ArrayList<>();
        for (DataTypeMeta meta : metas) {
            result.add(meta);
        }
        return Collections.unmodifiableList(result);
    }

    public static final List<DataTypeMeta> typeMetas //
            =  //
            createTypes(new DataTypeMeta(DataType.createVarChar(32), new ColSizeRange(1, 2000)),
                    new DataTypeMeta(new DataType(JDBCTypes.INTEGER)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.TINYINT)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.SMALLINT)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.BIGINT)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.CHAR)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.FLOAT)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.DOUBLE)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.DECIMAL, 20) //
                            .setDecimalDigits(0), new ColSizeRange(1, 46), new DecimalRange(1, 20)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.DATE)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.TIME)), //
                    new DataTypeMeta(new DataType(JDBCTypes.TIMESTAMP)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.BIT)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.LONGVARCHAR, Integer.MAX_VALUE)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.LONGNVARCHAR, Integer.MAX_VALUE)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.BOOLEAN)) //
                    //    , new DataTypeMeta(new DataType(Types.T, "BOOLEAN")) //
                    , new DataTypeMeta(new DataType(JDBCTypes.BLOB, 1000), new ColSizeRange(1, 2000))//
                    , new DataTypeMeta(new DataType(JDBCTypes.BINARY, 1000), new ColSizeRange(1, 2000)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.LONGVARBINARY, 1000), new ColSizeRange(1, 2000)) //
                    , new DataTypeMeta(new DataType(JDBCTypes.VARBINARY, 1000), new ColSizeRange(1, 4000)));

    private static final Map<JDBCTypes, DataTypeMeta> typeMetasDic;

    static {
        Map<JDBCTypes, DataTypeMeta> dic = new HashMap<>();
        for (DataTypeMeta type : typeMetas) {
            dic.put(type._type.getJdbcType(), type);
        }
        dic.put(JDBCTypes.OTHER, dic.get(JDBCTypes.VARCHAR));
        dic.put(JDBCTypes.REAL, dic.get(JDBCTypes.FLOAT));
        dic.put(JDBCTypes.NUMERIC, dic.get(JDBCTypes.DECIMAL));
        typeMetasDic = Collections.unmodifiableMap(dic);
    }

    public static Map<JDBCTypes, DataTypeMeta> getTypeMetasDic() {
        return typeMetasDic;
    }

    /**
     * @param type
     * @return
     * @see java.sql.Types
     */
    public static DataTypeMeta getDataTypeMeta(JDBCTypes type) {
        return Objects.requireNonNull(typeMetasDic.get(type),
                "type:" + type + " relevant " + DataTypeMeta.class.getSimpleName() + " can not be null");
    }

    public DataTypeMeta(DataType type) {
        this(type, Optional.empty(), Optional.empty());
    }

    public DataTypeMeta(DataType type, ColSizeRange colSizeRange) {
        this(type, Optional.of(colSizeRange), Optional.empty());
    }

    public DataTypeMeta(DataType type, ColSizeRange colSizeRange, DecimalRange decimalRange) {
        this(type, Optional.of(colSizeRange), Optional.of(decimalRange));
    }

    private DataTypeMeta(DataType type, Optional<ColSizeRange> colSizeRange, Optional<DecimalRange> decimalRange) {
        this._type = Objects.requireNonNull(type, "dataType can not be null");
        this.colSizeRange = colSizeRange;
        this.decimalRange = decimalRange;
    }

    public DataType getType() {
        return Objects.requireNonNull(this._type, "type can not be null");
    }

    public boolean isContainColSize() {
        return this.colSizeRange.isPresent();
    }

    public ColSizeRange getColsSizeRange() {
        return this.colSizeRange.orElse(null);
    }

    public boolean isContainDecimalRange() {
        return this.decimalRange.isPresent();
    }

    public DecimalRange getDecimalRange() {
        return this.decimalRange.orElse(null);
    }
}
