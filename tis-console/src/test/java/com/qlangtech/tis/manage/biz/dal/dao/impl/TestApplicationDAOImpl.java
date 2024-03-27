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

package com.qlangtech.tis.manage.biz.dal.dao.impl;

import com.qlangtech.tis.BasicActionTestCase;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-03-26 10:10
 **/
public class TestApplicationDAOImpl extends BasicActionTestCase {

  public void testUpdateSelect() throws Exception {
    IApplicationDAO applicationDAO = runContext.getApplicationDAO();


    try (Connection conn = ((SqlMapClientDaoSupport) applicationDAO).getDataSource().getConnection()) {

//      conn.createStatement().execute("CREATE TABLE application3 (\n" +
//        "  app_id bigint NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) ,\n" +
//        "  \n" +
//        "  full_build_cron_time varchar(120) DEFAULT 'full_build_cron_time' ,\n" +
//        "  PRIMARY KEY (app_id)\n" +
//        ")");


      DatabaseMetaData metaData = conn.getMetaData();
//      try (ResultSet tables = metaData.getTables(null, null, "application", null)) {
//        int count = 0;
//        List<String> matchEntries = Lists.newArrayList();
//        while (tables.next()) {
//          matchEntries.add(tables.getString("TABLE_NAME")
//            + "(" + tables.getString("TABLE_TYPE")
//            + "," + tables.getString("TABLE_SCHEM") + ")");
//          count++;
//        }
//        if (count < 1) {
//          throw new TableNotFoundException(this, table.getFullName() + ",url:" + conn.getUrl());
//        } else if (count > 1) {
//          throw new IllegalStateException("duplicate table entities exist:" + String.join(",", matchEntries));
//        }
//      }

//      ResultSet console_db = metaData.getTables(null, null, null, null);
//      while (console_db.next()) {
//        System.out.println(console_db.getString(3));
//      }

      ResultSet colsResult = metaData.getColumns(null, null, "APPLICATION", null);

      while (colsResult.next()) {
        System.out.println(colsResult.getString("COLUMN_NAME")
          + ",type:" //colsResult.getString("KEY_COLUMN_SIZE")
          + colsResult.getString(6) + ",size:" + colsResult.getString(7) + "\n");
      }
    }

    Application record = new Application();
    String crontime = "{\n" +
      "  \"workflow_id\":6,\n" +
      "  \"execRange\":[\n" +
      "    \"dump\",\n" +
      "    \"dump\"\n" +
      "  ]\n" +
      "}";
    System.out.println("length:" + crontime.length());
    record.setFullBuildCronTime(crontime);
    ApplicationCriteria example = new ApplicationCriteria();
    example.createCriteria().andAppIdNotEqualTo(2);
//    int updateCount = applicationDAO.updateByExampleSelective(record, example);
//    Assert.assertEquals(1, updateCount);
  }
}
