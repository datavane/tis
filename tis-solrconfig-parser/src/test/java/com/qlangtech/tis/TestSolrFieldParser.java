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
package com.qlangtech.tis;

import java.io.InputStream;
import com.qlangtech.tis.exec.IIndexMetaData;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestSolrFieldParser extends TestCase {

    public void testSchemaFields() throws Exception {
        // SolrFieldsParser solrFieldsParser = new SolrFieldsParser();
        try (InputStream reader = this.getClass().getResourceAsStream("schema-sample.txt")) {
             assertNotNull(reader);
            IIndexMetaData meta = SolrFieldsParser.parse(() -> IOUtils.toByteArray(reader));
            ParseResult parseResult = meta.getSchemaParseResult();
            Assert.assertTrue("isValid shall be true", parseResult.isValid());
            Assert.assertEquals(20, parseResult.dFields.size());
            System.out.println("dFields.size:" + parseResult.dFields.size());
            Assert.assertEquals("parseResult.getFieldNameSet() can not be empty", 20, parseResult.getFieldNameSet().size());
            System.out.println(parseResult.getFieldNameSet().size());
        }
    }
}
