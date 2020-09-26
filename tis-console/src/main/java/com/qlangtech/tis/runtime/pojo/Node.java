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
package com.qlangtech.tis.runtime.pojo;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-5-9
 */
public class Node {

    private final String name;

    private final Integer pId;

    private final Integer id;

    public Node(String name, Integer pId, Integer id) {
        super();
        this.name = name;
        this.pId = pId;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Integer getpId() {
        return pId;
    }

    public Integer getId() {
        return id;
    }
}
