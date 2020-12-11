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
package com.qlangtech.tis.compiler.java;

import com.qlangtech.tis.plugin.ds.DBConfig;
import junit.framework.TestCase;
import java.io.File;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestJavaCompilerProcess extends TestCase {

    public void testCreateJar() throws Exception {
        File rootDir = new File("/opt/data/dao/shop/20191025105833/src/main");
        File classpathDir = new File("/Users/mozhenghua/Desktop/j2ee_solution/project/tis-ibatis/target/dependency");
        DBConfig dbConfig = new DBConfig();
        dbConfig.setName("shop");
        JavaCompilerProcess compilerProcess = new JavaCompilerProcess(dbConfig, rootDir, classpathDir);
        compilerProcess.compileAndBuildJar();
    }
}
