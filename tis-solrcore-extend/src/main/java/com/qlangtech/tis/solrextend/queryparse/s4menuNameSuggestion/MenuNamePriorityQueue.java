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
package com.qlangtech.tis.solrextend.queryparse.s4menuNameSuggestion;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.lucene.util.PriorityQueue;

/* *
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
