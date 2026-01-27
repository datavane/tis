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
package com.qlangtech.tis.plugin.table.join;

import com.qlangtech.tis.plugin.ds.IMultiElement;

import java.io.Serializable;

/**
 * Table JOIN filter condition
 * Used to add constant filters in ON clause, e.g.: A.valid='1' AND B.valid='1'
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/01/23
 */
public class TableJoinFilterCondition implements IMultiElement, Serializable {

    // Table type: primary or dimension
    private TableJoinFilterConditionCreatorFactory.TableType tableType;

    // Column name
    private String columnName;

    // Operator: =, !=, >, <, >=, <=
    private TableJoinFilterConditionCreatorFactory.Operator operator;

    // Value type: string, number, boolean
    private TableJoinFilterConditionCreatorFactory.ValueType valueType;

    // Value
    private String value;

    public TableJoinFilterConditionCreatorFactory.TableType getTableType() {
        return tableType;
    }

    public void setTableType(TableJoinFilterConditionCreatorFactory.TableType tableType) {
        this.tableType = tableType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public TableJoinFilterConditionCreatorFactory.Operator getOperator() {
        return operator;
    }

    public void setOperator(TableJoinFilterConditionCreatorFactory.Operator operator) {
        this.operator = operator;
    }

    public TableJoinFilterConditionCreatorFactory.ValueType getValueType() {
        return valueType;
    }

    public void setValueType(TableJoinFilterConditionCreatorFactory.ValueType valueType) {
        this.valueType = valueType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return "";
    }
}
