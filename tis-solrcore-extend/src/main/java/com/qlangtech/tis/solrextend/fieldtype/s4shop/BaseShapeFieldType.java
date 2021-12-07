/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

import java.util.Collections;
import java.util.List;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.SpatialRecursivePrefixTreeFieldType;
import org.locationtech.spatial4j.shape.Shape;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BaseShapeFieldType extends SpatialRecursivePrefixTreeFieldType {

    public BaseShapeFieldType() {
        super();
    }

    private static final ThreadLocal<ShapeStrStore> shapeStrTL = new ThreadLocal<ShapeStrStore>() {

        @Override
        protected ShapeStrStore initialValue() {
            return new ShapeStrStore();
        }
    };

    private static class ShapeStrStore {

        private String shapeStr;
    }

    protected final String getStoredValue(Shape shape, String shapeStr) {
        return shapeStrTL.get().shapeStr;
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object val) {
        // 客户端 传入的参数是这样的
        if (isShapeLiteria(val)) {
            shapeStrTL.get().shapeStr = String.valueOf(val);
            return super.createFields(field, val);
        }
        StringBuffer buffer = buildShapLiteria(val);
        if (buffer == null) {
            // logger.warn("field:"+ field.getName() +",val:" + val + " is illegal");
            return Collections.emptyList();
        }
        shapeStrTL.get().shapeStr = buffer.toString();
        return super.createFields(field, parseShape(buffer.toString()));
    }

    protected abstract StringBuffer buildShapLiteria(Object val);

    protected abstract boolean isShapeLiteria(Object val);
}
