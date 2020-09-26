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
package com.qlangtech.tis.sql.parser.stream.generate;

import com.qlangtech.tis.sql.parser.BasicTestCase;
import com.qlangtech.tis.sql.parser.er.ERRules;
import com.qlangtech.tis.sql.parser.er.TableRelation;
import com.qlangtech.tis.sql.parser.er.TestERRules;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestFlatTableRelation extends BasicTestCase {

    public void testGetFinalLinkKey() {
        ERRules totalpayErRules = TestERRules.getTotalpayErRules();
        Optional<TableRelation> orderdetailParentRelation = totalpayErRules.getFirstParent("orderdetail");
        assertTrue(orderdetailParentRelation.isPresent());
        Optional<TableRelation> instancedetailParentRelation = totalpayErRules.getFirstParent("instancedetail");
        assertTrue(instancedetailParentRelation.isPresent());
        // orderdetailParentRelation.get().
        String primaryKey = "totalpay_id";
        String entityId = "entity_id";
        TableRelation.FinalLinkKey finalLinkKey = FlatTableRelation.getFinalLinkKey(primaryKey, orderdetailParentRelation.get().getCurrentTableRelation(true));
        // for (TableRelation r : totalpayErRules.getRelationList()) {
        // System.out.println(r);
        // }
        assertNotNull(finalLinkKey);
        assertEquals(primaryKey, finalLinkKey.linkKeyName);
        Optional<TableRelation> servicebillinfoParentRelation = totalpayErRules.getFirstParent("servicebillinfo");
        assertTrue(servicebillinfoParentRelation.isPresent());
        finalLinkKey = FlatTableRelation.getFinalLinkKey(primaryKey, servicebillinfoParentRelation.get().getCurrentTableRelation(true));
        assertNotNull(finalLinkKey);
        assertEquals("servicebill_id", finalLinkKey.linkKeyName);
        finalLinkKey = FlatTableRelation.getFinalLinkKey(entityId, servicebillinfoParentRelation.get().getCurrentTableRelation(true));
        assertNotNull(finalLinkKey);
        assertEquals(entityId, finalLinkKey.linkKeyName);
    }
}
