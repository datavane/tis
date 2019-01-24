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
package com.qlangtech.tis.realtime.transfer.n2one;

import static com.qlangtech.tis.wangjubao.jingwei.Alias.alias;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.realtime.transfer.BasicONSListener;
import com.qlangtech.tis.realtime.transfer.BasicPojoConsumer;
import com.qlangtech.tis.realtime.transfer.IPojo;
import com.qlangtech.tis.realtime.transfer.ITable;
import com.qlangtech.tis.realtime.transfer.impl.CompositePK;
import com.qlangtech.tis.realtime.transfer.impl.DefaultTable.EventType;
import com.google.common.collect.Lists;
import com.qlangtech.tis.wangjubao.jingwei.Alias;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class N2OneConsumer extends BasicPojoConsumer {

    public N2OneConsumer(BasicONSListener onsListener) {
        super(onsListener);
    }

    private static final List<Alias> CUSTOMER_ALIAS_LIST = Lists.newArrayList(alias("name", "customer_name"), alias("spell", "customer_spell"), alias("phone", "customer_phone"), alias("sex", "customer_sex"), alias("birthday", "customer_birthday"), alias("extend_fields", "customer_extend_fields"));

    protected abstract ITable getPrimaryTable(IPojo pojo);

    protected abstract String getForeignTableName();

    protected abstract String getPrimaryTableName();

    protected abstract ITable getForeignTable(IPojo pojo);

    protected abstract Object getForeignPojoFromOuterStorage(String fk, CompositePK pk);

    public boolean indexIsExist(IPojo pojo, TisSolrInputDocument addDoc) throws Exception {
        // 该方法处理cardId不存在于solr记录中的情况，该方法执行完成后会继续执行processPojo
        CompositePK cardid = (CompositePK) pojo.getPK();
        ITable card = getPrimaryTable(pojo);
        // pojo.getTable(CustomerCardListener.TAB_CUSTOMER);
        ITable customer = getForeignTable(pojo);
        // 没有card记录则忽略，但不会出现card = null的情况
        if (card != null && customer == null) {
        // StringBuilder tabs = new StringBuilder();
        // for (Map.Entry<String, ITable> entry : pojo.getTables()) {
        // tabs.append(entry.getKey()).append(",");
        // }
        // log.warn("table:" + CustomerCardListener.TAB_CARD + " is not
        // exist in:"
        // + tabs.toString());
        // try {
        // for (Map.Entry<String, String> entry :
        // card.getColumns().entrySet()) {
        // if (StringUtils.isNotBlank(card.getColumn(entry.getKey()))) {
        // addDoc.setField(card.getTableName(), entry.getKey(),
        // entry.getValue());
        // }
        // }
        // } catch (Exception e) {
        // log.error(e.getMessage(), e);
        // return false;
        // }
        // 
        // if (customer == null) {
        // // load customer from db
        // 
        // // addDoc.setField();
        // }
        }
        if (customer != null && card == null) {
        }
        return true;
    }

    @Override
    protected boolean processPojo(IPojo pojo, TisSolrInputDocument addDoc) throws Exception {
        // 该方法只处理cardId存在solr记录中的情况
        CompositePK pk = (CompositePK) pojo.getPK();
        // 主表
        ITable ptable = getPrimaryTable(pojo);
        // 外表
        ITable ftable = getForeignTable(pojo);
        // }
        if (ptable != null) {
            // 因为card信息一定存在，可以直接更新(
            for (Map.Entry<String, String> entry : ptable.getColumns().entrySet()) {
                // if (StringUtils.isNotBlank(card.getColumn(entry.getKey()))) {
                addDoc.setField(ptable.getTableName(), entry.getKey(), entry.getValue());
            // }
            }
            String foreignyKeyValue = getForeignKeyValueFromPrimaryTable(ptable);
            if (StringUtils.isBlank(foreignyKeyValue)) {
                for (Alias alias : CUSTOMER_ALIAS_LIST) {
                    addDoc.clearField(alias.getToName());
                }
            } else if (ptable.getEventType() == EventType.ADD && ftable == null) {
                // 当添加了一条主表的记录，且更新中又没有字表字表记录，那就需要到维表中去load回子表记录
                Object foreignPojo = getForeignPojoFromOuterStorage(foreignyKeyValue, pk);
                if (foreignPojo != null) {
                    for (Alias alias : CUSTOMER_ALIAS_LIST) {
                        addDoc.setField(this.getForeignTableName(), alias.getToName(), BeanUtils.getProperty(foreignPojo, alias.getName()));
                    }
                } else {
                    sendRecoredLog.warn("xxxxxxxxxxxxxxxxx");
                }
            }
        }
        if (ftable != null) {
            for (Alias alias : CUSTOMER_ALIAS_LIST) {
                addDoc.setField(ftable.getTableName(), alias.getToName(), ftable.getColumn(alias.getName()));
            }
        }
        return true;
    }

    protected String getForeignKeyValueFromPrimaryTable(ITable ptable) {
        return ptable.getColumn("customer_id");
    }
}
