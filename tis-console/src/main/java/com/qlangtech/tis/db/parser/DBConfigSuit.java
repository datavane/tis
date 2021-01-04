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
