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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.beanutils.BeanUtils;
import com.qlangtech.tis.realtime.transfer.BasicPojoConsumer.TisSolrInputDocument;
import com.qlangtech.tis.realtime.transfer.ITable;
import com.qlangtech.tis.realtime.transfer.impl.BeanValueGetter;
import com.google.common.collect.Lists;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.wangjubao.jingwei.Alias.ITransfer;
import static java.util.stream.Collectors.toSet;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AliasList {

    private final List<Alias> aliasList;

    private final Set<String> focusColumns;

    private final String tableName;

    public AliasList(String tableName, Alias... alias) {
        this.aliasList = Collections.unmodifiableList(Lists.newArrayList(alias));
        this.focusColumns = this.aliasList.stream().filter((a) -> {
            return !a.ignoreChange;
        }).map(Alias::getName).collect(toSet());
        // Sets.newHashSet(Lists.transform(this.aliasList.stream().filter((a) -> {
        // return !a.ignoreChange;
        // })., (a) -> {
        // return a.getName();
        // }));
        this.tableName = tableName;
    }

    public List<Alias> getAliasList() {
        return this.aliasList;
    }

    public boolean getColumnsChange(ITable table) {
        Assert.assertEquals(this.tableName, table.getTableName());
        return table.columnChange(focusColumns);
    }

    public void copy2TisDocument(Serializable bean, TisSolrInputDocument document) {
        BeanValueGetter propGetter = new BeanValueGetter(bean);
        aliasList.forEach((a) -> {
            try {
                if (a.copy) {
                    ITransfer transfer = a.getValTransfer();
                    document.setField(tableName, a.getToName(), transfer.process(propGetter, BeanUtils.getProperty(bean, a.getBeanPropName())));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void copy2TisDocument(ITable tab, TisSolrInputDocument document) {
        Assert.assertEquals(this.tableName, tab.getTableName());
        aliasList.forEach((a) -> {
            if (a.copy) {
                document.setField(tab.getTableName(), a.getToName(), a.getValTransfer().process(tab, tab.getColumn(a.getName())));
            }
        });
    }

    public void cleanTisDocument(TisSolrInputDocument document) {
        aliasList.forEach((a) -> {
            try {
                if (a.copy) {
                    document.clearField(a.getToName());
                }
            // document.setField(tabName, a.getToName(),
            // BeanUtils.getProperty(bean, a.getBeanPropName()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
