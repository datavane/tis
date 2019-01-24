/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.wangjubao.jingwei;

import static com.qlangtech.tis.wangjubao.jingwei.Alias.$;
import com.qlangtech.tis.realtime.transfer.BasicONSListener.RowVersionCreator;
import com.qlangtech.tis.realtime.transfer.impl.DefaultTable;
import com.qlangtech.tis.realtime.transfer.impl.DefaultTable.EventType;
import junit.framework.Assert;
import junit.framework.TestCase;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestAliasList extends TestCase {

    public void testIgnoreColumn() {
        AliasList aliasList = new AliasList("user", $("name"), $("age"), $("last_ver").ignoreChange());
        DefaultTable table = new DefaultTable("user", new RowVersionCreator[] {});
        table.setEventType(EventType.UPDATE);
        table.addColumn("name", "百岁");
        table.addBeforeColumn("name", "百岁");
        table.addColumn("age", "11");
        table.addBeforeColumn("age", "11");
        table.addColumn("last_ver", "1");
        table.addBeforeColumn("last_ver", "2");
        Assert.assertTrue(!aliasList.getColumnsChange(table));
        aliasList = new AliasList("user", $("name"), $("age"), $("last_ver"));
        Assert.assertTrue(aliasList.getColumnsChange(table));
    }
}
