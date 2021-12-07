/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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
