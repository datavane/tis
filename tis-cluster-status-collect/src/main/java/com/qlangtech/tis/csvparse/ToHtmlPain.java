/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.csvparse;

import java.util.Formatter;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
