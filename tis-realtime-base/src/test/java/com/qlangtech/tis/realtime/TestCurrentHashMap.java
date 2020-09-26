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
package com.qlangtech.tis.realtime;

import java.util.concurrent.ConcurrentHashMap;
import junit.framework.TestCase;
import com.qlangtech.tis.realtime.transfer.IPk;
import com.qlangtech.tis.realtime.transfer.IPojo;
import com.qlangtech.tis.realtime.transfer.impl.DefaultPk;
import com.qlangtech.tis.realtime.transfer.impl.DefaultPojo;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestCurrentHashMap extends TestCase {

    public void test() {
        ConcurrentHashMap<IPk, IPojo> pojoMap = new ConcurrentHashMap<>();
        IPk pk = new DefaultPk("123");
        DefaultPojo pojo = new DefaultPojo(null);
        pojo.setPrimaryKey(pk);
        IPojo pojo1 = pojoMap.putIfAbsent(pk, pojo);
        System.out.println(pojo1 + "_" + pojoMap.get(pk).getPK());
        DefaultPojo pojo2 = new DefaultPojo(null);
        pojo2.setPrimaryKey(new DefaultPk("124"));
        pojo1 = pojoMap.putIfAbsent(pk, pojo2);
        System.out.println(pojo1.getPK() + "_" + pojoMap.get(pk).getPK());
    }
}
