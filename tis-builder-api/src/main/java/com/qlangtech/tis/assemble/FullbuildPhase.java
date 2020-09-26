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
package com.qlangtech.tis.assemble;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 索引构建的三个阶段
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月17日
 */
public enum FullbuildPhase {

    FullDump(1, "dump", "数据导出"), JOIN(2, "join", "宽表构建"), BUILD(3, "indexBuild", "索引构建"), IndexBackFlow(4, "indexBackflow", "索引回流");

    public static String desc(Collection<FullbuildPhase> phaseSet) {
        return "[" + phaseSet.stream().map(r -> r.getName()).collect(Collectors.joining(",")) + "]";
    }

    public static FullbuildPhase getFirst(Collection<FullbuildPhase> phaseSet) {
        Optional<FullbuildPhase> min = phaseSet.stream().min((r1, r2) -> r1.value - r2.value);
        if (!min.isPresent()) {
            throw new IllegalStateException("phaseSet size shall not be 0");
        }
        return min.get();
    }

    public boolean bigThan(FullbuildPhase phase) {
        return this.value > phase.value;
    }

    // Dump_AND_JOIN(1, "join", "导出+宽表") // JOIN(2, "join", "宽表构建"),
    // , BUILD(2, "indexBuild", "索引构建") //
    // , IndexBackFlow(3, "indexBackflow", "索引回流");
    private final int value;

    private final String name;

    private final String literal;

    private FullbuildPhase(int value, String name, String literal) {
        this.value = value;
        this.name = name;
        this.literal = literal;
    }

    public String getLiteral() {
        return this.literal;
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return this.value;
    }

    public static FullbuildPhase parse(String value) {
        if (FullDump.name.equals(value)) {
            return FullDump;
        } else if (JOIN.name.equals(value)) {
            return JOIN;
        } else if (BUILD.name.equals(value)) {
            return BUILD;
        } else if (IndexBackFlow.name.equals(value)) {
            return IndexBackFlow;
        } else {
            throw new IllegalStateException("value " + value + " is not illegal");
        }
    }

    public static FullbuildPhase parse(int value) {
        if (value == FullDump.value) {
            return FullDump;
        } else if (value == JOIN.value) {
            return JOIN;
        } else if (value == BUILD.value) {
            return BUILD;
        } else if (IndexBackFlow.value == value) {
            return IndexBackFlow;
        } else {
            throw new IllegalStateException("value " + value + " is not illegal");
        }
    }
}
