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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.lucene.util.PriorityQueue;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MenuNamePriorityQueue extends PriorityQueue<Map.Entry<Order2Doc, AtomicInteger>> {

    public MenuNamePriorityQueue(int maxSize) {
        super(maxSize);
    }

    @Override
    protected boolean lessThan(Entry<Order2Doc, AtomicInteger> a, Entry<Order2Doc, AtomicInteger> b) {
        return a.getValue().get() < b.getValue().get();
    }

    public static void main(String[] args) {
    // MenuNamePriorityQueue menuNamePriorityQueue = new MenuNamePriorityQueue(1);
    // Map<Order2Doc,AtomicInteger> map = new HashMap();
    // for(int i=0;i<10;i++){
    // map.put(new Order2Doc(i,i),new AtomicInteger(i));
    // }
    // for(Map.Entry<Order2Doc,AtomicInteger> entry:map.entrySet()){
    // menuNamePriorityQueue.insertWithOverflow(entry);
    // }
    // for(int i=0;i<menuNamePriorityQueue.size();i++){
    // System.out.println(menuNamePriorityQueue.pop());
    // }
    }
}
