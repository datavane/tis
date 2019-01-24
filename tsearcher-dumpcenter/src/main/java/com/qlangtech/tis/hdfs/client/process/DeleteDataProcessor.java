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
package com.qlangtech.tis.hdfs.client.process;

import java.util.Map;
import com.qlangtech.tis.hdfs.util.Constants;

/*
 *  基本照搬原来terminator 的删除Doc的逻辑
 * 如果业务方在增量模式下需要删除数据的话，则需要实现此类，
 * 有删除操作的，必须在原来的Map添加OPT_COLUMN列，标示这条记录是删除还是其他操作
 * @since 2011-8-25 下午07:14:35
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class DeleteDataProcessor implements DataProcessor<String, String> {

    // 如果是需要删除的数据则会在存储HDFS中加入一列，来标示该Doc是需要删除的
    @Override
    public boolean process(Map<String, String> map) {
        map.put(Constants.DEL_ID, getUniqueValue(map));
        if (this.needDelete(map)) {
            map.put(Constants.OPT_COLUMN, Constants.DEL_OPT);
        } else {
            map.put(Constants.OPT_COLUMN, Constants.ADD_OPT);
        }
        map.remove(getDelMarkColumn());
        return true;
    }

    /**
     * 判断该Doc是否是需要删除
     *
     * @param map 一个Doc的集合
     * @return
     */
    protected boolean needDelete(Map<String, String> map) {
        return map.get(getDelMarkColumn()).equals(getDelMarkValue());
    }

    /**
     * @return 标记删除的列
     */
    protected abstract String getDelMarkColumn();

    /**
     * @return 标记删除的值
     */
    protected abstract String getDelMarkValue();

    /**
     * @param row
     * @return 每行记录的唯一值
     */
    protected abstract String getUniqueValue(Map<String, String> row);
}
