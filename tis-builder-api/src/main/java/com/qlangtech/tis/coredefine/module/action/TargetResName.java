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

package com.qlangtech.tis.coredefine.module.action;

import org.apache.commons.lang.StringUtils;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-10-15 15:59
 **/
public class TargetResName {
    private final String name;

    public TargetResName(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("param name can not be empty");
        }
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getK8SResName() {
        return StringUtils.replace(name, "_", "-");
    }
}
