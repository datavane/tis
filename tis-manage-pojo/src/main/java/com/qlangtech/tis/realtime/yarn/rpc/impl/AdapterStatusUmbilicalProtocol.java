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

package com.qlangtech.tis.realtime.yarn.rpc.impl;

import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildSharedPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.realtime.yarn.rpc.*;
import org.apache.commons.lang.NotImplementedException;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-03 14:13
 **/
public class AdapterStatusUmbilicalProtocol implements IncrStatusUmbilicalProtocol {
    @Override
    public PingResult ping() {
        throw new NotImplementedException();
    }

    @Override
    public MasterJob reportStatus(UpdateCounterMap upateCounter) {
        throw new NotImplementedException();
    }

    @Override
    public void nodeLaunchReport(LaunchReportInfo launchReportInfo) {
        throw new NotImplementedException();
    }

    @Override
    public void reportDumpTableStatus(DumpPhaseStatus.TableDumpStatus tableDumpStatus) {
        throw new NotImplementedException();
    }

    @Override
    public void reportBuildIndexStatus(BuildSharedPhaseStatus buildStatus) {
        throw new NotImplementedException();
    }
}
