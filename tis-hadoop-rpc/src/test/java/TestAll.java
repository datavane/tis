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
import com.qlangtech.tis.rpc.grpc.log.stream.TestPhaseStatusCollection;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-12 11:50
 */
public class TestAll {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestPhaseStatusCollection.class);
        return suite;
    }
}
