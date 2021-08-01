/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 *   This program is free software: you can use, redistribute, and/or modify
 *   it under the terms of the GNU Affero General Public License, version 3
 *   or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.qlangtech.tis.exec.datax;

import com.qlangtech.tis.realtime.yarn.rpc.IncrStatusUmbilicalProtocol;
import com.qlangtech.tis.rpc.grpc.log.ILogReporter;
import com.tis.hadoop.rpc.StatusRpcClient;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-04 09:19
 **/
public class DataXAssembleSvcCompsite extends StatusRpcClient.AssembleSvcCompsite {

    public DataXAssembleSvcCompsite(IncrStatusUmbilicalProtocol statReceiveSvc) {
        super(statReceiveSvc, new StatusRpcClient.MockLogReporter());
    }
    @Override
    public void close() {
    }

    @Override
    public StatusRpcClient.AssembleSvcCompsite unwrap() {
        return this;
    }
}
