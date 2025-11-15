/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin.ds;

import com.qlangtech.tis.common.utils.Assert;
import junit.framework.TestCase;

import java.util.Optional;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/15
 */
public class TestDataSourceFactory extends TestCase {


    public void testSchemaSupported_getCDCTableTokens() {
        // 模拟mysql8
        Optional<DataSourceFactory.ISchemaSupported> schemaSupport = Optional.of(new DataSourceFactory.ISchemaSupported() {
            @Override
            public String getDBSchema() {
                return "tisSchema";
            }

            @Override
            public boolean isUseDBNameAsSchemaName() {
                return true;
            }
        });
        Assert.assertEquals("tis_db_name1.order_001", DataSourceFactory.ISchemaSupported.getCDCTableTokens(schemaSupport, "tis_db_name1", "order_001"));

        // 模拟sqlserver
        schemaSupport = Optional.of(new DataSourceFactory.ISchemaSupported() {
            @Override
            public String getDBSchema() {
                return "tisSchema";
            }

            @Override
            public boolean isUseDBNameAsSchemaName() {
                return false;
            }
        });
        Assert.assertEquals("tisSchema.order_001", DataSourceFactory.ISchemaSupported.getCDCTableTokens(schemaSupport, "tis_db_name1", "order_001"));

    }
}
