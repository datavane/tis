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
package com.qlangtech.tis;

import com.qlangtech.tis.extension.impl.TestXmlFile;
import com.qlangtech.tis.extension.util.TestGroovyShellEvaluate;
import com.qlangtech.tis.extension.util.TestPluginExtraProps;
import com.qlangtech.tis.plugin.TestComponentMeta;
import com.qlangtech.tis.plugin.TestPluginStore;
import com.qlangtech.tis.plugin.annotation.TestValidator;
import com.qlangtech.tis.plugin.ds.TestDataSourceFactoryPluginStore;
import com.qlangtech.tis.util.TestUploadPluginMeta;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestAll extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestUploadPluginMeta.class);
        suite.addTestSuite(TestValidator.class);
        suite.addTestSuite(TestTIS.class);
        suite.addTestSuite(TestComponentMeta.class);
        suite.addTestSuite(TestXmlFile.class);
        suite.addTestSuite(TestPluginStore.class);
        suite.addTestSuite(TestGroovyShellEvaluate.class);
        suite.addTestSuite(TestPluginExtraProps.class);
        suite.addTestSuite(TestDataSourceFactoryPluginStore.class);

        return suite;
    }
}
