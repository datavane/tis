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
package com.qlangtech.tis.realtime.transfer;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MQ binLog 建模
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月8日 下午2:27:03
 */
public class DTO {

    private Map<String, Object> after = Collections.emptyMap();

    private Map<String, Object> before = Collections.emptyMap();

    private String dbName;

    private String tableName;
    private String physicsTabName;

    private EventType eventType;

    public String getPhysicsTabName() {
        return this.physicsTabName;
    }

    public void setPhysicsTabName(String physicsTabName) {
        this.physicsTabName = physicsTabName;
    }

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

    public DTO setEventType(EventType eventType) {
        this.eventType = eventType;
        return this;
    }

    public DTO clone() {
        DTO d = new DTO();
        d.setBefore(Maps.newHashMap(this.getBefore()));
        d.setAfter(Maps.newHashMap(this.getAfter()));
        d.setTableName(this.tableName);
        d.setEventType(this.eventType);
        d.setDbName(this.dbName);
        return d;
    }


    public enum EventType {

        UPDATE_BEFORE("UPDATE_BEFORE", "update") //
        , UPDATE_AFTER("UPDATE_AFTER", UPDATE_BEFORE.collapse) //
        , ADD("INSERT", "insert") //
        , DELETE("DELETE", "delete");

        private final String type;
        private final String collapse;

        private EventType(String type, String collapse) {
            this.type = type;
            this.collapse = collapse;
        }

        public static EventType parse(String eventType) {
            for (EventType e : EventType.values()) {
                if (e.type.equalsIgnoreCase(eventType)) {
                    return e;
                }
            }
            throw new IllegalStateException("eventType:" + eventType + " is illegal");
        }

        public String getCollapse() {
            return collapse;
        }

        public String getTypeName() {
            return this.type;
        }
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer("{");
        if (after != null) {
            buffer.append("after=" + after.entrySet().stream().map((e) -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(",")));
        }
        buffer.append(", execType='" + String.valueOf(eventType) + '\'');
        buffer.append(", dbName='" + dbName + '\'');
        buffer.append(", tableName='" + tableName + '\'');
        buffer.append('}');
        return buffer.toString();
    }
}
