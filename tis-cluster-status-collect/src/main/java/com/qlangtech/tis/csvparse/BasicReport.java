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

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class BasicReport {

    private final Date parseDate;

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    protected BasicReport(Date parseDate) {
        super();
        this.parseDate = parseDate;
    }

    public String getFormatDate() {
        return dateFormat.format(parseDate);
    }

    public Date getParseDate() {
        return parseDate;
    }

    public File createReportFileName(Date date) {
        return new File(dateFormat.format(date) + "core_report.xls");
    }
    // public static final void processExcel(final InputStream tempReader,
    // WokbookProcess wokbookProcess) throws Exception {
    // POIFSFileSystem reader = null;
    // try {
    // //	reader = new POIFSFileSystem();
    // XSSFWorkbook workbook = new XSSFWorkbook(tempReader);
    // wokbookProcess.start(workbook);
    // 
    // } finally {
    // try {
    // tempReader.close();
    // } catch (Throwable e) {
    // 
    // }
    // }
    // }
}
