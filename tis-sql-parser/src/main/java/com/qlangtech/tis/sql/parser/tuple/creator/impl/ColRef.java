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
package com.qlangtech.tis.sql.parser.tuple.creator.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.sql.parser.ColName;
import com.qlangtech.tis.sql.parser.meta.NodeType;
import com.qlangtech.tis.sql.parser.tuple.creator.IDataTupleCreator;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-22 16:51
 */
public class ColRef {

    private ListMap /**
     * colName
     */
    colRefMap = new ListMap();

    private Map<String, IDataTupleCreator> /**
     * ref
     */
    baseRefMap = Maps.newHashMap();

    public Set<Map.Entry<String, IDataTupleCreator>> getBaseRefEntities() {
        return this.baseRefMap.entrySet();
    }

    public IDataTupleCreator createBaseRefIfNull(String baseRef) {
        IDataTupleCreator tupleCreator = null;
        if ((tupleCreator = baseRefMap.get(baseRef)) == null) {
            tupleCreator = new TableTupleCreator(baseRef, NodeType.JOINER_SQL);
            baseRefMap.put(baseRef, tupleCreator);
        }
        return tupleCreator;
    }

    public int getBaseRefSize() {
        return this.baseRefMap.size();
    }

    public IDataTupleCreator getTupleCreator(String alias) {
        return this.baseRefMap.get(alias);
    }

    public Set<String> getBaseRefKeys() {
        return this.baseRefMap.keySet();
    }

    public ListMap getColRefMap() {
        return this.colRefMap;
    }

    public static class ListMap {

        private final Map<ColName, IDataTupleCreator> /**
         * colName
         */
        colRefMap = Maps.newHashMap();

        private final List<ColName> cols = Lists.newArrayList();

        public void put(ColName col, IDataTupleCreator tuple) {
            this.colRefMap.put(col, tuple);
            this.cols.add(col);
        }

        public Collection<IDataTupleCreator> values() {
            return this.colRefMap.values();
        }

        public Set<Map.Entry<ColName, IDataTupleCreator>> entrySet() {
            return this.colRefMap.entrySet();
        }

        public int size() {
            return colRefMap.size();
        }

        public IDataTupleCreator get(ColName col) {
            return colRefMap.get(col);
        }

        public List<ColName> keySet() {
            return this.cols;
        }
    }
}
