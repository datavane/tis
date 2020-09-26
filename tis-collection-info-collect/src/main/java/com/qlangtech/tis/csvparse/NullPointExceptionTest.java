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
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class NullPointExceptionTest {

    private void executeFile(File file, String type, long date) {
    // ......
    }

    private Date getExecuteDate() {
        return null;
    }

    private File getNowHasNotBeenExecuteFile() {
        return new File("D:\\j2ee_solution\\eclipse-SDK-3.5.2-taobao20111209\\workspace\\terminator-manage\\src\\main\\webapp\\WEB-INF\\lib\\commons-codec-1.5.jar");
    }

    public static void main(String[] args) {
        NullPointExceptionTest exceptionTest = new NullPointExceptionTest();
        exceptionTest.executeFile(exceptionTest.getNowHasNotBeenExecuteFile(), "new", exceptionTest.getExecuteDate().getTime());
    }
}
