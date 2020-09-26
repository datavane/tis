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
package com.qlangtech.tis.sql.parser.meta;

import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DependencyNode {

    private String id;

    private String tabid;

    private String dbid;

    private String name;

    private String dbName;

    private String extractSql;

    private String type = NodeType.JOINER_SQL.getType();

    // 节点额外元数据信息
    private TabExtraMeta extraMeta;

    public TabExtraMeta getExtraMeta() {
        return extraMeta;
    }

    public void setExtraMeta(TabExtraMeta extraMeta) {
        this.extraMeta = extraMeta;
    }

    private Position position;

    public Position getPosition() {
        return this.position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getTabid() {
        return this.tabid;
    }

    public void setTabid(String tabid) {
        this.tabid = tabid;
    }

    public String getDbid() {
        return this.dbid;
    }

    public void setDbid(String dbid) {
        this.dbid = dbid;
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getExtraSql() {
        return this.extractSql;
    }

    public void setExtraSql(String extractSql) {
        this.extractSql = extractSql;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public NodeType parseNodeType() {
        NodeType result = NodeType.parse(this.type);
        if (result == NodeType.DUMP) {
            if (StringUtils.isBlank(this.tabid)) {
                throw new IllegalStateException("tabid can not be null,id:" + this.id);
            }
        }
        return result;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityName parseEntityName() {
        if (StringUtils.isEmpty(this.dbName)) {
            return EntityName.parse(this.name);
        } else {
            return EntityName.parse(this.dbName + "." + this.name);
        }
    }

    @Override
    public String toString() {
        return dbName + "." + name;
    }
}
