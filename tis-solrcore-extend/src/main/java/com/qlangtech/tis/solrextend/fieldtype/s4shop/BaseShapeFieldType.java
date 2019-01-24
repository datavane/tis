/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

import java.util.Collections;
import java.util.List;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.SpatialRecursivePrefixTreeFieldType;
import org.locationtech.spatial4j.shape.Shape;

/* *
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
