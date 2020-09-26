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
package com.qlangtech.tis.fullbuild.taskflow;

import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年11月30日
 */
public abstract class BasicTask implements ITask {

    protected String name;

    // 节点成功执行，之后执行的节点
    protected String successTo;

    // 依赖的dump表
    private List<EntityName> dependencyTables;

    public final String getName() {
        return this.name;
    }

    public void setDependencyTables(List<EntityName> dependencyTables) {
        this.dependencyTables = dependencyTables;
    }

    /**
     * 取得依赖的表
     *
     * @return
     */
    public List<EntityName> getDependencyTables() {
        return this.dependencyTables;
    }

    public void setName(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("parm name can not be null");
        }
        // StringUtils.defaultIfBlank(name, String.valueOf(UUID.randomUUID()));
        this.name = name;
    }

    public String getSuccessTo() {
        return successTo;
    }

    public void setSuccessTo(String successTo) {
        if (StringUtils.isBlank(successTo)) {
            throw new IllegalArgumentException("param successTo can not be null");
        }
        this.successTo = successTo;
    }
}
