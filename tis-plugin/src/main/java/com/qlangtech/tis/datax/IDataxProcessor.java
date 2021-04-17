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

import java.util.List;

/**
 * Datax 执行器可以在各种容器上执行 https://github.com/alibaba/DataX
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 14:38
 */
public interface IDataxProcessor {

    IDataxReader getReader();

    IDataxWriter getWriter();


    /**
     * 类似MySQL(A库)导入MySQL(B库) A库中的一张a表可能对应的B库的表为aa表名称会不一致，
     */
    public class TableMap {
        private String from;
        private List<String> sourceCols;
        private String to;

        public List<String> getSourceCols() {
            return sourceCols;
        }

        public void setSourceCols(List<String> sourceCols) {
            this.sourceCols = sourceCols;
        }

        public String getFrom() {
            return this.from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return this.to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        @Override
        public String toString() {
            return "TableMap{" +
                    "from='" + from + '\'' +
                    ", to='" + to + '\'' +
                    '}';
        }
    }
}
