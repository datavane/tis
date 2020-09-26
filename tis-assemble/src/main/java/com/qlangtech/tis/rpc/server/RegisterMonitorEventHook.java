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
package com.qlangtech.tis.rpc.server;

import ch.qos.logback.classic.spi.LoggingEvent;
import com.qlangtech.tis.rpc.grpc.log.stream.PExecuteState;
import java.io.File;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-18 14:43
 */
public class RegisterMonitorEventHook {

    void startSession() {
    }

    void send2Client(PExecuteState s, LoggingEvent e) {
    }

    void closeSession() {
    }

    public void validateExpect() {
    }

    public void send2ClientFromFileTailer(File logFile, PExecuteState s) {
    }
}
