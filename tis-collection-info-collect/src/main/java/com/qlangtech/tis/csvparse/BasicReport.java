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

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
