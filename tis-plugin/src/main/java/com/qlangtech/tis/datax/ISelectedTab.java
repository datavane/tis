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

import java.sql.Types;
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
        private boolean pk = false;

        /**
         * 是否是主键，有时下游writer表例如clickhouse如果选择自动建表脚本，则需要知道表中的主键信息
         *
         * @return
         */
        public boolean isPk() {
            return this.pk;
        }

        public void setPk(boolean pk) {
            this.pk = pk;
        }

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

    // https://github.com/alibaba/DataX/blob/master/mysqlreader/doc/mysqlreader.md#33-%E7%B1%BB%E5%9E%8B%E8%BD%AC%E6%8D%A2
    public enum DataXReaderColType {
        Long("long"),
        INT("int"),
        Double("double"),
        STRING("string"),
        Boolean("boolean"),
        Date("date"),
        Bytes("bytes");

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

        public String getLiteria() {
            return literia;
        }

        /**
         * https://github.com/alibaba/DataX/blob/master/mysqlreader/doc/mysqlreader.md#33-%E7%B1%BB%E5%9E%8B%E8%BD%AC%E6%8D%A2
         *
         * @param mysqlType java.sql.Types
         * @return
         */
        public static DataXReaderColType parse(int mysqlType) {
            switch (mysqlType) {
                case Types.INTEGER:
                case Types.TINYINT:
                case Types.SMALLINT:
                case Types.BIGINT:
                    return DataXReaderColType.Long;
                case Types.FLOAT:
                case Types.DOUBLE:
                case Types.DECIMAL:
                    return DataXReaderColType.Double;
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                    return DataXReaderColType.Date;
                case Types.BIT:
                case Types.BOOLEAN:
                    return DataXReaderColType.Boolean;
                case Types.BLOB:
                case Types.BINARY:
                case Types.LONGVARBINARY:
                case Types.VARBINARY:
                    return DataXReaderColType.Bytes;
                default:
                    return DataXReaderColType.STRING;
            }
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
