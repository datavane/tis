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
package com.qlangtech.tis.solrextend.queryparse.s4menuNameSuggestion;

import com.qlangtech.tis.solrextend.queryparse.BitQuery;
import org.apache.lucene.util.BitSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FreqBitQuery extends BitQuery {

    private Map<Integer, Integer> /*doc_id,count*/
    doc2freq;

    private int hashcode;

    public FreqBitQuery(BitSet bitSet, Map<Integer, /*doc_id,count*/
    Integer> doc2freq, int hashcode) {
        super(bitSet);
        this.doc2freq = doc2freq;
        this.hashcode = hashcode;
    }

    @Override
    protected float getScore(int docid) {
        return doc2freq.get(docid);
    }

    @Override
    public String toString(String field) {
        return doc2freq.toString();
    }

    @Override
    public int hashCode() {
        return this.hashcode;
    }
}
