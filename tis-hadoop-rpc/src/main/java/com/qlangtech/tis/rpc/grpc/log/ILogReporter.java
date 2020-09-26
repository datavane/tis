/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.rpc.grpc.log;

import com.qlangtech.tis.rpc.grpc.log.stream.PMonotorTarget;
import com.qlangtech.tis.trigger.jst.ILogListener;
import com.qlangtech.tis.trigger.jst.MonotorTarget;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.BlockingQueue;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public interface ILogReporter {

    StreamObserver<PMonotorTarget> registerMonitorEvent(ILogListener logListener);

    java.util.Iterator<com.qlangtech.tis.rpc.grpc.log.stream.PPhaseStatusCollection> buildPhraseStatus(Integer taskid) throws Exception;
}
