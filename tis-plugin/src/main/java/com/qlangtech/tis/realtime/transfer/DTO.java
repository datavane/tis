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
package com.qlangtech.tis.realtime.transfer;

import java.util.Map;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * MQ binLog 建模
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月8日 下午2:27:03
 */
public class DTO {

    private Map<String, String> after;

    private Map<String, String> before;

    private String dbName;

    private String orginTableName;

    private EventType eventType;

    public Map<String, String> getAfter() {
        return after;
    }

    public void setAfter(Map<String, String> after) {
        this.after = after;
    }

    public Map<String, String> getBefore() {
        return before;
    }

    public void setBefore(Map<String, String> before) {
        this.before = before;
    }

    public String getTargetTable() {
        return this.getOrginTableName();
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getOrginTableName() {
        return orginTableName;
    }

    public void setOrginTableName(String orginTableName) {
        this.orginTableName = orginTableName;
    }

    @JSONField(serialize = false)
    public EventType getEvent() {
        return this.eventType;
    }

    public String getEventType() {
        return this.eventType.getTypeName();
    }

    public void setEventType(String eventType) {
        this.eventType = EventType.parse(eventType);
    }

    public enum EventType {

        UPDATE("UPDATE"), ADD("INSERT"), DELETE("DELETE");

        private final String type;

        EventType(String type) {
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
}
