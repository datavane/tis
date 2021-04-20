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

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.ITaskPhaseInfo;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.sql.parser.er.IPrimaryTabFinder;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;

import java.util.List;

/**
 * 本地执行data实例
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-19 11:36
 */
public class LocalDataxProcessor extends DataxProcessor {

  @Override
  protected int getChannel() {
    return 0;
  }

  @Override
  protected int getErrorLimitCount() {
    return 0;
  }

  @Override
  protected int getErrorLimitPercentage() {
    return 0;
  }

  @Override
  public IDataxReader getReader() {
    return null;
  }

  @Override
  public IDataxWriter getWriter() {
    return null;
  }

  @Override
  public List<ColumnMetaData> reflectCols() {
    return null;
  }

  @Override
  public boolean triggerFullIndexSwapeValidate(IMessageHandler msgHandler, Context ctx) {
    return false;
  }

  @Override
  public EntityName getTargetEntity() {
    return null;
  }

  @Override
  public IPrimaryTabFinder getPrimaryTabFinder() {
    return null;
  }

  @Override
  public ExecuteResult getProcessDataResults(IExecChainContext execChainContext, ISingleTableDumpFactory singleTableDumpFactory, IDataProcessFeedback dataProcessFeedback, ITaskPhaseInfo taskPhaseInfo) throws Exception {
    return null;
  }
}
