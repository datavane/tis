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
package com.qlangtech.tis.wangjubao.jingwei;

import com.qlangtech.tis.exec.IIndexMetaData;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestTableClusterParser extends TestCase {

    public void testParse() throws Exception {
        SolrFieldsParser.ParseResult parseResult = getSchemaReflect();
        TableCluster tabCluster = getTableCluster(parseResult);
        Table totalpayinfo = tabCluster.getTable("totalpayinfo");
        TabField currDateFieldProcess = totalpayinfo.findAliasColumn("curr_date");
        assertNotNull(currDateFieldProcess);
        // Map<String, String> valuesStore = Maps.newHashMap();
        // valuesStore.put("curr_date", "2020-11-09");
        Object result = currDateFieldProcess.getAliasProcess().process("2020-11-09");
        assertNotNull(result);
        assertEquals("20201109", String.valueOf(result));
    }

    public static TableCluster getTableCluster(SolrFieldsParser.ParseResult parseResult) throws Exception {
        TableCluster tabCluster = null;
        try (InputStream input = TestTableClusterParser.class.getResourceAsStream("field-transfer.xml")) {
            tabCluster = TableClusterParser.parse(IOUtils.toString(input, TisUTF8.get()), parseResult);
        }
        assertNotNull(tabCluster);
        return tabCluster;
    }

    public static SolrFieldsParser.ParseResult getSchemaReflect() throws Exception {
        // SolrFieldsParser solrFieldsParser = new SolrFieldsParser();
        SolrFieldsParser.ParseResult parseResult = null;
        try (InputStream reader = TestTableClusterParser.class.getResourceAsStream("s4totalpay-schema.xml")) {
            IIndexMetaData meta = SolrFieldsParser.parse(() -> IOUtils.toByteArray(reader), (fieldType) -> false);
            // solrFieldsParser.parseSchema(reader, false);
            parseResult = meta.getSchemaParseResult();
        }
        assertNotNull(parseResult);
        return parseResult;
    }
}
