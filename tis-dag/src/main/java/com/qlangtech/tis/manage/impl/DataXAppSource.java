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
package com.qlangtech.tis.manage.impl;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.ITaskPhaseInfo;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.sql.parser.er.IPrimaryTabFinder;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-09 16:07
 */
public class DataXAppSource implements IAppSource {

    //=================================================================
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
