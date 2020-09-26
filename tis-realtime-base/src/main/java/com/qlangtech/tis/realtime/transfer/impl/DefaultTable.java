/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.realtime.transfer.impl;

import com.qlangtech.tis.realtime.transfer.DTO;
import com.qlangtech.tis.realtime.transfer.ITable;
import com.qlangtech.tis.wangjubao.jingwei.Table;
import org.apache.commons.lang.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月5日 下午8:00:51
 */
public class DefaultTable extends AbstractRowValueGetter implements ITable {

    private final String tableName;

    private DTO.EventType eventType;

    private DTO rowDto;

    public DefaultTable(String tableName, Table tableProcessor) {
        super(tableProcessor);
        this.tableName = (tableName);
    // this.versionCreator = versionCreator;
    }

    /**
     * 为groovy使用
     * @param name
     * @return
     */
    public Object getProperty(String name) {
        return this.getColumn(name);
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
        if (this.eventType == DTO.EventType.ADD) {
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
        if (this.eventType == DTO.EventType.UPDATE) {
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
    // public long getVersion() {
    // return getVersion(this, versionCreator);
    // }
    // private static long getVersion(IRowValueGetter rvalGetter, RowVersionCreator[] versionCreator) {
    // String version = null;
    // inner:
    // for (RowVersionCreator versionCol : versionCreator) {
    // version = rvalGetter.getColumn(versionCol.getVersionColumnName());
    // if (version == null) {
    // continue inner;
    // }
    // 
    // return versionCol.getVersion(version);
    // }
    // return 0l;
    // }
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

    public DTO.EventType getEventType() {
        return eventType;
    }

    public void setEventType(DTO.EventType eventType) {
        this.eventType = eventType;
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
        this.columns.put(converFieldtName(name), processRawVal(name, value));
    }

    protected String converFieldtName(String name) {
        return name;
    }

    public void addBeforeColumn(String name, String value) {
        this.beforeColumns.put(converFieldtName(name), processRawVal(name, value));
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
        StringBuffer result = new StringBuffer();
        // new StringBuffer("rowcontent:").append(this.rowDto.getRowContent()).append("\n");
        // return "rowcontent:" + this.rowDto.getRowContent() +
        // JSON.toJSONString(this, false);
        desc(result);
        return result.toString();
    }
}
