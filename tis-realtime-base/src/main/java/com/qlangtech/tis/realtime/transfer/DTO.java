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
package com.qlangtech.tis.realtime.transfer;

import java.util.Map;
import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.realtime.transfer.impl.DefaultTable;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DTO {

    private Map<String, String> after;

    private Map<String, String> before;

    // private String targetTable;
    private String dbName;

    private String orginTableName;

    private DefaultTable.EventType eventType;

    private com.alibaba.fastjson.JSONObject rowContent;

    @JSONField(serialize = false)
    public com.alibaba.fastjson.JSONObject getRowContent() {
        return rowContent;
    }

    @JSONField(deserialize = false)
    public void setRowContent(com.alibaba.fastjson.JSONObject rowContent) {
        this.rowContent = rowContent;
    }

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

    // public void setTargetTable(String targetTable) {
    // this.targetTable = targetTable;
    // }
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

    public DefaultTable.EventType getEventType() {
        return this.eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = DefaultTable.EventType.parse(eventType);
    }
}
