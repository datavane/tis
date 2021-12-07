/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.db.parser;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.util.DescribableJSON;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DBConfigSuit {
  private final DatasourceDb db;

  public DBConfigSuit(DatasourceDb db) {
    this.db = db;
  }

  public Integer getDbId() {
    return this.db.getId();
  }

  public String getName() {
    return this.db.getName();
  }


  private DescribableJSON detailed;
  private DescribableJSON facade;

  public JSONObject getDetailed() throws Exception {
    return this.detailed.getItemJson();
  }

  public JSONObject getFacade() throws Exception {
    if (this.facade == null) {
      return null;
    }
    return this.facade.getItemJson();
  }

  public void setDetailed(DataSourceFactory detailed) {
    if (detailed == null) {
      throw new IllegalStateException("param detailed can not be null");
    }
    this.detailed = new DescribableJSON(detailed);
  }

  public void setFacade(DataSourceFactory facade) {
    if (facade == null) {
      throw new IllegalStateException("param detailed can not be null");
    }
    this.facade = new DescribableJSON(facade);
  }
}
