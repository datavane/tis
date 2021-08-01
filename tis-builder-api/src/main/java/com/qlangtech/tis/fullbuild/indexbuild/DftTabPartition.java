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
package com.qlangtech.tis.fullbuild.indexbuild;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 10:40
 */
public class DftTabPartition implements ITabPartition {
    private final String pt;

    public DftTabPartition(String pt) {
        this.pt = pt;
    }

    @Override
    public String getPt() {
        return this.pt;
    }

    @Override
    public int hashCode() {
        return this.getPt().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return hashCode() == obj.hashCode();
    }

    @Override
    public String toString() {
        return this.getPt();
    }
}
