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
import com.qlangtech.tis.build.yarn.TestIndexBuildNodeMaster;
import com.qlangtech.tis.build.yarn.TestTableDumpNodeMaster;
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
        suite.addTestSuite(TestTableDumpNodeMaster.class);
        suite.addTestSuite(TestIndexBuildNodeMaster.class);
        return suite;
    }

    public static void main(String[] args) throws Exception {
    // List<Object> list = new ArrayList<>();
    // List<String> strList = new ArrayList<>();
    // strList.add("ddd");
    // list = strList.stream().map((r) -> r).collect(Collectors.toList());
    }
}
