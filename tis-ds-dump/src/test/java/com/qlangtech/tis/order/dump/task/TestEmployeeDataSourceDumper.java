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
package com.qlangtech.tis.order.dump.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.IDataSourceDumper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-02 17:32
 */
public class TestEmployeeDataSourceDumper implements IDataSourceDumper {
    private List<ColumnMetaData> colsMeta;
    private List<Map<String, String>> dumpData;


    public TestEmployeeDataSourceDumper() throws IOException {
        this.colsMeta = Lists.newArrayList();
        List<String> lines = null;
        try (InputStream input = TestEmployeeDataSourceDumper.class.getResourceAsStream("employee_data_source.txt")) {
            Assert.assertNotNull(input);
            lines = IOUtils.readLines(input, TisUTF8.get());
            String[] titles = StringUtils.split(lines.get(1), "|");
            ColumnMetaData cm = null;
            for (int i = 0; i < titles.length; i++) {
                //int index, String key, int type, boolean pk
                cm = new ColumnMetaData(i, StringUtils.trimToEmpty(titles[i]), Types.VARCHAR, false);
                this.colsMeta.add(cm);
            }
        }
        String line = null;
        dumpData = Lists.newArrayList();
        Map<String, String> row = null;
        String[] vals = null;
        for (int i = 3; i < lines.size(); i++) {
            row = Maps.newHashMap();
            line = lines.get(i);
            vals = StringUtils.split(line, "|");
            for (int titleIndex = 0; titleIndex < colsMeta.size(); titleIndex++) {
                row.put(colsMeta.get(titleIndex).getKey(), StringUtils.trimToNull(vals[titleIndex]));
            }
            dumpData.add(row);
        }

        Assert.assertTrue(dumpData.size() > 0);
    }

    public static void main(String[] args) throws Exception {
        TestEmployeeDataSourceDumper d = new TestEmployeeDataSourceDumper();
    }

    @Override
    public void closeResource() {

    }

    @Override
    public int getRowSize() {
        return dumpData.size();
    }

    @Override
    public List<ColumnMetaData> getMetaData() {
        return this.colsMeta;
    }

    @Override
    public Iterator<Map<String, String>> startDump() {
        return dumpData.iterator();
    }

    @Override
    public String getDbHost() {
        return "getDbHost";
    }
}
