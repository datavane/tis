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

import java.util.Formatter;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ToHtmlPain {

    // private StringBuffer out = new StringBuffer();
    // = new Formatter(output);
    private final Formatter out;

    public ToHtmlPain(StringBuffer buffer) {
        super();
        this.out = new Formatter(buffer);
    }

    // private void writeDateHeader(final HistoryAvarageResult historyRecord,
    // XSSFSheet sheet) {
    // int colIndex = 3;
    // XSSFRow row;
    // row = sheet.createRow(2);
    // ExcelRow erow = new ExcelRow(row, "");
    // // 写日期title
    // for (int i = 0; i < 2; i++) {
    // for (Date date : historyRecord.dates) {
    // erow.setString(colIndex++, datef.format(date));
    // }
    // }
    // }
    public void printHeader() {
        out.format("<thead>                                                             ");
        out.format(" <tr>                                                               ");
        out.format("    <th class=\"style_03\" rowspan=\"2\">业务线</th>                ");
        out.format("    <th class=\"style_09\" rowspan=\"2\">部门</th>                  ");
        out.format("    <th class=\"style_09\" rowspan=\"2\">索引名称</th>              ");
        out.format("    <th class=\"style_07\" colspan=\"7\">QP</th>                    ");
        out.format("                                                                    ");
        out.format("    <th class=\"style_0b\" colspan=\"7\">索引条目</th>              ");
        out.format("                                                                    ");
        out.format("    <th class=\"style_05\" rowspan=\"2\">机器数</th>                ");
        out.format("</tr>                                                               ");
        out.format("<tr>                                                                ");
        out.format("                                                                    ");
        // region 1
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">04/26</th>   ");
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">04/27</th>   ");
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">04/28</th>   ");
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">04/29</th>   ");
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">04/30</th>   ");
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">05/01</th>   ");
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">05/02</th>   ");
        // region 2
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">04/26</th>   ");
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">04/27</th>   ");
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">04/28</th>   ");
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">04/29</th>   ");
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">04/30</th>   ");
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">05/01</th>   ");
        out.format("    <th class=\"style_00\" style=\"text-align: left;\">05/02</th>   ");
        out.format("</tr>                                                               ");
        out.format("</thead>                                                            ");
    }
}
