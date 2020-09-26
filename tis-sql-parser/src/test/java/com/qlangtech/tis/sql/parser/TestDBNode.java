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
package com.qlangtech.tis.sql.parser;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestDBNode extends TestCase {

    public void testLoadAndDump() throws Exception {
        List<DBNode> nodes = Lists.newArrayList();
        String dbname = "baisuidbName";
        int dbid = 9527;
        long timestamp = 12378845l;
        DBNode node = new DBNode(dbname, dbid);
        node.setTimestampVer(timestamp);
        nodes.add(node);
        File f = new File("dataflow/dbnodes.yaml");
        DBNode.dump(nodes, f);
        try (InputStream input = FileUtils.openInputStream(f)) {
            nodes = DBNode.load(input);
        }
        assertEquals(1, nodes.size());
        node = nodes.get(0);
        assertEquals(dbname, node.getDbName());
        assertEquals(dbid, node.getDbId());
        assertEquals(timestamp, node.getTimestampVer());
    }
}
