/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.realtime.transfer;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * MQ binLog 建模
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月8日 下午2:27:03
 */
public class DTO {

    private Map<String, Object> after;

    private Map<String, Object> before;

    private String dbName;

    private String tableName;

    private EventType eventType;

    public Map<String, Object> getAfter() {
        return after;
    }

    public void setAfter(Map<String, Object> after) {
        this.after = after;
    }

    public Map<String, Object> getBefore() {
        return before;
    }

    public void setBefore(Map<String, Object> before) {
        this.before = before;
    }

    public String getTargetTable() {
        return this.getTableName();
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @JSONField(serialize = false)
    public EventType getEvent() {
        return this.eventType;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }


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

        public String getTypeName() {
            return this.type;
        }
    }

    @Override
    public String toString() {
        return "DTO{" +
                "after=" + after.entrySet().stream().map((e) -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(",")) +
                ", dbName='" + dbName + '\'' +
                ", tableName='" + tableName + '\'' +
                '}';
    }
}
