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
package com.qlangtech.tis.realtime.transfer;

// import java.util.Map;
import java.util.Map;
import java.util.Set;
import com.qlangtech.tis.realtime.transfer.impl.DefaultTable.EventType;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface ITable extends IRowValueGetter {

    public String getTableName();

    /**
     * 数据更新版本，一般以时间戳标记，防止在row更新過程中被脏数据覆盖
     *
     * @return
     */
    public long getVersion();

    public Map<String, String> getColumns();

    public EventType getEventType();

    // /**
    // * 取得更新之后最新值
    // *
    // * @param key
    // * @return
    // */
    // public String getColumn(String key);
    /**
     * 取得更新后的 int值
     *
     * @param key
     * @param errorCare
     *            当转型错误是否要抛出异常
     * @return
     */
    public int getInt(String key, boolean errorCare);

    public long getLong(String key, boolean errorCare);

    public double getDouble(String key, boolean errCare);

    public float getFloat(String key, boolean errCare);

    /**
     * 取得老字段值
     *
     * @param key
     * @return
     */
    public String getOldColumn(String key);

    /**
     * 对于参数中提示的几个列是否有更新
     *
     * @param keys
     * @return
     */
    public boolean columnChange(Set<String> keys);

    public void desc(StringBuffer buffer);
}
