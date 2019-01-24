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
package com.qlangtech.tis.realtime.transfer.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.realtime.transfer.BasicONSListener.RowVersionCreator;
import com.qlangtech.tis.realtime.transfer.DTO;
import com.qlangtech.tis.realtime.transfer.ITable;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DefaultTable implements ITable {

    private final String tableName;

    private EventType eventType;

    private DTO rowDto;

    private final RowVersionCreator[] versionCreator;

    public enum EventType {

        UPDATE("UPDATE"), ADD("INSERT"), DELETE("DELETE");

        private final String type;

        private EventType(String type) {
            this.type = type;
        }

        public static EventType parse(String eventType) {
            if (UPDATE.type.equalsIgnoreCase(eventType)) {
                return UPDATE;
            } else if (ADD.type.equalsIgnoreCase(eventType)) {
                return ADD;
            } else if (DELETE.type.equalsIgnoreCase(eventType)) {
                return DELETE;
            }
            throw new IllegalStateException("eventType:" + eventType + " is illegal");
        }
    }

    public DTO getRowDto() {
        return rowDto;
    }

    public void setRowDto(DTO rowDto) {
        this.rowDto = rowDto;
    }

    @Override
    public boolean columnChange(Set<String> keys) {
        if (keys == null) {
            throw new IllegalStateException("keys can not be null");
        }
        if (this.eventType == EventType.ADD) {
            return true;
        }
        validateTable();
        for (String key : keys) {
            if (!strEquals(beforeColumns.get((key)), columns.get((key)))) {
                return true;
            }
        }
        return false;
    }

    /**
     */
    public void validateTable() {
        if (this.eventType == EventType.UPDATE) {
            if (this.beforeColumns.isEmpty()) {
                throw new IllegalStateException("update table is not valid,shalll have before value:" + this.toString());
            }
        }
    }

    // private static final long VERSION_0 = 0;
    /**
     * 数据更新版本，一般以时间戳标记，防止在row更新過程中被脏数据覆盖
     *
     * @return
     */
    public long getVersion() {
        String version = null;
        inner: for (RowVersionCreator versionCol : versionCreator) {
            version = this.getColumn(versionCol.getVersionColumnName());
            if (version == null) {
                continue inner;
            }
            return versionCol.getVersion(version);
        }
        return 0l;
    }

    /**
     * 字符串是否相
     *
     * @param value1
     * @param value2
     * @return
     */
    private boolean strEquals(Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        return StringUtils.equals(String.valueOf(value1), String.valueOf(value2));
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public DefaultTable(String tableName, RowVersionCreator[] versionCreator) {
        super();
        this.tableName = (tableName);
        this.versionCreator = versionCreator;
    }

    protected final Map<String, String> columns = new HashMap<String, String>();

    protected final Map<String, String> beforeColumns = new HashMap<String, String>();

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public Map<String, String> getColumns() {
        return columns;
    }

    public void addColumn(String name, String value) {
        this.columns.put(converFieldtName(name), value);
    }

    protected String converFieldtName(String name) {
        return name;
    }

    public void addBeforeColumn(String name, String value) {
        this.beforeColumns.put(converFieldtName(name), value);
    }

    @Override
    public String getColumn(String key) {
        return this.columns.get(key);
    }

    @Override
    public int getInt(String key, boolean errorCare) {
        String val = getColumn(key);
        try {
            return Integer.parseInt(val);
        } catch (Throwable e) {
            if (errorCare) {
                throw new RuntimeException(e);
            } else {
                return -1;
            }
        }
    }

    @Override
    public double getDouble(String key, boolean errCare) {
        String val = getColumn(key);
        try {
            return Double.parseDouble(val);
        } catch (Throwable e) {
            if (errCare) {
                throw new RuntimeException(e);
            } else {
                return -1;
            }
        }
    }

    @Override
    public float getFloat(String key, boolean errCare) {
        String val = getColumn(key);
        try {
            return Float.parseFloat(val);
        } catch (Throwable e) {
            if (errCare) {
                throw new RuntimeException(e);
            } else {
                return -1f;
            }
        }
    }

    @Override
    public long getLong(String key, boolean errorCare) {
        String val = getColumn(key);
        try {
            return Long.parseLong(val);
        } catch (Throwable e) {
            if (errorCare) {
                throw new RuntimeException(e);
            } else {
                return -1;
            }
        }
    }

    @Override
    public String getOldColumn(String key) {
        return this.beforeColumns.get(key);
    }

    @Override
    public void desc(StringBuffer buffer) {
        for (Map.Entry<String, String> cols : this.getColumns().entrySet()) {
            buffer.append(cols.getKey()).append(":").append(cols.getValue()).append(",");
        }
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("rowcontent:").append(this.rowDto.getRowContent()).append("\n");
        // return "rowcontent:" + this.rowDto.getRowContent() +
        // JSON.toJSONString(this, false);
        desc(result);
        return result.toString();
    }
}
