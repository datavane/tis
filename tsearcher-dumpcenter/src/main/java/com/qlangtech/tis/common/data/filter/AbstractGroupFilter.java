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
package com.qlangtech.tis.common.data.filter;

import java.util.Map;

/*
 * 根据groupNumber对shardKey的抽象操作类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstractGroupFilter implements GroupFilter {

    /**
     * @uml.property  name="groupNumber"
     */
    protected int groupNumber;

    /**
     * @uml.property  name="shardKey"
     */
    protected String shardKey;

    /**
     * @uml.property  name="shardNumber"
     */
    protected int shardNumber;

    /**
     * @return
     * @uml.property  name="groupNumber"
     */
    public int getGroupNumber() {
        return groupNumber;
    }

    /**
     * @param groupNumber
     * @uml.property  name="groupNumber"
     */
    public void setGroupNumber(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    /**
     * @return
     * @uml.property  name="shardKey"
     */
    public String getShardKey() {
        return shardKey;
    }

    /**
     * @param shardKey
     * @uml.property  name="shardKey"
     */
    public void setShardKey(String shardKey) {
        this.shardKey = shardKey;
    }

    /**
     * @return
     * @uml.property  name="shardNumber"
     */
    public int getShardNumber() {
        return shardNumber;
    }

    /**
     * @param shardNumber
     * @uml.property  name="shardNumber"
     */
    public void setShardNumber(int shardNumber) {
        this.shardNumber = shardNumber;
    }

    public abstract boolean accept(Map<String, String> rowData);
}
