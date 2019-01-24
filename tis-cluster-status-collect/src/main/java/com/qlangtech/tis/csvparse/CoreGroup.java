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
package com.qlangtech.tis.csvparse;

import java.util.HashSet;
import java.util.Set;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CoreGroup {

    private final int index;

    private final String name;

    public CoreGroup(int index, String name) {
        super();
        this.index = index;
        this.name = name;
    }

    private long indexSize;

    private long indexNum;

    private long queryCount;

    private float queryConsumeTime;

    public long getIndexSize() {
        return indexSize;
    }

    public void setIndexSize(long indexSize) {
        this.indexSize = indexSize;
    }

    public long getIndexNum() {
        return indexNum;
    }

    public void setIndexNum(long indexNum) {
        this.indexNum = indexNum;
    }

    public long getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(long queryCount) {
        this.queryCount = queryCount;
    }

    public float getQueryConsumeTime() {
        return queryConsumeTime;
    }

    public void setQueryConsumeTime(float queryConsumeTime) {
        this.queryConsumeTime = queryConsumeTime;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + index;
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    public static void main(String[] arg) {
        CoreGroup group = new CoreGroup(0, "abc");
        Set<CoreGroup> groupSet = new HashSet<CoreGroup>();
        groupSet.add(group);
        group = new CoreGroup(0, "abc");
        groupSet.add(group);
        group = new CoreGroup(0, "abcc");
        groupSet.add(group);
        System.out.println(groupSet.size());
    }
}
