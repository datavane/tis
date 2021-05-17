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
package com.qlangtech.tis.datax;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 选中需要导入的表
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-18 10:16
 */
public interface ISelectedTab {
    String getName();

    String getWhere();

    boolean isAllCols();

    List<ColMeta> getCols();

    public class ColMeta {
        private String name;
        private DataXReaderColType type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public DataXReaderColType getType() {
            return type;
        }

        public void setType(DataXReaderColType type) {
            this.type = type;
        }
    }

    public enum DataXReaderColType {
        Long("long"),
        Double("double"),
        STRING("string"),
        Boolean("boolean"),
        Date("date");

        private final String literia;

        private DataXReaderColType(String literia) {
            this.literia = literia;
        }

        public static DataXReaderColType parse(String literia) {
            literia = StringUtils.lowerCase(literia);
            for (DataXReaderColType t : DataXReaderColType.values()) {
                if (literia.equals(t.literia)) {
                    return t;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.literia;
        }

        public static String toDesc() {
            return Arrays.stream(DataXReaderColType.values()).map((t) -> "'" + t.literia + "'").collect(Collectors.joining(","));
        }
    }
}
