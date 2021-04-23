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
package com.qlangtech.tis.datax.impl;

import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.plugin.IdentityName;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DataX任务执行方式的抽象
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 16:46
 */
public abstract class DataxProcessor implements IAppSource, IdentityName, IDataxProcessor {


  private List<TableAlias> tableMaps;

  @Override
  public Map<String, TableAlias> getTabAlias() {
    if (tableMaps == null) {
      return Collections.emptyMap();
    }
    return this.tableMaps.stream().collect(Collectors.toMap((m) -> m.getFrom(), (m) -> m));
  }

  @Override
  public IDataxReader getReader() {
    return DataxReader.load(this.identityValue());
  }

  @Override
  public IDataxWriter getWriter() {
    return DataxWriter.load(this.identityValue());
  }

  public void setTableMaps(List<TableAlias> tableMaps) {
    this.tableMaps = tableMaps;
  }

//     "setting": {
//        "speed": {
//            "channel": 3
//        },
//        "errorLimit": {
//            "record": 0,
//                    "percentage": 0.02
//        }
//    },

}
