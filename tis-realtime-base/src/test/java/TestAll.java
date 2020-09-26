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
/* * Copyright 2020 QingLang, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.qlangtech.tis.realtime.s4totalpay.AbstractTestS4totalpayIncr;
import com.qlangtech.tis.realtime.s4totalpay.TestS4totalpayIncr;
import com.qlangtech.tis.realtime.transfer.ruledriven.TestAllThreadLocal;
import com.qlangtech.tis.realtime.transfer.ruledriven.TestFunctionUtils;
import com.qlangtech.tis.wangjubao.jingwei.TestAliasList;
import com.qlangtech.tis.wangjubao.jingwei.TestTableClusterParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestAll extends TestCase {

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        // suite.addTestSuite(TestGroupKey.class);
        suite.addTestSuite(TestTableClusterParser.class);
        suite.addTestSuite(TestAllThreadLocal.class);
        suite.addTestSuite(TestFunctionUtils.class);
        suite.addTestSuite(TestAliasList.class);
        suite.addTestSuite(TestTableClusterParser.class);
        suite.addTestSuite(TestS4totalpayIncr.class);
        return suite;
    }
}
