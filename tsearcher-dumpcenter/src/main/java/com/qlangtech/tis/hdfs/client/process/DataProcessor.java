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
import com.qlangtech.tis.exception.DataImportHDFSException;

/*
 *  读源数据进行符合条件的加工处理<br>
 *  如果某些数据增量需要对数据进行删除操作,
 *  那么需要继承该接口，默认添加一个对该行进行操作的列
 *  如：该行数据 的opt 是删除，则形成(opt,d)到map中
 *  其他操作操作以此类似
 * @since 2011-8-25 下午07:08:55
 * @version 1.0
 * @param <V>
 * @param <K>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface DataProcessor<V, K> {

    public boolean process(Map<K, V> map) throws DataImportHDFSException;
}
