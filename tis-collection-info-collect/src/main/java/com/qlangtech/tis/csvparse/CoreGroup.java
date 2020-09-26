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
package com.qlangtech.tis.csvparse;

import java.util.HashSet;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class CoreGroup {

    private final int index;

    private final String name;

    public CoreGroup(int index, String name) {
        super();
        this.index = index;
        this.name = name;
    }

    private long indexSize;

    private long indexNum;

    private long queryCount;

    private float queryConsumeTime;

    public long getIndexSize() {
        return indexSize;
    }

    public void setIndexSize(long indexSize) {
        this.indexSize = indexSize;
    }

    public long getIndexNum() {
        return indexNum;
    }

    public void setIndexNum(long indexNum) {
        this.indexNum = indexNum;
    }

    public long getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(long queryCount) {
        this.queryCount = queryCount;
    }

    public float getQueryConsumeTime() {
        return queryConsumeTime;
    }

    public void setQueryConsumeTime(float queryConsumeTime) {
        this.queryConsumeTime = queryConsumeTime;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + index;
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    public static void main(String[] arg) {
        CoreGroup group = new CoreGroup(0, "abc");
        Set<CoreGroup> groupSet = new HashSet<CoreGroup>();
        groupSet.add(group);
        group = new CoreGroup(0, "abc");
        groupSet.add(group);
        group = new CoreGroup(0, "abcc");
        groupSet.add(group);
        System.out.println(groupSet.size());
    }
}
