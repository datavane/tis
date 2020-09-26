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
package com.qlangtech.tis.solrdao.extend;

import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2018年10月8日
 */
public class IndexBuildHook extends BaseExtendConfig {

    private static final String CLASS = "class";

    private final String fullClassName;

    private IndexBuildHook(String args) {
        super(args);
        fullClassName = this.params.get(CLASS);
        if (StringUtils.isEmpty(fullClassName)) {
            throw new IllegalArgumentException();
        }
    }

    public static IndexBuildHook create(String args) {
        IndexBuildHook indexBuildHook = new IndexBuildHook(args);
        return indexBuildHook;
    }

    public String getFullClassName() {
        return this.fullClassName;
    }
}
