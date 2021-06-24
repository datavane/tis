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
package com.qlangtech.tis.plugin.ds;


import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract the dataSource modal
 *
 * @author: baisui 百岁
 * @create: 2020-11-24 10:40
 **/
public class DataDumpers {
    public final int splitCount;
    public final Iterator<IDataSourceDumper> dumpers;

    public static DataDumpers create(List<String> jdbcUrls, TISTable table) {
        if (jdbcUrls == null || jdbcUrls.isEmpty()) {
            throw new IllegalArgumentException("param jdbcUrls can not be empty");
        }
        final int length = jdbcUrls.size();
        final AtomicInteger index = new AtomicInteger();
        Iterator<IDataSourceDumper> dsIt = new Iterator<IDataSourceDumper>() {
            @Override
            public boolean hasNext() {
                return index.get() < length;
            }

            @Override
            public IDataSourceDumper next() {
                final String jdbcUrl = jdbcUrls.get(index.getAndIncrement());
                return new DataDumpers.DefaultDumper(jdbcUrl, table);
            }
        };

        DataDumpers dumpers = new DataDumpers(length, dsIt);
        return dumpers;
    }

    public DataDumpers(int splitCount, Iterator<IDataSourceDumper> dumpers) {
        this.splitCount = splitCount;
        this.dumpers = dumpers;
    }

    private static class DefaultDumper implements IDataSourceDumper {

        public final TISTable table;
        private final String jdbcUrl;

        public DefaultDumper(String jdbcUrl, TISTable table) {
            this.table = table;
            if (StringUtils.isEmpty(jdbcUrl)) {
                throw new IllegalArgumentException("param jdbcUrl can not be null");
            }
            this.jdbcUrl = jdbcUrl;
        }

        @Override
        public void closeResource() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getRowSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ColumnMetaData> getMetaData() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Map<String, String>> startDump() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getDbHost() {
            return this.jdbcUrl;
        }
    }
}
