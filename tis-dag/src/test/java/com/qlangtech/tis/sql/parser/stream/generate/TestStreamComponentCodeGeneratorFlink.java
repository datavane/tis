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

package com.qlangtech.tis.sql.parser.stream.generate;

import com.google.common.collect.Lists;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.sql.parser.tuple.creator.IStreamIncrGenerateStrategy;
import junit.framework.TestCase;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-10-07 18:15
 **/
public class TestStreamComponentCodeGeneratorFlink extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        CenterResource.setNotFetchFromCenterRepository();
    }

    /**
     * 测试单表增量脚本生成
     *
     * @throws Exception
     */
    public void testSingleTableCodeGenerator() throws Exception {

        //  CoreAction.create
      //  String topologyName = "employees4local";
        String collectionName = "mysql_elastic";


        IAppSource appSource = IAppSource.load(null, collectionName);
        assertTrue(appSource instanceof DataxProcessor);
        DataxProcessor dataXProcessor = (DataxProcessor) appSource;

//        Optional<ERRules> erRule = ERRules.getErRule(topologyName);
//        // 测试针对单表的的topology增量脚本生成
        long timestamp = 20191111115959l;
//        SqlTaskNodeMeta.SqlDataFlowTopology topology = SqlTaskNodeMeta.getSqlDataFlowTopology(topologyName);
//        assertNotNull(topology);
//        if (!erRule.isPresent()) {
//            ERRules.createDefaultErRule(topology);
//        }

        List<FacadeContext> facadeList = Lists.newArrayList();
        StreamComponentCodeGeneratorFlink streamCodeGenerator
                = new StreamComponentCodeGeneratorFlink(collectionName, timestamp, facadeList, (IStreamIncrGenerateStrategy) appSource, true);
        //EasyMock.replay(streamIncrGenerateStrategy);
        streamCodeGenerator.build();

        TestStreamComponentCodeGenerator
                .assertGenerateContentEqual(timestamp, collectionName, "MysqlElasticListener.scala");
        // EasyMock.verify(streamIncrGenerateStrategy);
    }


}
