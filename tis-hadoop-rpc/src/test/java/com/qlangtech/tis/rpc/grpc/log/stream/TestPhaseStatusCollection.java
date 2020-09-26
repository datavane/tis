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
package com.qlangtech.tis.rpc.grpc.log.stream;

import com.google.protobuf.util.JsonFormat;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.rpc.grpc.log.common.TableDumpStatus;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import java.io.InputStream;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-12 11:19
 */
public class TestPhaseStatusCollection extends TestCase {

    public void testJsonFormat() throws Exception {
        JsonFormat.Printer jsonPrint = JsonFormat.printer();
        PDumpPhaseStatus.Builder dumpState = PDumpPhaseStatus.newBuilder();
        String tableName = "totalpayinfo";
        TableDumpStatus.Builder dumpBuilder = TableDumpStatus.newBuilder();
        dumpBuilder.setWaiting(true);
        dumpBuilder.setComplete(true);
        dumpBuilder.setFaild(true);
        dumpBuilder.setTaskid(9527);
        dumpBuilder.setTableName(tableName);
        dumpBuilder.setReadRows(127);
        dumpBuilder.setAllRows(9999);
        dumpState.putTablesDump(tableName, dumpBuilder.build());
        try (InputStream input = TestPhaseStatusCollection.class.getResourceAsStream("totalpayinfo_proto_json_format.json")) {
            assertEquals(IOUtils.toString(input, TisUTF8.get()), jsonPrint.print(dumpState));
        }
    }
}
